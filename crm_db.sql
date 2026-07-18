-- ============================================================================
-- 领航 CRM：可重复执行的 MySQL 8 初始化脚本
-- 用法：在 MySQL 8 中执行整个文件，创建 crm_db、13 张表以及完整演示数据。
-- 注意：脚本会删除同名旧表后重建，适合开发/演示环境，不要直接对有真实数据的生产库执行。
-- ============================================================================

-- 数据库使用 utf8mb4，可完整保存中文、英文和四字节 Unicode 字符。
CREATE DATABASE IF NOT EXISTS crm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- 后续的建表和插入语句都在 crm_db 中执行。
USE crm_db;

-- 重建前临时关闭外键检查，才能顺利删除存在相互引用的旧表。
SET FOREIGN_KEY_CHECKS=0;
-- IF EXISTS 使表不存在时也不报错；先删子表、后删父表。
DROP TABLE IF EXISTS crm_customer_transfer_log;
DROP TABLE IF EXISTS crm_contract_product;
DROP TABLE IF EXISTS crm_contract;
DROP TABLE IF EXISTS crm_follow_up_record;
DROP TABLE IF EXISTS crm_opportunity_product;
DROP TABLE IF EXISTS crm_business_opportunity;
DROP TABLE IF EXISTS crm_opportunity_stage;
DROP TABLE IF EXISTS crm_product;
DROP TABLE IF EXISTS crm_contact;
DROP TABLE IF EXISTS crm_customer;
DROP TABLE IF EXISTS crm_lead_source;
DROP TABLE IF EXISTS crm_customer_level;
DROP TABLE IF EXISTS sys_user;
-- 表删除完成后立即恢复外键一致性检查。
SET FOREIGN_KEY_CHECKS=1;

-- ----------------------------------------------------------------------------
-- 1. 系统用户表：存储登录账号和 admin/sales_manager/sales 三种角色。
-- status=1 表示正常，status=0 表示逻辑删除/停用；UNIQUE 保证用户名不重复。
-- ----------------------------------------------------------------------------
CREATE TABLE sys_user (
 id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE,
 password VARCHAR(100) NOT NULL, real_name VARCHAR(50) NOT NULL,
 phone VARCHAR(20), email VARCHAR(100), role VARCHAR(30) NOT NULL DEFAULT 'sales',
 status INT NOT NULL DEFAULT 1, create_time DATETIME DEFAULT NOW(), update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 客户等级字典：保存 VIP、重点、普通和潜在等级及显示顺序。
CREATE TABLE crm_customer_level (
 id INT PRIMARY KEY AUTO_INCREMENT, level_name VARCHAR(50) NOT NULL,
 level_code VARCHAR(20) NOT NULL UNIQUE, sort_order INT DEFAULT 0,
 description VARCHAR(200), create_time DATETIME DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 客户来源字典：定义官网、转介绍、展会等获客渠道。
CREATE TABLE crm_lead_source (
 id INT PRIMARY KEY AUTO_INCREMENT, source_name VARCHAR(50) NOT NULL,
 source_code VARCHAR(30) NOT NULL UNIQUE, description VARCHAR(200),
 create_time DATETIME DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 客户主表：customer_no 是页面展示的业务编号，id 只用于数据库关联。
-- owner_user_id 决定销售员数据权限；last_follow_time 用于计算 30 天未跟进预警。
CREATE TABLE crm_customer (
 id INT PRIMARY KEY AUTO_INCREMENT, customer_no VARCHAR(30) NOT NULL UNIQUE,
 customer_name VARCHAR(100) NOT NULL, industry VARCHAR(50), scale VARCHAR(30),
 province VARCHAR(30), city VARCHAR(30), address VARCHAR(200), website VARCHAR(100),
 level_id INT, source_id INT, owner_user_id INT, status INT NOT NULL DEFAULT 1,
 credit_rating VARCHAR(10) DEFAULT 'B', description TEXT, last_follow_time DATETIME,
 create_time DATETIME DEFAULT NOW(), update_time DATETIME,
 FOREIGN KEY(level_id) REFERENCES crm_customer_level(id),
 FOREIGN KEY(source_id) REFERENCES crm_lead_source(id), FOREIGN KEY(owner_user_id) REFERENCES sys_user(id),
 INDEX idx_customer_owner_status(owner_user_id,status), INDEX idx_customer_name(customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 联系人表：通过 customer_id 属于某个客户，可标记主要联系人和决策人。
CREATE TABLE crm_contact (
 id INT PRIMARY KEY AUTO_INCREMENT, customer_id INT NOT NULL, name VARCHAR(50) NOT NULL,
 gender VARCHAR(5), position VARCHAR(50), phone VARCHAR(30), email VARCHAR(100), wechat VARCHAR(50),
 is_primary INT DEFAULT 0, is_decision_maker INT DEFAULT 0, hobby VARCHAR(200), remarks TEXT,
 status INT NOT NULL DEFAULT 1, create_time DATETIME DEFAULT NOW(), update_time DATETIME,
 FOREIGN KEY(customer_id) REFERENCES crm_customer(id), INDEX idx_contact_customer(customer_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 产品目录：DECIMAL 使单价保持精确的两位小数，status 用于逻辑删除。
CREATE TABLE crm_product (
 id INT PRIMARY KEY AUTO_INCREMENT, product_name VARCHAR(100) NOT NULL, category VARCHAR(50),
 unit VARCHAR(20) DEFAULT '套', unit_price DECIMAL(10,2) DEFAULT 0, description TEXT,
 status INT NOT NULL DEFAULT 1, create_time DATETIME DEFAULT NOW(), update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 商机阶段字典：sort_order 控制漏斗顺序，win_probability 是该阶段的默认成交概率。
CREATE TABLE crm_opportunity_stage (
 id INT PRIMARY KEY AUTO_INCREMENT, stage_name VARCHAR(50) NOT NULL,
 stage_code VARCHAR(30) NOT NULL UNIQUE, sort_order INT NOT NULL,
 win_probability INT DEFAULT 0, description VARCHAR(200), create_time DATETIME DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 商机主表：business_status 保存进行中/已成交/已丢单，status 单独保存逻辑删除状态。
-- 负责人+状态索引加速销售员列表，客户索引加速客户详情中的关联查询。
CREATE TABLE crm_business_opportunity (
 id INT PRIMARY KEY AUTO_INCREMENT, opportunity_no VARCHAR(30) NOT NULL UNIQUE,
 title VARCHAR(200) NOT NULL, customer_id INT NOT NULL, contact_id INT, stage_id INT NOT NULL,
 expected_amount DECIMAL(12,2) DEFAULT 0, estimated_close_date DATE, owner_user_id INT,
 probability INT DEFAULT 0, description TEXT, result_reason TEXT,
 business_status VARCHAR(20) NOT NULL DEFAULT '进行中', status INT NOT NULL DEFAULT 1,
 create_time DATETIME DEFAULT NOW(), update_time DATETIME,
 FOREIGN KEY(customer_id) REFERENCES crm_customer(id), FOREIGN KEY(contact_id) REFERENCES crm_contact(id),
 FOREIGN KEY(stage_id) REFERENCES crm_opportunity_stage(id), FOREIGN KEY(owner_user_id) REFERENCES sys_user(id),
 INDEX idx_opp_owner_status(owner_user_id,status), INDEX idx_opp_customer(customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 商机产品明细：每行保存数量、成交单价和小计，是商机的“从表”。
-- product_name 冗余保存可在产品后续改名时仍保留当时快照。
CREATE TABLE crm_opportunity_product (
 id INT PRIMARY KEY AUTO_INCREMENT, opportunity_id INT NOT NULL, product_id INT NOT NULL,
 product_name VARCHAR(100), quantity INT NOT NULL DEFAULT 1, unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
 subtotal DECIMAL(12,2) DEFAULT 0, status INT NOT NULL DEFAULT 1, create_time DATETIME DEFAULT NOW(),
 FOREIGN KEY(opportunity_id) REFERENCES crm_business_opportunity(id), FOREIGN KEY(product_id) REFERENCES crm_product(id),
 INDEX idx_opp_item(opportunity_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 跟进记录：必须属于客户，商机和联系人可选，next_follow_time 用于今日/逾期提醒。
CREATE TABLE crm_follow_up_record (
 id INT PRIMARY KEY AUTO_INCREMENT, customer_id INT NOT NULL, opportunity_id INT, contact_id INT,
 follow_type VARCHAR(30) NOT NULL, follow_content TEXT NOT NULL, customer_feedback TEXT,
 next_plan TEXT, next_follow_time DATETIME, follow_user_id INT, follow_time DATETIME NOT NULL,
 is_reminded INT DEFAULT 0, status INT NOT NULL DEFAULT 1, create_time DATETIME DEFAULT NOW(),
 FOREIGN KEY(customer_id) REFERENCES crm_customer(id), FOREIGN KEY(opportunity_id) REFERENCES crm_business_opportunity(id),
 FOREIGN KEY(contact_id) REFERENCES crm_contact(id), FOREIGN KEY(follow_user_id) REFERENCES sys_user(id),
 INDEX idx_follow_customer(customer_id,status), INDEX idx_follow_next(next_follow_time,is_reminded)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 合同主表：business_status 保存执行中/已到期/已终止，status 保存逻辑状态。
-- opportunity_id 可为空，因为系统也允许不经商机直接录入历史合同。
CREATE TABLE crm_contract (
 id INT PRIMARY KEY AUTO_INCREMENT, contract_no VARCHAR(30) NOT NULL UNIQUE,
 contract_name VARCHAR(200) NOT NULL, opportunity_id INT, customer_id INT NOT NULL,
 contract_amount DECIMAL(12,2) NOT NULL DEFAULT 0, signed_date DATE, start_date DATE, end_date DATE,
 payment_terms VARCHAR(200), business_status VARCHAR(20) NOT NULL DEFAULT '执行中',
 attachment_path VARCHAR(300), create_user_id INT, remarks TEXT, status INT NOT NULL DEFAULT 1,
 create_time DATETIME DEFAULT NOW(), update_time DATETIME,
 FOREIGN KEY(opportunity_id) REFERENCES crm_business_opportunity(id), FOREIGN KEY(customer_id) REFERENCES crm_customer(id),
 FOREIGN KEY(create_user_id) REFERENCES sys_user(id), INDEX idx_contract_opportunity(opportunity_id,status),
 INDEX idx_contract_customer(customer_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. 合同产品明细：与商机明细结构对应，用于计算合同总额。
CREATE TABLE crm_contract_product (
 id INT PRIMARY KEY AUTO_INCREMENT, contract_id INT NOT NULL, product_id INT NOT NULL,
 product_name VARCHAR(100), quantity INT NOT NULL DEFAULT 1, unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
 subtotal DECIMAL(12,2) DEFAULT 0, status INT NOT NULL DEFAULT 1, create_time DATETIME DEFAULT NOW(),
 FOREIGN KEY(contract_id) REFERENCES crm_contract(id), FOREIGN KEY(product_id) REFERENCES crm_product(id),
 INDEX idx_contract_item(contract_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. 客户转移审计日志：记录原负责人、新负责人、时间和原因，不提供删除入口。
CREATE TABLE crm_customer_transfer_log (
 id INT PRIMARY KEY AUTO_INCREMENT, customer_id INT NOT NULL, from_user_id INT, to_user_id INT NOT NULL,
 transfer_time DATETIME DEFAULT NOW(), reason VARCHAR(200),
 FOREIGN KEY(customer_id) REFERENCES crm_customer(id), FOREIGN KEY(from_user_id) REFERENCES sys_user(id),
 FOREIGN KEY(to_user_id) REFERENCES sys_user(id), INDEX idx_transfer_customer(customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 基础演示数据：账号、字典和产品
-- 演示账号密码统一为 123456，方便本地验收；真实系统应使用强哈希存储密码。
-- ============================================================================
INSERT INTO sys_user(username,password,real_name,phone,email,role) VALUES
('admin','123456','系统管理员','13800000001','admin@example.com','admin'),
('manager','123456','销售经理','13800000002','manager@example.com','sales_manager'),
('sales','123456','销售专员','13800000003','sales@example.com','sales'),
('zhangsan','123456','张三','13800000004','zhangsan@example.com','sales_manager'),
('lisi','123456','李四','13800000005','lisi@example.com','sales');

-- 客户等级演示字典。
INSERT INTO crm_customer_level(level_name,level_code,sort_order,description) VALUES
('VIP客户','VIP',1,'最高价值客户'),('重点客户','KEY',2,'重点维护客户'),
('普通客户','NORMAL',3,'稳定合作客户'),('潜在客户','POTENTIAL',4,'有待持续培育');
-- 客户来源演示字典。
INSERT INTO crm_lead_source(source_name,source_code,description) VALUES
('官网咨询','WEBSITE','官网表单'),('转介绍','REFERRAL','客户转介绍'),('展会活动','EXHIBITION','行业展会'),
('广告投放','ADS','广告渠道'),('电话营销','CALL','电话营销'),('社交媒体','SOCIAL','社交媒体'),('其他','OTHER','其他渠道');
-- 完整销售漏斗阶段，包括成交和丢单终态。
INSERT INTO crm_opportunity_stage(stage_name,stage_code,sort_order,win_probability) VALUES
('线索获取','LEAD',1,10),('初步接洽','CONTACT',2,25),('需求分析','REQUIREMENT',3,40),
('方案报价','PROPOSAL',4,60),('商务谈判','NEGOTIATION',5,80),('成交','WON',6,100),('丢单','LOST',7,0);
-- 五种不同类别与计价单位的演示产品。
INSERT INTO crm_product(product_name,category,unit,unit_price,description) VALUES
('企业 CRM 标准版','软件','套',36800,'适合中小型销售团队'),('客户数据驾驶舱','软件','套',19800,'可视化数据分析'),
('销售流程咨询','咨询','次',8800,'销售流程梳理服务'),('年度运维服务','服务','年',12000,'系统运维与升级'),
('智能工牌终端','硬件','台',2600,'线下客户接待终端');

-- ============================================================================
-- 业务演示数据：覆盖正常、30+天未跟进、今日/逾期提醒、各阶段商机及合同状态。
-- NOW()-INTERVAL 创建相对当前日期的数据，因此无论何时导入都能演示预警与提醒。
-- ============================================================================
-- 四家演示客户分属两个销售员，用于验证数据隔离。
INSERT INTO crm_customer(customer_no,customer_name,industry,scale,province,city,address,website,level_id,source_id,owner_user_id,credit_rating,description,last_follow_time,create_time) VALUES
('KH202607140001','星海科技有限公司','软件与信息服务','500-1000人','上海市','上海市','浦东新区创新路88号','https://example.com',1,1,3,'A','重点数字化客户',NOW()-INTERVAL 2 DAY,NOW()-INTERVAL 60 DAY),
('KH202607140002','远峰智能制造集团','智能制造','1000人以上','江苏省','苏州市','工业园区未来路6号','https://example.com',2,2,3,'A','智能工厂升级项目',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 90 DAY),
('KH202607140003','云帆教育服务有限公司','教育','100-500人','浙江省','杭州市','滨江区育才路18号','https://example.com',3,3,5,'B','在线教育客户',NOW()-INTERVAL 12 DAY,NOW()-INTERVAL 40 DAY),
('KH202607140004','青禾商业管理有限公司','零售','50-100人','广东省','深圳市','南山区商业街9号','https://example.com',4,6,5,'B','等待首次沟通',NULL,NOW()-INTERVAL 8 DAY);
-- 每家客户的主要决策联系人。
INSERT INTO crm_contact(customer_id,name,gender,position,phone,email,wechat,is_primary,is_decision_maker,hobby,remarks) VALUES
(1,'王珊','女','信息化总监','13910000001','wangshan@example.com','wangshan_crm',1,1,'阅读','关注交付能力'),
(2,'赵峰','男','采购经理','13910000002','zhaofeng@example.com','zhaofeng88',1,1,'跑步','预算已获批'),
(3,'陈晓','女','运营总监','13910000003','chenxiao@example.com','chenxiao',1,1,'旅行','重视易用性'),
(4,'林青','男','总经理','13910000004','linqing@example.com','linqing',1,1,'摄影','首次接洽');
-- 分布在多个漏斗阶段的商机，其中一条已成交可用于生成合同。
INSERT INTO crm_business_opportunity(opportunity_no,title,customer_id,contact_id,stage_id,expected_amount,estimated_close_date,owner_user_id,probability,description,business_status,create_time) VALUES
('CRM202607140001','星海 CRM 升级项目',1,1,5,57600,CURDATE()+INTERVAL 15 DAY,3,80,'标准版与年度运维','进行中',NOW()-INTERVAL 35 DAY),
('CRM202607140002','远峰智能驾驶舱项目',2,2,4,48400,CURDATE()+INTERVAL 25 DAY,3,60,'驾驶舱与咨询','进行中',NOW()-INTERVAL 28 DAY),
('CRM202607140003','云帆销售流程数字化',3,3,3,36800,CURDATE()+INTERVAL 45 DAY,5,40,'CRM 标准版','进行中',NOW()-INTERVAL 20 DAY),
('CRM202607140004','青禾客户经营平台',4,4,2,19800,CURDATE()+INTERVAL 60 DAY,5,25,'客户数据驾驶舱','进行中',NOW()-INTERVAL 7 DAY),
('CRM202607140005','星海二期服务合同',1,1,6,12000,CURDATE()-INTERVAL 10 DAY,3,100,'年度服务','已成交',NOW()-INTERVAL 80 DAY),
('CRM202607140006','远峰终端采购',2,2,1,52000,CURDATE()+INTERVAL 90 DAY,3,10,'智能终端','进行中',NOW()-INTERVAL 3 DAY);
-- 商机产品明细，小计等于数量乘以单价。
INSERT INTO crm_opportunity_product(opportunity_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
(1,1,'企业 CRM 标准版',1,36800,36800),(1,4,'年度运维服务',1,12000,12000),(1,3,'销售流程咨询',1,8800,8800),
(2,2,'客户数据驾驶舱',2,19800,39600),(2,3,'销售流程咨询',1,8800,8800),(3,1,'企业 CRM 标准版',1,36800,36800),
(4,2,'客户数据驾驶舱',1,19800,19800),(5,4,'年度运维服务',1,12000,12000),(6,5,'智能工牌终端',20,2600,52000);
-- 跟进数据中既有今日提醒，也有逾期提醒和未来计划。
INSERT INTO crm_follow_up_record(customer_id,opportunity_id,contact_id,follow_type,follow_content,customer_feedback,next_plan,next_follow_time,follow_user_id,follow_time) VALUES
(1,1,1,'线上会议','确认最终实施范围与排期','认可方案，等待商务条款','发送正式合同草案',CONCAT(CURDATE(),' 15:00:00'),3,NOW()-INTERVAL 2 DAY),
(2,2,2,'拜访','完成驾驶舱原型演示','希望增加生产指标','补充报价与技术清单',NOW()-INTERVAL 1 DAY,3,NOW()-INTERVAL 36 DAY),
(3,3,3,'电话','了解销售团队规模和流程','需要内部讨论','下周安排产品演示',NOW()+INTERVAL 3 DAY,5,NOW()-INTERVAL 12 DAY),
(4,4,4,'邮件','发送产品介绍与案例','已收件','电话确认需求',CONCAT(CURDATE(),' 10:30:00'),5,NOW()-INTERVAL 7 DAY);
-- 一份执行中且即将到期的合同，以及一份已到期历史合同。
INSERT INTO crm_contract(contract_no,contract_name,opportunity_id,customer_id,contract_amount,signed_date,start_date,end_date,payment_terms,business_status,create_user_id,remarks) VALUES
('HT202607140001','星海年度运维服务合同',5,1,12000,CURDATE()-INTERVAL 10 DAY,CURDATE()-INTERVAL 10 DAY,CURDATE()+INTERVAL 20 DAY,'签约后一次性支付','执行中',3,'即将到期提醒'),
('HT202607140002','云帆咨询服务历史合同',NULL,3,8800,CURDATE()-INTERVAL 400 DAY,CURDATE()-INTERVAL 400 DAY,CURDATE()-INTERVAL 35 DAY,'一次性支付','已到期',5,'历史合同');
-- 两份合同对应的产品快照明细。
INSERT INTO crm_contract_product(contract_id,product_id,product_name,quantity,unit_price,subtotal) VALUES
(1,4,'年度运维服务',1,12000,12000),(2,3,'销售流程咨询',1,8800,8800);
