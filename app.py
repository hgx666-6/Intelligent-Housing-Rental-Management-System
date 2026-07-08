# -*- coding: utf-8 -*-
"""
房屋租赁管理平台 - 主应用程序
支持三种角色: 用户(租客)、房东、管理员
"""
import json
from datetime import datetime, date, timedelta
from functools import wraps
from flask import (Flask, render_template, request, redirect, url_for,
                   flash, jsonify, session)
from flask_login import (LoginManager, login_user, logout_user,
                         login_required, current_user)
from config import SQLALCHEMY_DATABASE_URI, SECRET_KEY
from models import (db, User, House, Booking, Contract, Review,
                    Favorite, BrowseHistory, Bill, Deposit, SystemConfig)

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = SQLALCHEMY_DATABASE_URI
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = SECRET_KEY

db.init_app(app)

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'index'
login_manager.login_message = '请先登录后再访问该页面。'


@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))


# ==================== 装饰器 ====================

def role_required(role):
    """角色权限装饰器"""
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            if not current_user.is_authenticated:
                return redirect(url_for('index'))
            if current_user.role != role:
                flash('您没有权限访问该页面。', 'danger')
                return redirect(url_for(f'{current_user.role}_dashboard'))
            return f(*args, **kwargs)
        return decorated_function
    return decorator


# ==================== 公共路由 ====================

@app.route('/')
def index():
    if current_user.is_authenticated:
        return redirect(url_for(f'{current_user.role}_dashboard'))
    return render_template('login.html')


@app.route('/login', methods=['POST'])
def login():
    username = request.form.get('username', '').strip()
    password = request.form.get('password', '').strip()
    role = request.form.get('role', '').strip()

    if not username or not password:
        flash('请输入用户名和密码。', 'danger')
        return redirect(url_for('index'))

    user = User.query.filter_by(username=username).first()
    if not user:
        flash('用户名不存在。', 'danger')
        return redirect(url_for('index'))

    if not user.check_password(password):
        flash('密码错误。', 'danger')
        return redirect(url_for('index'))

    if user.status == 'frozen':
        flash('该账号已被冻结，请联系管理员。', 'danger')
        return redirect(url_for('index'))

    if role and user.role != role:
        flash(f'该账号不是{role}角色，请重新选择。', 'danger')
        return redirect(url_for('index'))

    login_user(user)
    flash(f'欢迎回来，{user.username}！', 'success')
    return redirect(url_for(f'{user.role}_dashboard'))


@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'GET':
        return render_template('register.html')

    username = request.form.get('username', '').strip()
    password = request.form.get('password', '').strip()
    password2 = request.form.get('password2', '').strip()
    role = request.form.get('role', 'user').strip()
    phone = request.form.get('phone', '').strip()
    email = request.form.get('email', '').strip()

    if not username or len(username) < 2:
        flash('用户名至少需要2个字符。', 'danger')
        return redirect(url_for('register'))

    if not password or len(password) < 6:
        flash('密码至少需要6个字符。', 'danger')
        return redirect(url_for('register'))

    if password != password2:
        flash('两次输入的密码不一致。', 'danger')
        return redirect(url_for('register'))

    if role not in ('user', 'landlord'):
        flash('请选择正确的角色。', 'danger')
        return redirect(url_for('register'))

    if User.query.filter_by(username=username).first():
        flash('用户名已存在，请换一个。', 'danger')
        return redirect(url_for('register'))

    user = User(username=username, role=role, phone=phone, email=email)
    user.set_password(password)
    db.session.add(user)
    db.session.commit()

    flash('注册成功！请登录。', 'success')
    return redirect(url_for('index'))


@app.route('/logout')
@login_required
def logout():
    logout_user()
    flash('已安全退出。', 'success')
    return redirect(url_for('index'))


# ==================== 用户端路由 ====================

@app.route('/user/dashboard')
@login_required
@role_required('user')
def user_dashboard():
    # 推荐房源(最新上架的6套)
    houses = House.query.filter_by(status='listed').order_by(House.created_at.desc()).limit(6).all()
    # 我的统计
    booking_count = Booking.query.filter_by(user_id=current_user.id).count()
    fav_count = Favorite.query.filter_by(user_id=current_user.id).count()
    contract_count = Contract.query.filter_by(user_id=current_user.id, status='active').count()
    return render_template('user/dashboard.html',
                         houses=houses,
                         booking_count=booking_count,
                         fav_count=fav_count,
                         contract_count=contract_count)


@app.route('/user/houses')
@login_required
@role_required('user')
def user_houses():
    page = request.args.get('page', 1, type=int)
    area = request.args.get('area', '').strip()
    min_rent = request.args.get('min_rent', type=float)
    max_rent = request.args.get('max_rent', type=float)
    house_type = request.args.get('house_type', '').strip()
    keyword = request.args.get('keyword', '').strip()

    query = House.query.filter_by(status='listed')

    if area:
        query = query.filter(House.area.contains(area))
    if min_rent is not None:
        query = query.filter(House.rent >= min_rent)
    if max_rent is not None:
        query = query.filter(House.rent <= max_rent)
    if house_type:
        query = query.filter(House.house_type.contains(house_type))
    if keyword:
        query = query.filter(
            db.or_(House.title.contains(keyword), House.description.contains(keyword))
        )

    pagination = query.order_by(House.created_at.desc()).paginate(
        page=page, per_page=12, error_out=False)
    houses = pagination.items

    # 所有区域列表(用于下拉选项)
    areas = [r[0] for r in db.session.query(House.area).filter(
        House.area != '', House.status == 'listed').distinct().all()]

    return render_template('user/houses.html',
                         houses=houses,
                         pagination=pagination,
                         areas=areas,
                         area=area,
                         min_rent=min_rent,
                         max_rent=max_rent,
                         house_type=house_type,
                         keyword=keyword)


@app.route('/user/house/<int:house_id>')
@login_required
@role_required('user')
def user_house_detail(house_id):
    house = House.query.get_or_404(house_id)
    landlord = User.query.get(house.landlord_id)

    # 记录浏览历史
    history = BrowseHistory.query.filter_by(
        user_id=current_user.id, house_id=house_id).first()
    if history:
        history.created_at = datetime.utcnow()
    else:
        history = BrowseHistory(user_id=current_user.id, house_id=house_id)
        db.session.add(history)
    db.session.commit()

    # 是否已收藏
    is_fav = Favorite.query.filter_by(
        user_id=current_user.id, house_id=house_id).first() is not None

    # 评价列表
    reviews = Review.query.filter_by(house_id=house_id).order_by(
        Review.created_at.desc()).all()

    # 平均评分
    avg_rating = db.session.query(db.func.avg(Review.rating)).filter_by(
        house_id=house_id).scalar() or 0

    # 是否已预约
    has_booking = Booking.query.filter_by(
        user_id=current_user.id, house_id=house_id).filter(
        Booking.status.in_(['pending', 'confirmed'])).first() is not None

    return render_template('user/house_detail.html',
                         house=house,
                         landlord=landlord,
                         is_fav=is_fav,
                         reviews=reviews,
                         avg_rating=round(avg_rating, 1),
                         has_booking=has_booking)


@app.route('/user/house/<int:house_id>/book', methods=['POST'])
@login_required
@role_required('user')
def user_book_house(house_id):
    # 检查预约规则限制
    rules = SystemConfig.get_config('booking_rules',
                                    {'max_daily_bookings': 5, 'max_cancel_limit': 3})
    today_count = Booking.query.filter_by(user_id=current_user.id).filter(
        db.func.date(Booking.created_at) == date.today()).count()
    if today_count >= rules.get('max_daily_bookings', 5):
        flash(f'今日预约次数已达上限({rules["max_daily_bookings"]}次)。', 'danger')
        return redirect(url_for('user_house_detail', house_id=house_id))

    cancel_count = Booking.query.filter_by(
        user_id=current_user.id, status='cancelled').count()
    if cancel_count >= rules.get('max_cancel_limit', 3):
        flash(f'取消次数已达上限({rules["max_cancel_limit"]}次)，无法继续预约。', 'danger')
        return redirect(url_for('user_house_detail', house_id=house_id))

    booking_time_str = request.form.get('booking_time', '').strip()
    message = request.form.get('message', '').strip()

    if not booking_time_str:
        flash('请选择看房时间。', 'danger')
        return redirect(url_for('user_house_detail', house_id=house_id))

    try:
        booking_time = datetime.strptime(booking_time_str, '%Y-%m-%dT%H:%M')
    except ValueError:
        flash('时间格式不正确。', 'danger')
        return redirect(url_for('user_house_detail', house_id=house_id))

    booking = Booking(user_id=current_user.id, house_id=house_id,
                      booking_time=booking_time, message=message)
    db.session.add(booking)
    db.session.commit()

    flash('预约成功！请等待房东确认。', 'success')
    return redirect(url_for('user_bookings'))


@app.route('/user/house/<int:house_id>/favorite', methods=['POST'])
@login_required
@role_required('user')
def user_toggle_favorite(house_id):
    fav = Favorite.query.filter_by(user_id=current_user.id, house_id=house_id).first()
    if fav:
        db.session.delete(fav)
        db.session.commit()
        return jsonify({'status': 'ok', 'action': 'removed'})
    else:
        fav = Favorite(user_id=current_user.id, house_id=house_id)
        db.session.add(fav)
        db.session.commit()
        return jsonify({'status': 'ok', 'action': 'added'})


@app.route('/user/bookings')
@login_required
@role_required('user')
def user_bookings():
    status_filter = request.args.get('status', '').strip()
    query = Booking.query.filter_by(user_id=current_user.id)
    if status_filter:
        query = query.filter_by(status=status_filter)
    bookings = query.order_by(Booking.created_at.desc()).all()
    return render_template('user/my_bookings.html', bookings=bookings,
                         status_filter=status_filter)


@app.route('/user/booking/<int:booking_id>/cancel', methods=['POST'])
@login_required
@role_required('user')
def user_cancel_booking(booking_id):
    booking = Booking.query.get_or_404(booking_id)
    if booking.user_id != current_user.id:
        flash('无权操作。', 'danger')
        return redirect(url_for('user_bookings'))
    if booking.status in ('pending', 'confirmed'):
        booking.status = 'cancelled'
        db.session.commit()
        flash('预约已取消。', 'success')
    else:
        flash('当前状态无法取消。', 'danger')
    return redirect(url_for('user_bookings'))


@app.route('/user/contracts')
@login_required
@role_required('user')
def user_contracts():
    contracts = Contract.query.filter_by(user_id=current_user.id).order_by(
        Contract.created_at.desc()).all()
    return render_template('user/my_contracts.html', contracts=contracts)


@app.route('/user/favorites')
@login_required
@role_required('user')
def user_favorites():
    favs = Favorite.query.filter_by(user_id=current_user.id).order_by(
        Favorite.created_at.desc()).all()
    # 对比功能：从query参数获取要对比的房源ID
    compare_ids = request.args.get('compare', '').split(',')
    compare_houses = []
    if compare_ids and compare_ids[0]:
        try:
            ids = [int(x) for x in compare_ids if x.strip().isdigit()]
            compare_houses = House.query.filter(House.id.in_(ids[:3])).all()
        except ValueError:
            pass
    return render_template('user/favorites.html', favs=favs,
                         compare_houses=compare_houses,
                         compare_ids=request.args.get('compare', ''))


@app.route('/user/history')
@login_required
@role_required('user')
def user_history():
    history = BrowseHistory.query.filter_by(user_id=current_user.id).order_by(
        BrowseHistory.created_at.desc()).limit(50).all()
    return render_template('user/history.html', history=history)


@app.route('/user/bills')
@login_required
@role_required('user')
def user_bills():
    bills = Bill.query.filter_by(user_id=current_user.id).order_by(
        Bill.created_at.desc()).all()
    # 分类统计
    stats = {}
    for t in ['rent', 'utility', 'maintenance', 'deposit']:
        stats[t] = db.session.query(db.func.sum(Bill.amount)).filter_by(
            user_id=current_user.id, type=t).scalar() or 0
    return render_template('user/bills.html', bills=bills, stats=stats)


@app.route('/user/review', methods=['POST'])
@login_required
@role_required('user')
def user_add_review():
    house_id = request.form.get('house_id', type=int)
    rating = request.form.get('rating', type=int)
    content = request.form.get('content', '').strip()

    if not house_id or not rating:
        flash('参数错误。', 'danger')
        return redirect(request.referrer or url_for('user_dashboard'))

    # 检查是否已评价
    existing = Review.query.filter_by(
        user_id=current_user.id, house_id=house_id).first()
    if existing:
        flash('您已对该房源进行过评价。', 'danger')
        return redirect(url_for('user_house_detail', house_id=house_id))

    review = Review(user_id=current_user.id, house_id=house_id,
                    rating=rating, content=content)
    db.session.add(review)
    db.session.commit()
    flash('评价提交成功！', 'success')
    return redirect(url_for('user_house_detail', house_id=house_id))


# ==================== 房东端路由 ====================

@app.route('/landlord/dashboard')
@login_required
@role_required('landlord')
def landlord_dashboard():
    landlord_id = current_user.id

    # 房源总数
    total_houses = House.query.filter_by(landlord_id=landlord_id).count()
    listed_houses = House.query.filter_by(landlord_id=landlord_id, status='listed').count()
    vacancy_rate = round((1 - listed_houses / total_houses * 1) * 100, 1) if total_houses > 0 else 0

    # 本月租金收入
    this_month = date.today().replace(day=1)
    monthly_income = db.session.query(db.func.sum(Bill.amount)).join(
        Contract).filter(
        Contract.landlord_id == landlord_id,
        Bill.type == 'rent',
        Bill.status == 'paid',
        Bill.created_at >= this_month
    ).scalar() or 0

    # 待处理预约数
    pending_bookings = Booking.query.join(House).filter(
        House.landlord_id == landlord_id,
        Booking.status == 'pending'
    ).count()

    # 活跃合同数
    active_contracts = Contract.query.filter_by(
        landlord_id=landlord_id, status='active').count()

    # 近6个月租金收入趋势
    income_trend = []
    for i in range(5, -1, -1):
        month_start = date.today().replace(day=1) - timedelta(days=i * 30)
        month_start = month_start.replace(day=1)
        if i == 0:
            next_month = date.today()
        else:
            next_month = (month_start.replace(day=28) + timedelta(days=4)).replace(day=1)
        income = db.session.query(db.func.sum(Bill.amount)).join(Contract).filter(
            Contract.landlord_id == landlord_id,
            Bill.type == 'rent',
            Bill.status == 'paid',
            Bill.created_at >= month_start,
            Bill.created_at < next_month
        ).scalar() or 0
        income_trend.append({
            'month': month_start.strftime('%Y-%m'),
            'income': float(income)
        })

    # 房源收益排行
    house_revenue = []
    houses = House.query.filter_by(landlord_id=landlord_id).all()
    for h in houses:
        revenue = db.session.query(db.func.sum(Bill.amount)).join(Contract).filter(
            Contract.house_id == h.id,
            Bill.type == 'rent',
            Bill.status == 'paid'
        ).scalar() or 0
        house_revenue.append({'title': h.title, 'revenue': float(revenue)})
    house_revenue.sort(key=lambda x: x['revenue'], reverse=True)

    # 待收租金
    pending_rent = db.session.query(db.func.sum(Bill.amount)).join(Contract).filter(
        Contract.landlord_id == landlord_id,
        Bill.type == 'rent',
        Bill.status == 'unpaid'
    ).scalar() or 0

    return render_template('landlord/dashboard.html',
                         total_houses=total_houses,
                         listed_houses=listed_houses,
                         vacancy_rate=vacancy_rate,
                         monthly_income=monthly_income,
                         pending_bookings=pending_bookings,
                         active_contracts=active_contracts,
                         income_trend=json.dumps(income_trend, ensure_ascii=False),
                         house_revenue=json.dumps(house_revenue, ensure_ascii=False),
                         pending_rent=pending_rent)


@app.route('/landlord/houses')
@login_required
@role_required('landlord')
def landlord_houses():
    houses = House.query.filter_by(landlord_id=current_user.id).order_by(
        House.created_at.desc()).all()
    return render_template('landlord/my_houses.html', houses=houses)


@app.route('/landlord/house/add', methods=['POST'])
@login_required
@role_required('landlord')
def landlord_add_house():
    title = request.form.get('title', '').strip()
    description = request.form.get('description', '').strip()
    area = request.form.get('area', '').strip()
    address = request.form.get('address', '').strip()
    rent = request.form.get('rent', type=float)
    house_type = request.form.get('house_type', '').strip()
    bedrooms = request.form.get('bedrooms', type=int, default=1)
    area_size = request.form.get('area_size', type=float, default=0)
    floor = request.form.get('floor', '').strip()
    facilities = request.form.get('facilities', '').strip()

    if not title or not rent:
        flash('标题和租金为必填项。', 'danger')
        return redirect(url_for('landlord_houses'))

    house = House(
        landlord_id=current_user.id,
        title=title,
        description=description,
        area=area,
        address=address,
        rent=rent,
        house_type=house_type,
        bedrooms=bedrooms,
        area_size=area_size,
        floor=floor,
        facilities=facilities
    )
    db.session.add(house)
    db.session.commit()
    flash('房源发布成功！', 'success')
    return redirect(url_for('landlord_houses'))


@app.route('/landlord/house/<int:house_id>/edit', methods=['POST'])
@login_required
@role_required('landlord')
def landlord_edit_house(house_id):
    house = House.query.get_or_404(house_id)
    if house.landlord_id != current_user.id:
        flash('无权操作。', 'danger')
        return redirect(url_for('landlord_houses'))

    house.title = request.form.get('title', house.title).strip()
    house.description = request.form.get('description', house.description).strip()
    house.area = request.form.get('area', house.area).strip()
    house.address = request.form.get('address', house.address).strip()
    rent = request.form.get('rent', type=float)
    if rent:
        house.rent = rent
    house.house_type = request.form.get('house_type', house.house_type).strip()
    house.bedrooms = request.form.get('bedrooms', type=int, default=house.bedrooms)
    house.area_size = request.form.get('area_size', type=float, default=house.area_size)
    house.floor = request.form.get('floor', house.floor).strip()
    house.facilities = request.form.get('facilities', house.facilities).strip()

    db.session.commit()
    flash('房源信息已更新。', 'success')
    return redirect(url_for('landlord_houses'))


@app.route('/landlord/house/<int:house_id>/delete', methods=['POST'])
@login_required
@role_required('landlord')
def landlord_delete_house(house_id):
    house = House.query.get_or_404(house_id)
    if house.landlord_id != current_user.id:
        flash('无权操作。', 'danger')
        return redirect(url_for('landlord_houses'))

    # 删除关联数据
    Booking.query.filter_by(house_id=house_id).delete()
    Review.query.filter_by(house_id=house_id).delete()
    Favorite.query.filter_by(house_id=house_id).delete()
    BrowseHistory.query.filter_by(house_id=house_id).delete()
    db.session.delete(house)
    db.session.commit()
    flash('房源已删除。', 'success')
    return redirect(url_for('landlord_houses'))


@app.route('/landlord/bookings')
@login_required
@role_required('landlord')
def landlord_bookings():
    status_filter = request.args.get('status', '').strip()
    query = Booking.query.join(House).filter(House.landlord_id == current_user.id)
    if status_filter:
        query = query.filter(Booking.status == status_filter)
    bookings = query.order_by(Booking.created_at.desc()).all()
    return render_template('landlord/bookings.html', bookings=bookings,
                         status_filter=status_filter)


@app.route('/landlord/booking/<int:booking_id>/<action>', methods=['POST'])
@login_required
@role_required('landlord')
def landlord_handle_booking(booking_id, action):
    booking = Booking.query.get_or_404(booking_id)
    house = House.query.get(booking.house_id)
    if house.landlord_id != current_user.id:
        flash('无权操作。', 'danger')
        return redirect(url_for('landlord_bookings'))

    if action == 'confirm':
        booking.status = 'confirmed'
        flash('已确认预约。', 'success')
    elif action == 'reject':
        booking.status = 'rejected'
        flash('已拒绝预约。', 'danger')
    elif action == 'complete':
        booking.status = 'completed'
        flash('预约已完成。', 'success')
    else:
        flash('无效操作。', 'danger')
        return redirect(url_for('landlord_bookings'))

    db.session.commit()
    return redirect(url_for('landlord_bookings'))


@app.route('/landlord/contracts')
@login_required
@role_required('landlord')
def landlord_contracts():
    contracts = Contract.query.filter_by(landlord_id=current_user.id).order_by(
        Contract.created_at.desc()).all()
    # 所有租客用户
    users = User.query.filter_by(role='user', status='active').all()
    houses = House.query.filter_by(landlord_id=current_user.id).all()
    return render_template('landlord/contracts.html', contracts=contracts,
                         users=users, houses=houses)


@app.route('/landlord/contract/add', methods=['POST'])
@login_required
@role_required('landlord')
def landlord_add_contract():
    user_id = request.form.get('user_id', type=int)
    house_id = request.form.get('house_id', type=int)
    start_date_str = request.form.get('start_date', '').strip()
    end_date_str = request.form.get('end_date', '').strip()
    rent = request.form.get('rent', type=float)
    deposit = request.form.get('deposit', type=float, default=0)

    if not all([user_id, house_id, start_date_str, end_date_str, rent]):
        flash('请填写所有必填项。', 'danger')
        return redirect(url_for('landlord_contracts'))

    house = House.query.get(house_id)
    if not house or house.landlord_id != current_user.id:
        flash('房源信息错误。', 'danger')
        return redirect(url_for('landlord_contracts'))

    start_date = datetime.strptime(start_date_str, '%Y-%m-%d').date()
    end_date = datetime.strptime(end_date_str, '%Y-%m-%d').date()

    contract = Contract(
        user_id=user_id,
        house_id=house_id,
        landlord_id=current_user.id,
        start_date=start_date,
        end_date=end_date,
        rent=rent,
        deposit=deposit
    )
    db.session.add(contract)
    db.session.flush()

    # 自动创建首月租金账单
    bill = Bill(
        user_id=user_id,
        contract_id=contract.id,
        type='rent',
        amount=rent,
        description=f'首月租金 - {house.title}',
        due_date=start_date
    )
    db.session.add(bill)

    # 押金记录
    if deposit > 0:
        dep = Deposit(
            user_id=user_id,
            house_id=house_id,
            amount=deposit,
            type='paid',
            description=f'押金 - {house.title}'
        )
        db.session.add(dep)
        # 押金账单
        dep_bill = Bill(
            user_id=user_id,
            contract_id=contract.id,
            type='deposit',
            amount=deposit,
            description=f'押金 - {house.title}',
            due_date=start_date
        )
        db.session.add(dep_bill)

    db.session.commit()
    flash('合同创建成功！', 'success')
    return redirect(url_for('landlord_contracts'))


@app.route('/landlord/finance')
@login_required
@role_required('landlord')
def landlord_finance():
    # 总收入
    total_income = db.session.query(db.func.sum(Bill.amount)).join(Contract).filter(
        Contract.landlord_id == current_user.id,
        Bill.type == 'rent',
        Bill.status == 'paid'
    ).scalar() or 0

    # 本月收入
    this_month = date.today().replace(day=1)
    month_income = db.session.query(db.func.sum(Bill.amount)).join(Contract).filter(
        Contract.landlord_id == current_user.id,
        Bill.type == 'rent',
        Bill.status == 'paid',
        Bill.created_at >= this_month
    ).scalar() or 0

    # 待收租金
    pending = db.session.query(db.func.sum(Bill.amount)).join(Contract).filter(
        Contract.landlord_id == current_user.id,
        Bill.status == 'unpaid'
    ).scalar() or 0

    # 收入明细
    bills = Bill.query.join(Contract).filter(
        Contract.landlord_id == current_user.id
    ).order_by(Bill.created_at.desc()).limit(50).all()

    # 房源收益排行
    house_revenue = []
    houses = House.query.filter_by(landlord_id=current_user.id).all()
    for h in houses:
        revenue = db.session.query(db.func.sum(Bill.amount)).join(Contract).filter(
            Contract.house_id == h.id,
            Bill.type == 'rent',
            Bill.status == 'paid'
        ).scalar() or 0
        contract_count = Contract.query.filter_by(house_id=h.id).count()
        avg_period = 0
        if contract_count > 0:
            total_days = db.session.query(
                db.func.sum(db.func.datediff(Contract.end_date, Contract.start_date))
            ).filter_by(house_id=h.id).scalar() or 0
            avg_period = round(total_days / contract_count, 1)
        house_revenue.append({
            'title': h.title,
            'revenue': float(revenue),
            'contracts': contract_count,
            'avg_period': avg_period
        })
    house_revenue.sort(key=lambda x: x['revenue'], reverse=True)

    return render_template('landlord/finance.html',
                         total_income=total_income,
                         month_income=month_income,
                         pending=pending,
                         bills=bills,
                         house_revenue=house_revenue)


@app.route('/landlord/deposits')
@login_required
@role_required('landlord')
def landlord_deposits():
    deposits = Deposit.query.join(House).filter(
        House.landlord_id == current_user.id
    ).order_by(Deposit.created_at.desc()).all()
    return render_template('landlord/deposits.html', deposits=deposits)


@app.route('/landlord/review/<int:review_id>/reply', methods=['POST'])
@login_required
@role_required('landlord')
def landlord_reply_review(review_id):
    review = Review.query.get_or_404(review_id)
    house = House.query.get(review.house_id)
    if house.landlord_id != current_user.id:
        flash('无权操作。', 'danger')
        return redirect(request.referrer or url_for('landlord_dashboard'))

    review.reply = request.form.get('reply', '').strip()
    db.session.commit()
    flash('回复已提交。', 'success')
    return redirect(request.referrer or url_for('landlord_dashboard'))


# ==================== 管理员端路由 ====================

@app.route('/admin/dashboard')
@login_required
@role_required('admin')
def admin_dashboard():
    total_users = User.query.filter_by(role='user').count()
    total_landlords = User.query.filter_by(role='landlord').count()
    total_houses = House.query.count()
    listed_houses = House.query.filter_by(status='listed').count()
    total_bookings = Booking.query.count()
    total_contracts = Contract.query.filter_by(status='active').count()
    this_month = date.today().replace(day=1)
    month_rent = db.session.query(db.func.sum(Bill.amount)).filter(
        Bill.type == 'rent', Bill.status == 'paid',
        Bill.created_at >= this_month
    ).scalar() or 0
    frozen_users = User.query.filter_by(status='frozen').count()

    # 预约状态分布
    booking_stats = {}
    for s in ['pending', 'confirmed', 'rejected', 'cancelled', 'completed']:
        booking_stats[s] = Booking.query.filter_by(status=s).count()

    return render_template('admin/dashboard.html',
                         total_users=total_users,
                         total_landlords=total_landlords,
                         total_houses=total_houses,
                         listed_houses=listed_houses,
                         total_bookings=total_bookings,
                         total_contracts=total_contracts,
                         month_rent=month_rent,
                         frozen_users=frozen_users,
                         booking_stats=json.dumps(booking_stats, ensure_ascii=False))


@app.route('/admin/users')
@login_required
@role_required('admin')
def admin_users():
    role_filter = request.args.get('role', '').strip()
    query = User.query
    if role_filter:
        query = query.filter_by(role=role_filter)
    users = query.order_by(User.created_at.desc()).all()
    return render_template('admin/users.html', users=users, role_filter=role_filter)


@app.route('/admin/user/<int:user_id>/freeze', methods=['POST'])
@login_required
@role_required('admin')
def admin_freeze_user(user_id):
    user = User.query.get_or_404(user_id)
    if user.role == 'admin':
        flash('不能冻结管理员账号。', 'danger')
        return redirect(url_for('admin_users'))
    user.status = 'frozen' if user.status == 'active' else 'active'
    db.session.commit()
    flash(f'用户{user.username}已{"冻结" if user.status == "frozen" else "解冻"}。', 'success')
    return redirect(url_for('admin_users'))


@app.route('/admin/user/<int:user_id>/reset-password', methods=['POST'])
@login_required
@role_required('admin')
def admin_reset_password(user_id):
    user = User.query.get_or_404(user_id)
    user.set_password('123456')
    db.session.commit()
    flash(f'用户{user.username}的密码已重置为: 123456', 'success')
    return redirect(url_for('admin_users'))


@app.route('/admin/houses')
@login_required
@role_required('admin')
def admin_houses():
    houses = House.query.order_by(House.created_at.desc()).all()
    return render_template('admin/houses.html', houses=houses)


@app.route('/admin/house/<int:house_id>/toggle', methods=['POST'])
@login_required
@role_required('admin')
def admin_toggle_house(house_id):
    house = House.query.get_or_404(house_id)
    house.status = 'unlisted' if house.status == 'listed' else 'listed'
    db.session.commit()
    flash(f'房源已{"下架" if house.status == "unlisted" else "上架"}。', 'success')
    return redirect(url_for('admin_houses'))


@app.route('/admin/statistics')
@login_required
@role_required('admin')
def admin_statistics():
    # 月度租金流水
    monthly_income = []
    for i in range(5, -1, -1):
        month_start = date.today().replace(day=1) - timedelta(days=i * 30)
        month_start = month_start.replace(day=1)
        if i == 0:
            next_month = date.today()
        else:
            next_month = (month_start.replace(day=28) + timedelta(days=4)).replace(day=1)
        income = db.session.query(db.func.sum(Bill.amount)).filter(
            Bill.type == 'rent', Bill.status == 'paid',
            Bill.created_at >= month_start, Bill.created_at < next_month
        ).scalar() or 0
        orders = Bill.query.filter(
            Bill.type == 'rent', Bill.status == 'paid',
            Bill.created_at >= month_start, Bill.created_at < next_month
        ).count()
        overdue = Bill.query.filter(
            Bill.status == 'unpaid', Bill.due_date < next_month,
            Bill.created_at >= month_start
        ).count()
        monthly_income.append({
            'month': month_start.strftime('%Y-%m'),
            'income': float(income),
            'orders': orders,
            'overdue': overdue
        })

    # 各房源预约热度排行
    house_bookings = db.session.query(
        House.title, db.func.count(Booking.id).label('cnt')
    ).join(Booking).group_by(House.id).order_by(db.text('cnt DESC')).all()
    booking_heat = [{'title': h[0], 'count': h[1]} for h in house_bookings]

    # 签约转化率
    total_bookings = Booking.query.count()
    completed = Booking.query.filter_by(status='completed').count()
    conversion_rate = round(completed / total_bookings * 100, 1) if total_bookings > 0 else 0

    # 热门收藏房源排行
    fav_ranking = db.session.query(
        House.title, db.func.count(Favorite.id).label('cnt')
    ).join(Favorite).group_by(House.id).order_by(db.text('cnt DESC')).limit(10).all()
    fav_heat = [{'title': f[0], 'count': f[1]} for f in fav_ranking]

    return render_template('admin/statistics.html',
                         monthly_income=json.dumps(monthly_income, ensure_ascii=False),
                         booking_heat=json.dumps(booking_heat, ensure_ascii=False),
                         fav_heat=json.dumps(fav_heat, ensure_ascii=False),
                         conversion_rate=conversion_rate,
                         total_bookings=total_bookings,
                         completed=completed)


@app.route('/admin/rules', methods=['GET', 'POST'])
@login_required
@role_required('admin')
def admin_rules():
    if request.method == 'POST':
        rent_rules = {
            'min_rent_period': request.form.get('min_rent_period', type=int, default=1),
            'deposit_multiplier': request.form.get('deposit_multiplier', type=int, default=1),
            'deposit_months': request.form.get('deposit_months', type=int, default=3)
        }
        booking_rules = {
            'max_daily_bookings': request.form.get('max_daily_bookings', type=int, default=5),
            'max_cancel_limit': request.form.get('max_cancel_limit', type=int, default=3)
        }
        SystemConfig.set_config('rent_rules', rent_rules)
        SystemConfig.set_config('booking_rules', booking_rules)
        flash('规则已更新。', 'success')
        return redirect(url_for('admin_rules'))

    rent_rules = SystemConfig.get_config('rent_rules',
                                         {'min_rent_period': 1, 'deposit_multiplier': 1, 'deposit_months': 3})
    booking_rules = SystemConfig.get_config('booking_rules',
                                            {'max_daily_bookings': 5, 'max_cancel_limit': 3})
    return render_template('admin/rules.html', rent_rules=rent_rules,
                         booking_rules=booking_rules)


@app.route('/admin/reviews')
@login_required
@role_required('admin')
def admin_reviews():
    reviews = Review.query.order_by(Review.created_at.desc()).all()
    return render_template('admin/reviews.html', reviews=reviews)


@app.route('/admin/review/<int:review_id>/delete', methods=['POST'])
@login_required
@role_required('admin')
def admin_delete_review(review_id):
    review = Review.query.get_or_404(review_id)
    db.session.delete(review)
    db.session.commit()
    flash('评价已删除。', 'success')
    return redirect(url_for('admin_reviews'))


# ==================== 启动 ====================

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
