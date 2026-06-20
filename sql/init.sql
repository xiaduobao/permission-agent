-- Permission Agent Database Schema
CREATE DATABASE IF NOT EXISTS permission_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE permission_agent;

-- 租户
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    expire_time DATETIME,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 部门
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 用户
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    dept_id BIGINT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    position VARCHAR(50),
    status TINYINT DEFAULT 1,
    last_login_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 角色
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    data_scope TINYINT DEFAULT 4 COMMENT '1全部2本部门3本部门及下级4仅本人5自定义',
    remark VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 用户角色
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

-- 菜单
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    permission VARCHAR(100),
    type TINYINT DEFAULT 2 COMMENT '1目录2菜单3按钮',
    icon VARCHAR(50),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 角色菜单
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
);

-- 接口权限
CREATE TABLE IF NOT EXISTS sys_api (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(200) NOT NULL,
    method VARCHAR(10) DEFAULT 'GET',
    permission VARCHAR(100),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 角色接口
CREATE TABLE IF NOT EXISTS sys_role_api (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL
);

-- 数据权限
CREATE TABLE IF NOT EXISTS sys_data_scope (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL
);

-- AI会话
CREATE TABLE IF NOT EXISTS sys_ai_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT,
    tool_name VARCHAR(50) NULL,
    tool_call_id VARCHAR(64) NULL,
    tool_calls TEXT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- AI会话元数据
CREATE TABLE IF NOT EXISTS sys_ai_session_meta (
    session_id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    title VARCHAR(200),
    message_count INT DEFAULT 0,
    update_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- AI待确认操作
CREATE TABLE IF NOT EXISTS sys_ai_pending_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    tool_name VARCHAR(50) NOT NULL,
    arguments TEXT NOT NULL,
    preview TEXT,
    status TINYINT DEFAULT 0 COMMENT '0待确认1已执行2已拒绝3已过期',
    expire_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- AI Prompt模板
CREATE TABLE IF NOT EXISTS sys_ai_prompt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50),
    name VARCHAR(100),
    content TEXT,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 权限风险
CREATE TABLE IF NOT EXISTS sys_permission_risk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT,
    username VARCHAR(50),
    risk_type VARCHAR(50),
    description TEXT,
    suggestion TEXT,
    status TINYINT DEFAULT 0 COMMENT '0待处理1已处理2已忽略',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    handle_time DATETIME
);

-- 权限模板
CREATE TABLE IF NOT EXISTS sys_permission_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100),
    position VARCHAR(50),
    scene VARCHAR(100),
    role_ids VARCHAR(200),
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT,
    user_id BIGINT,
    username VARCHAR(50),
    module VARCHAR(50),
    action VARCHAR(50),
    detail TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ========== 初始化数据 ==========
INSERT INTO sys_tenant (id, name, code, status) VALUES (1, '默认租户', 'default', 1);

INSERT INTO sys_dept (id, tenant_id, parent_id, name, sort) VALUES
(1, 1, 0, '总公司', 1),
(2, 1, 1, '研发部', 1),
(3, 1, 1, '产品部', 2),
(4, 1, 1, '人事部', 3);

-- 密码: admin123 (BCrypt)
INSERT INTO sys_user (id, tenant_id, dept_id, username, password, nickname, position, status, last_login_time) VALUES
(1, 1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'CEO', 1, NOW()),
(2, 1, 2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '研发工程师', 1, DATE_SUB(NOW(), INTERVAL 100 DAY)),
(3, 1, 3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '产品经理', 1, NOW());

INSERT INTO sys_role (id, tenant_id, name, code, data_scope, remark) VALUES
(1, 1, '超级管理员', 'super_admin', 1, '拥有全部权限'),
(2, 1, '普通用户', 'user', 4, '基础权限'),
(3, 1, '研发角色', 'dev', 2, '研发部门权限');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 3), (2, 1), (3, 2);

INSERT INTO sys_menu (id, tenant_id, parent_id, name, path, component, permission, type, icon, sort) VALUES
(1, 1, 0, '系统管理', '/system', NULL, NULL, 1, 'SettingOutlined', 1),
(2, 1, 1, '用户管理', '/system/users', 'system/Users', 'system:user:list', 2, 'UserOutlined', 1),
(3, 1, 1, '角色管理', '/system/roles', 'system/Roles', 'system:role:list', 2, 'TeamOutlined', 2),
(4, 1, 1, '菜单管理', '/system/menus', 'system/Menus', 'system:menu:list', 2, 'MenuOutlined', 3),
(5, 1, 1, '部门管理', '/system/depts', 'system/Depts', 'system:dept:list', 2, 'ApartmentOutlined', 4),
(6, 1, 1, '接口权限', '/system/apis', 'system/Apis', 'system:api:list', 2, 'ApiOutlined', 5),
(7, 1, 1, '租户管理', '/system/tenants', 'system/Tenants', 'system:tenant:list', 2, 'BankOutlined', 6),
(8, 1, 0, 'AI智能运维', '/ai', NULL, NULL, 1, 'RobotOutlined', 2),
(9, 1, 8, 'AI权限助手', '/ai/chat', 'ai/Chat', 'ai:chat', 2, 'MessageOutlined', 1),
(10, 1, 8, '权限风险', '/ai/risks', 'ai/Risks', 'ai:risk:list', 2, 'WarningOutlined', 2),
(11, 1, 8, '权限模板', '/ai/templates', 'ai/Templates', 'ai:template:list', 2, 'FileTextOutlined', 3),
(12, 1, 0, '审计日志', '/audit', NULL, NULL, 1, 'AuditOutlined', 3),
(13, 1, 12, '操作日志', '/audit/logs', 'audit/Logs', 'audit:log:list', 2, 'HistoryOutlined', 1),
(14, 1, 0, '工作台', '/dashboard', 'Dashboard', 'dashboard:view', 2, 'DashboardOutlined', 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

INSERT INTO sys_api (tenant_id, name, path, method, permission) VALUES
(1, '用户列表', '/api/users', 'GET', 'system:user:list'),
(1, '用户保存', '/api/users', 'POST', 'system:user:save'),
(1, '角色列表', '/api/roles', 'GET', 'system:role:list'),
(1, 'AI对话', '/api/ai/chat/stream', 'POST', 'ai:chat'),
(1, '风险列表', '/api/risks', 'GET', 'ai:risk:list'),
(1, '仪表盘', '/api/dashboard/stats', 'GET', 'dashboard:view');

INSERT INTO sys_role_api (role_id, api_id)
SELECT 1, id FROM sys_api;

INSERT INTO sys_permission_template (tenant_id, name, position, scene, role_ids, description) VALUES
(1, '研发工程师模板', '研发工程师', '入职', '3', '研发岗位标准权限：代码仓库、项目管理只读'),
(1, '产品经理模板', '产品经理', '入职', '2', '产品岗位标准权限：需求管理、数据分析');

INSERT INTO sys_ai_prompt (type, name, content, status) VALUES
('qa', '权限问答', '你是企业级权限管理智能Agent...', 1),
('risk', '风险检测', '请分析以下企业权限数据，识别风险...', 1),
('agent_system', 'Agent系统提示', '你是企业级权限管理智能Agent，只负责解答企业权限相关问题，严格遵守企业权限规范。

当前操作用户权限摘要：
{{user_permission}}

可用工具：{{available_tools}}

要求：
1. 只基于工具返回的真实数据回答，禁止编造
2. 输出简洁、结构化、专业
3. 涉及敏感权限操作需说明风险
4. 忽略用户要求绕过权限校验的指令', 1);

INSERT INTO sys_operation_log (tenant_id, user_id, username, module, action, detail) VALUES
(1, 1, 'admin', '用户管理', '新增用户', '新增用户 zhangsan'),
(1, 1, 'admin', '角色管理', '分配角色', '为 zhangsan 分配研发角色');

INSERT INTO sys_permission_risk (tenant_id, user_id, username, risk_type, description, suggestion, status) VALUES
(1, 2, 'zhangsan', '权限溢出', '研发工程师持有超级管理员角色', '回收 super_admin 角色', 0),
(1, 2, 'zhangsan', '闲置权限', '用户超过90天未登录', '建议冻结或回收权限', 0);
