# 🏠 智慧房屋租赁系统

基于 Spring Boot 3 + Vue.js 的房屋租赁管理平台，实现房源管理、预约看房、电子合同、在线支付等核心功能。

## 📋 项目简介

随着城市化进程的加快，房屋租赁市场日益活跃。传统的房屋租赁方式存在信息不对称、流程繁琐、管理效率低等问题。本项目通过开发一个智慧房屋租赁系统，实现房源信息的高效管理、租客与房东的便捷交互，以及租赁流程的自动化，提升房屋租赁市场的运行效率和服务质量。

## ✨ 核心功能

### 👤 用户端
- 用户注册与登录（支持租客/房东）
- 房源浏览与搜索（按区域、租金、户型等）
- 房源详情查看（图片、描述、配套设施）
- 在线预约看房
- 租赁合同管理
- 评价与反馈

### 🏠 房东端
- 房源发布、编辑、下架
- 预约看房管理（确认/拒绝）
- 合同管理（在线签署电子合同）
- 财务管理（租金收入记录）

### 🔧 管理员端
- 用户管理（租客/房东账户管理）
- 房源审核（确保信息真实有效）
- 数据统计分析（租赁数据报表）
- 系统配置（租金支付方式等）

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 开发语言 |
| Spring Boot | 3.5.6 | 核心框架 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| MySQL | 8.0.x | 关系型数据库 |
| JWT | 0.11.x | 身份认证 |
| Spring Security Crypto | - | 密码加密（BCrypt） |
| Fastjson | 2.0.x | JSON 处理 |
| Swagger/OpenAPI | 2.2.x | API 文档 |

### 前端
| 技术 | 说明 |
|------|------|
| Vue.js 3 | 前端框架 |
| Vue CLI | 项目脚手架 |
| Element Plus | UI 组件库 |
| Axios | HTTP 请求库 |

### 开发工具
- **IDE**: IntelliJ IDEA, VS Code
- **数据库管理**: Navicat / DBeaver
- **接口测试**: Python, curl
- **项目管理**: Maven
- **版本控制**: Git

## 📁 项目结构
housing/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── com.housing/
│ │ │ ├── HousingApplication.java # 启动类
│ │ │ ├── controller/ # 控制器层
│ │ │ │ ├── AuthController.java # 认证接口
│ │ │ │ ├── UserController.java # 用户接口
│ │ │ │ └── HouseController.java # 房源接口
│ │ │ ├── service/ # 业务层
│ │ │ │ ├── UserService.java
│ │ │ │ └── impl/
│ │ │ │ ├── UserServiceImpl.java
│ │ │ │ └── HouseServiceImpl.java
│ │ │ ├── mapper/ # 数据访问层
│ │ │ │ ├── UserMapper.java
│ │ │ │ └── HouseMapper.java
│ │ │ ├── entity/ # 实体类
│ │ │ │ ├── User.java
│ │ │ │ └── House.java
│ │ │ ├── dto/ # 数据传输对象
│ │ │ │ ├── request/ # 请求 DTO
│ │ │ │ └── response/ # 响应 DTO
│ │ │ ├── vo/ # 视图对象
│ │ │ │ ├── LoginVO.java
│ │ │ │ ├── PageResult.java
│ │ │ │ └── Result.java
│ │ │ ├── enums/ # 枚举类
│ │ │ │ ├── UserRoleEnum.java
│ │ │ │ ├── UserStatusEnum.java
│ │ │ │ └── HouseStatusEnum.java
│ │ │ ├── interceptor/ # 拦截器
│ │ │ │ └── AuthInterceptor.java
│ │ │ ├── config/ # 配置类
│ │ │ │ └── WebConfig.java
│ │ │ └── util/ # 工具类
│ │ │ ├── JwtUtil.java
│ │ │ └── PasswordUtil.java
│ │ └── resources/
│ │ ├── application.yml # 配置文件
│ │ └── mapper/ # MyBatis XML 映射
│ │ ├── UserMapper.xml
│ │ └── HouseMapper.xml
│ └── test/ # 测试目录
├── frontend/ # 前端项目（Vue.js）
├── pom.xml # Maven 配置
└── README.md # 项目说明


## 🚀 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+
- Node.js 16+（前端）

### 1. 克隆项目
```bash
git clone https://github.com/your-username/housing.git
cd housing
```

```sql
CREATE DATABASE housing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/housing?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

```sql
-- 用户表
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    real_name VARCHAR(50),
    role TINYINT NOT NULL DEFAULT 1 COMMENT '1-租客 2-房东 3-管理员',
    status TINYINT DEFAULT 1 COMMENT '1-正常 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 房源表
CREATE TABLE houses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    landlord_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50),
    rent_price DECIMAL(10,2) NOT NULL,
    house_type VARCHAR(30) NOT NULL,
    area DECIMAL(8,2),
    facilities JSON,
    images JSON,
    status TINYINT DEFAULT 0 COMMENT '0-待审核 1-已上架 2-已下架 3-已租出',
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (landlord_id) REFERENCES users(id) ON DELETE CASCADE
);
```

# 编译打包
```powershell
mvn clean package
./mvnw.cmd clean package
```

# 运行
```
java -jar target/housing-1.0.0.jar
```

6. 访问项目
后端 API: http://localhost:8080
Swagger 文档: http://localhost:8080/swagger-ui/index.html

📚 API 接口文档
认证接口（/api/v1/auth）
方法	路径	说明	权限
POST	/register	用户注册	公开
POST	/login	用户登录	公开
POST	/refresh	刷新 Token	公开
POST	/logout	退出登录	登录用户
用户接口（/api/v1/users）
方法	路径	说明	权限
GET	/me	获取当前用户信息	登录用户
PUT	/me	更新当前用户信息	登录用户
PUT	/me/password	修改密码	登录用户
GET	``	获取用户列表	管理员
GET	/{userId}	获取用户详情	管理员
PUT	/{userId}/status	禁用/启用用户	管理员
GET	/statistics	获取用户统计数据	管理员
房源接口（/api/v1/houses）
方法	路径	说明	权限
POST	``	发布房源	房东
PUT	/{houseId}	编辑房源	房东
GET	/my	我的房源列表	房东
PUT	/{houseId}/offline	下架房源	房东
GET	``	房源列表（搜索）	公开
GET	/{houseId}	房源详情	公开
GET	/cities	城市列表	公开
GET	/types	房源类型列表	公开
PUT	/{houseId}/approve	审核房源	管理员
GET	/pending	待审核列表	管理员
GET	/pending/count	待审核数量	管理员
请求示例

用户注册：

http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456",
  "phone": "13800138000",
  "realName": "张三",
  "role": 1
}

用户登录：

http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456"
}

响应示例：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "userInfo": {
      "id": 1,
      "username": "zhangsan",
      "phone": "13800138000",
      "realName": "张三",
      "role": 1,
      "roleDesc": "租客",
      "status": 1,
      "statusDesc": "正常",
      "createdAt": "2026-07-04 10:00:00"
    }
  }
}
```


🔑 默认账户
角色	用户名	密码
管理员	admin	123456
房东	landlord	123456
租客	tenant	123456
注：需先通过注册接口创建账户

📊 数据库设计
核心表结构
表名	说明	关键字段
users	用户表	id, username, password, role, status
houses	房源表	id, landlord_id, title, price, status
appointments	预约看房表	id, house_id, tenant_id, status
contracts	合同表	id, house_id, tenant_id, status
payments	支付记录表	id, contract_id, amount, status
reviews	评价表	id, house_id, tenant_id, rating
详细表结构请查看项目中的 schema.sql 文件

🔧 配置说明
application.yml 核心配置
yaml
# JWT 配置
jwt:
  secret: your-secret-key
  expiration: 86400000          # 24小时
  refresh-expiration: 604800000 # 7天

# 文件上传
file:
  upload-path: /uploads
  max-size: 10485760             # 10MB
📦 部署
Docker 部署
bash
# 构建镜像
docker build -t housing:1.0.0 .

# 运行容器
docker run -p 8080:8080 housing:1.0.0
Jar 包部署
bash
# 打包
mvn clean package -DskipTests

# 运行
nohup java -jar target/housing-1.0.0.jar > app.log 2>&1 &

🤝 贡献指南
Fork 本仓库
创建功能分支 (git checkout -b feature/AmazingFeature)
提交更改 (git commit -m 'Add some AmazingFeature')
推送到分支 (git push origin feature/AmazingFeature)
提交 Pull Request

📝 开发规范
遵循阿里巴巴 Java 开发手册
使用 Lombok 简化代码
统一返回格式：Result<T>
异常统一处理：GlobalExceptionHandler
API 文档：使用 Swagger 注解

📄 许可证
本项目采用 MIT 许可证

📧 联系方式
项目地址：https://github.com/your-username/housing
邮箱：your-email@example.com

🙏 致谢


Vue.js

Element Plus

⭐ 如果这个项目对你有帮助，请给个 Star！
