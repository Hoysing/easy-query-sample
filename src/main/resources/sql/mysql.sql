-- 删除公司表
DROP TABLE IF EXISTS company;
-- 创建公司表
CREATE TABLE company (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    parent_id INTEGER,
    deleted BOOLEAN
);

-- 删除公司表
DROP TABLE IF EXISTS company_detail;
-- 创建公司表
CREATE TABLE IF NOT EXISTS company_detail (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(255),
    company_id INT
);



-- 删除权限表
DROP TABLE IF EXISTS permission;
-- 创建权限表
CREATE TABLE permission (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- 删除角色表
DROP TABLE IF EXISTS role;
-- 创建角色表
CREATE TABLE role (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- 删除角色权限关联表
DROP TABLE IF EXISTS role_permission;
-- 创建角色权限关联表
CREATE TABLE role_permission (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    role_id INTEGER,
    permission_id INTEGER
);

-- 删除用户表
DROP TABLE IF EXISTS user;
-- 创建用户表
CREATE TABLE user (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    balance INTEGER,
    version INTEGER,
    create_time DATETIME,
    update_time DATETIME,
    enabled BOOLEAN,
    deleted BOOLEAN,
    company_id INTEGER
);

-- 删除用户详情表
DROP TABLE IF EXISTS user_detail;
-- 创建用户详情表
CREATE TABLE user_detail (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    signature VARCHAR(255),
    user_id INTEGER
);

-- 删除用户角色关联表
DROP TABLE IF EXISTS user_role;
-- 创建用户角色关联表
CREATE TABLE user_role (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER,
    role_id INTEGER
);

-- 删除商品表
DROP TABLE IF EXISTS product;

-- 创建商品表
CREATE TABLE product (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    deleted_time DATETIME,
    deleted_user_id INTEGER
);

-- 插入公司数据
INSERT INTO company (name, parent_id,deleted) VALUES ('总公司', NULL,0);
INSERT INTO company (name, parent_id,deleted) VALUES ('分公司A', 1,0);
INSERT INTO company (name, parent_id,deleted) VALUES ('分公司B', 1,0);

-- 插入测试数据到 company_detail 表
INSERT INTO company_detail (address, company_id)
VALUES ('广州市番禺区', 1),('江门市鹤山市', 2),('九江市濂溪区', 3)
;

-- 插入公司详情数据
INSERT INTO company_detail (address, company_id) VALUES ('总公司', NULL);
INSERT INTO company (name, parent_id) VALUES ('分公司A', 1);
INSERT INTO company (name, parent_id) VALUES ('分公司B', 1);

-- 插入用户数据
INSERT INTO user (name,balance, create_time,update_time,version, enabled,deleted, company_id) VALUES ('张三', 999,NOW(),NOW(), TRUE,1,0, 1);
INSERT INTO user (name,balance, create_time, update_time,version, enabled, deleted, company_id) VALUES ('李四', 100,NOW(),NOW(),TRUE,1,0,2);
INSERT INTO user (name,balance, create_time, update_time,version, enabled,deleted,  company_id) VALUES ('王五', 60,NOW(),NOW(), FALSE,1,0, 3);

-- 插入用户详情数据
INSERT INTO user_detail (signature, user_id) VALUES ('静水流深', 1);
INSERT INTO user_detail (signature, user_id) VALUES ('海阔天空', 2);
INSERT INTO user_detail (signature, user_id) VALUES ('岁月静好', 3);

-- 插入角色数据
INSERT INTO role (name) VALUES ('管理员');
INSERT INTO role (name) VALUES ('编辑员');
INSERT INTO role (name) VALUES ('用户');

-- 插入权限数据
INSERT INTO permission (name) VALUES ('查看报表');
INSERT INTO permission (name) VALUES ('管理用户');
INSERT INTO permission (name) VALUES ('编辑内容');


-- 插入用户角色关联数据
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO user_role (user_id, role_id) VALUES (1, 3);
INSERT INTO user_role (user_id, role_id) VALUES (2, 2);
INSERT INTO user_role (user_id, role_id) VALUES (3, 3);

-- 插入角色权限关联数据
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permission (role_id, permission_id) VALUES (2, 3);
INSERT INTO role_permission (role_id, permission_id) VALUES (3, 1);

