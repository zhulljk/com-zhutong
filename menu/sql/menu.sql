-- 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
    `id` BIGINT NOT NULL COMMENT '菜单 ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单 ID（顶级菜单为 0）',
    `name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '菜单图标',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单类型：0-目录，1-菜单，2-按钮',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单状态：0-禁用，1-正常',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';
