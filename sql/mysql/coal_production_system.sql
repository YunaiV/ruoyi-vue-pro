-- ========================================
-- 选煤厂生产管理系统数据库脚本
-- 基于芋道开源框架标准
-- 数据库：MySQL 8.0+
-- ========================================

-- ----------------------------
-- 1. 生产计划表（树表结构）
-- ----------------------------
DROP TABLE IF EXISTS `coal_production_plan`;
CREATE TABLE `coal_production_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    `name` VARCHAR(100) NOT NULL COMMENT '计划名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父计划ID',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `plan_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '计划编号',
    `plan_type` TINYINT NOT NULL COMMENT '计划类型：1年度 2月度 3日计划 4班计划',
    `plan_year` INT NULL COMMENT '计划年度',
    `plan_month` TINYINT NULL COMMENT '计划月份',
    `plan_date` DATE NULL COMMENT '计划日期',
    `shift_id` BIGINT NULL COMMENT '班次ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '计划状态：0停用 1正常',
    
    -- 计划产量数据（基础）
    `raw_coal_plan` DECIMAL(10,2) NULL COMMENT '计划入洗原煤量(吨)',
    
    -- 精煤产量计划（按粒度分类）
    `fine_coal_plan` DECIMAL(10,2) NULL COMMENT '计划末煤产量(吨)',
    `granular_coal_plan` DECIMAL(10,2) NULL COMMENT '计划粒煤产量(吨)',
    `large_block_coal_plan` DECIMAL(10,2) NULL COMMENT '计划大块煤产量(吨)',
    `medium_block_coal_plan` DECIMAL(10,2) NULL COMMENT '计划中块煤产量(吨)',
    `small_block_coal_plan` DECIMAL(10,2) NULL COMMENT '计划小块煤产量(吨)',
    
    -- 其他产品计划产量
    `middling_coal_plan` DECIMAL(10,2) NULL COMMENT '计划中煤产量(吨)',
    `slime_plan` DECIMAL(10,2) NULL COMMENT '计划煤泥产量(吨)',
    `gangue_plan` DECIMAL(10,2) NULL COMMENT '计划矸石产量(吨)',
    
    -- 预留计划产量字段
    `reserved_product_plan1` DECIMAL(10,2) NULL COMMENT '预留计划产量字段1(吨)',
    `reserved_product_plan2` DECIMAL(10,2) NULL COMMENT '预留计划产量字段2(吨)',
    
    -- 质量指标
    `fine_coal_ash` DECIMAL(5,2) NULL COMMENT '末煤灰分(%)',
    `granular_coal_ash` DECIMAL(5,2) NULL COMMENT '粒煤灰分(%)',
    `large_block_coal_ash` DECIMAL(5,2) NULL COMMENT '大块煤灰分(%)',
    `medium_block_coal_ash` DECIMAL(5,2) NULL COMMENT '中块煤灰分(%)',
    `small_block_coal_ash` DECIMAL(5,2) NULL COMMENT '小块煤灰分(%)',
    `middling_coal_ash` DECIMAL(5,2) NULL COMMENT '中煤灰分(%)',
    
    -- 审批信息
    `creator_id` BIGINT NULL COMMENT '制定人ID',
    `approver_id` BIGINT NULL COMMENT '审批人ID',
    `approve_time` DATETIME NULL COMMENT '审批时间',
    
    -- 基础字段（芋道框架标准）
    `creator` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '生产计划表（树表结构）';

-- ----------------------------
-- 2. 现场生产日报表
-- ----------------------------
DROP TABLE IF EXISTS `coal_production_daily_report`;
CREATE TABLE `coal_production_daily_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日报ID',
    `report_date` DATE NOT NULL COMMENT '日期',
    `shift_id` BIGINT NOT NULL COMMENT '班次ID',

    -- 人员信息
    `operator_id` BIGINT NULL COMMENT '集控员（操作人）ID',
    `shift_leader_id` BIGINT NULL COMMENT '带班主任/班长ID',

    -- 时间记录
    `start_time` TIME NULL COMMENT '启车时间',
    `coal_feeding_time` INT NULL COMMENT '带煤时间(分钟)',
    `stop_time` TIME NULL COMMENT '停车时间',
    `effective_feeding_time` INT NULL COMMENT '有效带煤时间(分钟)',
    `fault_impact_time` INT NULL COMMENT '故障影响时间(分钟)',

    -- 产量数据
    `raw_coal_input` DECIMAL(10,2) NULL COMMENT '入洗煤量(吨)',
    `hourly_capacity` DECIMAL(8,2) NULL COMMENT '小时处理量(吨/小时)',
    `block_clean_coal_output` DECIMAL(10,2) NULL COMMENT '块精煤产量(吨)',
    `fine_clean_coal_output` DECIMAL(10,2) NULL COMMENT '末精煤产量(吨)',
    `gangue_output` DECIMAL(10,2) NULL COMMENT '矸石产量(吨)',
    `middling_coal_output` DECIMAL(10,2) NULL COMMENT '中煤产量(吨)',

    -- 压滤相关
    `filter_press_cycles` INT NULL COMMENT '压滤板次',
    `filter_press_coal_amount` DECIMAL(8,2) NULL COMMENT '压滤煤量(吨)',
    `filter_cloth_usage` INT NULL COMMENT '滤布使用量(张)',

    -- 放舱记录
    `discharge_record` VARCHAR(500) NULL COMMENT '放舱记录',

    -- 介质添加
    `baffle_medium_addition` DECIMAL(8,2) NULL COMMENT '挡板添加介质量(kg)',
    `cao_amount` DECIMAL(8,2) NULL COMMENT 'CaO量(kg)',
    `flocculant_amount` DECIMAL(8,2) NULL COMMENT '絮凝剂(kg)',
    `density_317` DECIMAL(5,2) NULL COMMENT '317密度(kg/L)',

    -- 第一次灰分检测内容
    `first_ash_block_clean` DECIMAL(5,2) NULL COMMENT '第一次块精煤灰分(%)',
    `first_ash_fine_clean` DECIMAL(5,2) NULL COMMENT '第一次末精煤灰分(%)',
    `first_ash_middling` DECIMAL(5,2) NULL COMMENT '第一次中煤灰分(%)',
    `first_ash_slime` DECIMAL(5,2) NULL COMMENT '第一次煤泥灰分(%)',
    `first_ash_gangue` DECIMAL(5,2) NULL COMMENT '第一次矸石灰分(%)',

    -- 第二次灰分检测内容
    `second_ash_block_clean` DECIMAL(5,2) NULL COMMENT '第二次块精煤灰分(%)',
    `second_ash_fine_clean` DECIMAL(5,2) NULL COMMENT '第二次末精煤灰分(%)',
    `second_ash_middling` DECIMAL(5,2) NULL COMMENT '第二次中煤灰分(%)',
    `second_ash_slime` DECIMAL(5,2) NULL COMMENT '第二次煤泥灰分(%)',
    `second_ash_gangue` DECIMAL(5,2) NULL COMMENT '第二次矸石灰分(%)',

    -- 影响时间记录
    `impact_time_record` TEXT NULL COMMENT '影响时间记录详情',

    -- 交办事项
    `assigned_tasks` TEXT NULL COMMENT '交办事项',

    -- 启车时的仓位和液位
    `start_circulating_water_pool` DECIMAL(6,2) NULL COMMENT '启车循环水池液位',
    `start_clean_water_tank` DECIMAL(6,2) NULL COMMENT '启车清水桶液位',
    `start_fine_coal_level` DECIMAL(6,2) NULL COMMENT '启车末煤仓位',
    `start_granular_coal_level` DECIMAL(6,2) NULL COMMENT '启车粒煤仓位',
    `start_large_block_level` DECIMAL(6,2) NULL COMMENT '启车大块仓位',
    `start_medium_block_level` DECIMAL(6,2) NULL COMMENT '启车中块仓位',
    `start_small_block_level` DECIMAL(6,2) NULL COMMENT '启车小块仓位',
    `start_middling_coal_level` DECIMAL(6,2) NULL COMMENT '启车中煤仓位',
    `start_gangue_level` DECIMAL(6,2) NULL COMMENT '启车矸石仓位',

    -- 停车时的仓位和液位
    `stop_circulating_water_pool` DECIMAL(6,2) NULL COMMENT '停车循环水池液位',
    `stop_clean_water_tank` DECIMAL(6,2) NULL COMMENT '停车清水桶液位',
    `stop_fine_coal_level` DECIMAL(6,2) NULL COMMENT '停车末煤仓位',
    `stop_granular_coal_level` DECIMAL(6,2) NULL COMMENT '停车粒煤仓位',
    `stop_large_block_level` DECIMAL(6,2) NULL COMMENT '停车大块仓位',
    `stop_medium_block_level` DECIMAL(6,2) NULL COMMENT '停车中块仓位',
    `stop_small_block_level` DECIMAL(6,2) NULL COMMENT '停车小块仓位',
    `stop_middling_coal_level` DECIMAL(6,2) NULL COMMENT '停车中煤仓位',
    `stop_gangue_level` DECIMAL(6,2) NULL COMMENT '停车矸石仓位',

    -- 备注
    `remarks` TEXT NULL COMMENT '备注信息',

    -- 预留字段（用于后续扩展新字段）
    `reserved_field1` VARCHAR(200) NULL COMMENT '预留字段1',
    `reserved_field2` VARCHAR(200) NULL COMMENT '预留字段2',
    `reserved_field3` DECIMAL(10,2) NULL COMMENT '预留字段3',
    `reserved_field4` DECIMAL(10,2) NULL COMMENT '预留字段4',
    `reserved_field5` TEXT NULL COMMENT '预留字段5',

    -- 基础字段（芋道框架标准）
    `creator` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_report_date` (`report_date`) USING BTREE,
    INDEX `idx_shift_id` (`shift_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '现场生产日报表';

-- ----------------------------
-- 3. 班次配置表
-- ----------------------------
DROP TABLE IF EXISTS `coal_shift_config`;
CREATE TABLE `coal_shift_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '班次ID',
    `name` VARCHAR(50) NOT NULL COMMENT '班次名称',
    `code` VARCHAR(20) NOT NULL COMMENT '班次编码',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `shift_type` TINYINT NOT NULL COMMENT '班次类型：1生产班 2检修班 3值班',
    `is_production` BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否生产班',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    
    -- 基础字段（芋道框架标准）
    `creator` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_code` (`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班次配置表';

-- ----------------------------
-- 初始化班次数据
-- ----------------------------
INSERT INTO `coal_shift_config` (`name`, `code`, `start_time`, `end_time`, `shift_type`, `is_production`, `sort`, `status`, `creator`) VALUES
('早班', 'MORNING', '08:00:00', '16:00:00', 1, b'1', 1, 1, 'system'),
('中班', 'AFTERNOON', '16:00:00', '00:00:00', 1, b'1', 2, 1, 'system'),
('夜班', 'NIGHT', '00:00:00', '08:00:00', 1, b'1', 3, 1, 'system');
