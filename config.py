# -*- coding: utf-8 -*-
import os

# MySQL配置
MYSQL_USER = 'root'
MYSQL_PASSWORD = '123456'
MYSQL_HOST = '127.0.0.1'
MYSQL_PORT = 3306
MYSQL_DB = 'rental_platform'

# SQLAlchemy配置
SQLALCHEMY_DATABASE_URI = f'mysql+pymysql://{MYSQL_USER}:{MYSQL_PASSWORD}@{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB}?charset=utf8mb4'
SQLALCHEMY_TRACK_MODIFICATIONS = False

# Flask配置
SECRET_KEY = os.urandom(24).hex()
