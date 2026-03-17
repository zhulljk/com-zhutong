-- Doris 数据库表结构
-- 注意：Doris 不支持 AUTO_INCREMENT，需要使用 AGGREGATE KEY 和 MAX 函数实现自增效果
-- 或者在插入时指定 ID 值

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '用户 ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `status` TINYINT DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    `register_source` VARCHAR(20) DEFAULT 'LOCAL' COMMENT '注册来源：LOCAL-本地，SSO-单点登录，OAUTH2-第三方登录',
    `oauth_provider_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方登录提供商 ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间'
) ENGINE=OLAP
UNIQUE KEY (`username`)
DISTRIBUTED BY HASH(`username`) BUCKETS 1
PROPERTIES (
    "replication_num" = "1",
    "enable_unique_key_merge_on_write" = "true"
);

-- 插入测试用户（密码为 123456 的 BCrypt 加密）
INSERT INTO `user` (`id`, `username`, `password`, `email`, `nickname`, `status`, `register_source`, `create_time`, `update_time`) 
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJ1fXfS7GqQbXqJhL9K5Z8X9Z8X', 'admin@zhut.com', '管理员', 1, 'LOCAL', NOW(), NOW());