-- ============================================================
-- Deepay 全量数据库初始化脚本（合并版）
-- 覆盖范围：Phase 3 → Phase 12（v3 / v4-v5 / v6 / v8 / v9 / v10 / v11 / v12）
-- 执行环境：MySQL 5.7 / 8.0
-- 说明：全部列已写入 CREATE TABLE，无需任何 ALTER TABLE，幂等安全
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. deepay_style_chain — 设计链（核心流转单元）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_style_chain` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`       VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '6位随机大写链码，全局唯一',
    `image_url`        VARCHAR(512)  NOT NULL DEFAULT ''     COMMENT '最终选中的设计图片URL',
    `status`           VARCHAR(32)   NOT NULL DEFAULT ''     COMMENT '状态：CREATED / PUBLISHED / SOLD',
    `keyword`          VARCHAR(256)           DEFAULT NULL   COMMENT '用户输入关键词',
    `selected_image`   VARCHAR(512)           DEFAULT NULL   COMMENT 'AI 决策选中的图片',
    `pattern_file`     VARCHAR(512)           DEFAULT NULL   COMMENT '打版文件路径（.dxf）',
    `decision_reason`  VARCHAR(1024)          DEFAULT NULL   COMMENT 'AI 决策原因（可解释）',
    `context_snapshot` LONGTEXT               DEFAULT NULL   COMMENT '决策快照 JSON，每个 Agent 执行后更新',
    `ima_kb_id`        VARCHAR(128)           DEFAULT NULL   COMMENT 'ima 知识库 ID，同步失败时为 NULL',
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`) COMMENT '链码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 样式链码表（设计链核心）';

-- ============================================================
-- 2. deepay_product — 商品表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_product` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`    VARCHAR(64)    NOT NULL DEFAULT ''     COMMENT '关联链码',
    `title`         VARCHAR(512)   NOT NULL DEFAULT ''     COMMENT '商品标题',
    `description`   VARCHAR(2048)           DEFAULT NULL   COMMENT '商品描述',
    `price`         DECIMAL(10,2)  NOT NULL DEFAULT 0      COMMENT '售价（元）',
    `status`        VARCHAR(32)    NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / SELLING / STOPPED / REDESIGNING',
    `sold_count`    INT            NOT NULL DEFAULT 0      COMMENT '累计销量',
    `stock`         INT            NOT NULL DEFAULT 0      COMMENT '可用库存',
    `cost_price`    DECIMAL(10,2)           DEFAULT NULL   COMMENT '生产成本（元），用于利润计算',
    `cdn_image_url` VARCHAR(512)            DEFAULT NULL   COMMENT '商品主图 CDN 地址',
    `category`      VARCHAR(64)             DEFAULT NULL   COMMENT '商品品类（外套/内裤/裤子/上衣/连衣裙等）',
    `style`         VARCHAR(64)             DEFAULT NULL   COMMENT '风格标签（SEXY/MINIMAL/CASUAL/SPORT/LUXURY）',
    `created_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`) COMMENT '链码唯一索引',
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 商品表';

-- ============================================================
-- 3. deepay_inventory — 库存表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_inventory` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`   VARCHAR(64) NOT NULL DEFAULT ''     COMMENT '关联链码',
    `stock`        INT         NOT NULL DEFAULT 0      COMMENT '可用库存',
    `locked_stock` INT         NOT NULL DEFAULT 0      COMMENT '锁定库存（下单未支付）',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 库存表';

-- ============================================================
-- 4. deepay_metrics — 销售指标快照表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_metrics` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`      VARCHAR(64)   NOT NULL               COMMENT '关联链码',
    `sold_count`      INT           NOT NULL DEFAULT 0     COMMENT '销量快照',
    `view_count`      INT                    DEFAULT 0     COMMENT '商品详情页浏览次数',
    `pay_count`       INT                    DEFAULT 0     COMMENT '成功支付次数',
    `conversion_rate` DECIMAL(6,4)           DEFAULT 0     COMMENT '转化率 = pay_count / view_count',
    `cost_price`      DECIMAL(10,2)          DEFAULT NULL  COMMENT '成本快照',
    `profit`          DECIMAL(10,2)          DEFAULT NULL  COMMENT '单笔利润 = price - cost',
    `roi`             DECIMAL(10,4)          DEFAULT NULL  COMMENT 'ROI = profit / cost',
    `price`           DECIMAL(10,2)          DEFAULT NULL  COMMENT '价格快照',
    `category`        VARCHAR(256)           DEFAULT NULL  COMMENT '分类 / 复盘 action',
    `style`           VARCHAR(32)            DEFAULT NULL  COMMENT '风格标签',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 销售指标快照表';

-- ============================================================
-- 5. deepay_order — 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_order` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `payment_id` VARCHAR(128)  NOT NULL               COMMENT '唯一支付 ID（PAY-{chainCode}-{ts}）',
    `chain_code` VARCHAR(64)   NOT NULL               COMMENT '关联链码',
    `user_id`    BIGINT                 DEFAULT NULL  COMMENT '下单用户 ID（B2B 场景）',
    `status`     VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / PAID / CANCELLED',
    `amount`     DECIMAL(10,2) NOT NULL DEFAULT 0     COMMENT '实收金额',
    `paid_at`    DATETIME               DEFAULT NULL  COMMENT '实际支付时间',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_payment_id` (`payment_id`),
    UNIQUE KEY `uk_chain_code_user_id` (`chain_code`, `user_id`),
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 订单表';

-- ============================================================
-- 6. deepay_design_version — 设计版本历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_design_version` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(64)           DEFAULT NULL  COMMENT '关联链码（JudgeAgent 运行时可能尚无 chainCode）',
    `image_url`  VARCHAR(512) NOT NULL               COMMENT '设计图 URL',
    `version`    INT          NOT NULL DEFAULT 1     COMMENT '版本号（每次 REDESIGN +1）',
    `selected`   TINYINT(1)   NOT NULL DEFAULT 0     COMMENT '是否被 AIDecisionAgent 选中',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 设计版本历史表（可追溯）';

-- ============================================================
-- 7. deepay_production — 生产记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_production` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`   VARCHAR(64)  NOT NULL               COMMENT '关联链码',
    `pattern_file` VARCHAR(512)          DEFAULT NULL  COMMENT '版型文件路径（.dxf）',
    `tech_pack`    VARCHAR(512)          DEFAULT NULL  COMMENT '技术包下载地址（PDF）',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 生产记录表（PatternAgent 落库）';

-- ============================================================
-- 8. deepay_log — 操作审计日志
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_log` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(32)          DEFAULT NULL  COMMENT '关联链码',
    `action`     VARCHAR(32) NOT NULL               COMMENT 'CREATE/PUBLISH/PAY/REPRICE/STOP/REDESIGN/REFUND/RESTOCK/RETRY',
    `before_val` TEXT                 DEFAULT NULL  COMMENT '操作前状态',
    `after_val`  TEXT                 DEFAULT NULL  COMMENT '操作后状态',
    `time`       DATETIME    NOT NULL               COMMENT '操作时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`),
    KEY `idx_action`     (`action`),
    KEY `idx_time`       (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 操作审计日志（不可变，永久保留）';

-- ============================================================
-- 9. deepay_retry_task — 失败任务重试队列
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_retry_task` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`    VARCHAR(32)            DEFAULT NULL  COMMENT '关联链码',
    `task_type`     VARCHAR(32)   NOT NULL               COMMENT 'AI_DESIGN/PAYMENT/PATTERN/PUBLISH',
    `status`        VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RETRYING/DONE/FAILED',
    `error_msg`     VARCHAR(2048)          DEFAULT NULL  COMMENT '失败原因摘要',
    `retry_count`   INT           NOT NULL DEFAULT 0     COMMENT '已重试次数',
    `max_retry`     INT           NOT NULL DEFAULT 3     COMMENT '最大重试次数',
    `next_retry_at` DATETIME               DEFAULT NULL  COMMENT '下次重试时间（退避策略）',
    `created_at`    DATETIME      NOT NULL               COMMENT '创建时间',
    `updated_at`    DATETIME      NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_status_next_retry` (`status`, `next_retry_at`),
    KEY `idx_chain_code`        (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 失败任务重试队列';

-- ============================================================
-- 10. deepay_client — B2B 客户表（分层：A/B/C）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_client` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`               VARCHAR(128)  NOT NULL               COMMENT '客户名称',
    `level`              CHAR(1)       NOT NULL DEFAULT 'B'   COMMENT '等级：A大客户 / B标准 / C限量',
    `total_order_amount` DECIMAL(14,2) NOT NULL DEFAULT 0     COMMENT '历史累计下单金额（元）',
    `contact`            VARCHAR(128)           DEFAULT NULL  COMMENT '联系方式',
    `remark`             VARCHAR(512)           DEFAULT NULL  COMMENT '备注',
    `created_at`         DATETIME      NOT NULL               COMMENT '创建时间',
    `updated_at`         DATETIME      NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay B2B 客户表（分层：A/B/C）';

-- ============================================================
-- 11. deepay_demand_forecast — 需求预测表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_demand_forecast` (
    `id`                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`              VARCHAR(32)           DEFAULT NULL  COMMENT '关联链码',
    `category`                VARCHAR(64)           DEFAULT NULL  COMMENT '品类关键词',
    `predicted_sales`         INT          NOT NULL               COMMENT '预测销量（件）',
    `confidence`              DECIMAL(5,2)          DEFAULT NULL  COMMENT '置信度 0~1',
    `forecast_days`           INT          NOT NULL DEFAULT 7     COMMENT '预测周期（天）',
    `season_factor`           DECIMAL(5,2)          DEFAULT NULL  COMMENT '季节因子',
    `suggested_production_qty` INT                  DEFAULT NULL  COMMENT '建议生产量',
    `created_at`              DATETIME     NOT NULL               COMMENT '预测时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`),
    KEY `idx_category`   (`category`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 需求预测表（DemandAgent 输出）';

-- ============================================================
-- 12. deepay_production_plan — 生产计划表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_production_plan` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`        VARCHAR(32)          DEFAULT NULL  COMMENT '关联链码',
    `planned_quantity`  INT         NOT NULL               COMMENT '计划生产量',
    `forecast_demand`   INT                  DEFAULT NULL  COMMENT '来自 DemandAgent 的预测需求',
    `current_stock`     INT                  DEFAULT NULL  COMMENT '当前库存快照',
    `in_transit_stock`  INT                  DEFAULT NULL  COMMENT '在途库存快照',
    `status`            VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/IN_PRODUCTION/COMPLETED/CANCELLED',
    `factory_id`        BIGINT               DEFAULT NULL  COMMENT '关联工厂 ID（可选）',
    `created_at`        DATETIME    NOT NULL               COMMENT '创建时间',
    `updated_at`        DATETIME    NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`),
    KEY `idx_status`     (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 生产计划表（ProductionPlanner 输出）';

-- ============================================================
-- 13. deepay_user_profile — 终端用户画像表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_user_profile` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`            VARCHAR(64)  NOT NULL               COMMENT '外部用户 ID（微信openid/平台UID等）',
    `category`           VARCHAR(64)           DEFAULT NULL  COMMENT '主营品类',
    `style_preference`   VARCHAR(64)           DEFAULT NULL  COMMENT '主要风格标签（SEXY/CASUAL/SPORT/MINIMAL/LUXURY）',
    `style_weights_json` TEXT                  DEFAULT NULL  COMMENT '风格权重 JSON，如 {"SEXY":0.8,"MINIMAL":0.3}',
    `gender`             VARCHAR(16)           DEFAULT NULL  COMMENT '目标性别：MALE/FEMALE/UNISEX',
    `age_group`          VARCHAR(32)           DEFAULT NULL  COMMENT '年龄段：YOUNG/MIDDLE/ELDER',
    `price_range`        VARCHAR(32)           DEFAULT NULL  COMMENT '价格带：LOW/MID/HIGH',
    `market`             VARCHAR(32)           DEFAULT NULL  COMMENT '目标市场：CN/EU/US/ME',
    `confidence`         DECIMAL(5,2) NOT NULL DEFAULT 0.00  COMMENT '画像置信度（0~1），≥0.6 跳过问卷',
    `created_at`         DATETIME     NOT NULL               COMMENT '创建时间',
    `updated_at`         DATETIME     NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_id`    (`user_id`),
    KEY        `idx_category`  (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 终端用户画像（MemoryAgent 读写，实现"记住用户偏好"）';

-- ============================================================
-- 14. deepay_customer_profile — B2B 客户画像表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_customer_profile` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `customer_id`      BIGINT       NOT NULL               COMMENT 'B2B 客户 ID（关联 deepay_client.id）',
    `category_level1`  VARCHAR(64)           DEFAULT NULL  COMMENT '一级品类',
    `category_level2`  VARCHAR(64)           DEFAULT NULL  COMMENT '二级品类',
    `style_weights`    TEXT                  DEFAULT NULL  COMMENT '风格权重 JSON',
    `price_level`      VARCHAR(16)           DEFAULT NULL  COMMENT '价格带：LOW/MID/HIGH',
    `market`           VARCHAR(16)           DEFAULT NULL  COMMENT '目标市场：CN/EU/US/ME',
    `target_age`       VARCHAR(16)           DEFAULT NULL  COMMENT '年龄段：YOUNG/MIDDLE/ELDER',
    `gender`           VARCHAR(16)           DEFAULT NULL  COMMENT '目标性别：MALE/FEMALE/UNISEX',
    `confidence_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00  COMMENT '画像置信度（0~1）',
    `created_at`       DATETIME     NOT NULL               COMMENT '创建时间',
    `updated_at`       DATETIME     NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_customer_id`       (`customer_id`),
    KEY        `idx_category_level1`  (`category_level1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay B2B 客户画像（CustomerProfileAgent 读写）';

-- ============================================================
-- 15. deepay_user_selection — 用户选图记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_user_selection` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        VARCHAR(64)  NOT NULL               COMMENT '外部用户 ID',
    `chain_code`     VARCHAR(64)           DEFAULT NULL  COMMENT '关联链码',
    `selected_image` VARCHAR(512)          DEFAULT NULL  COMMENT '用户选中的设计图 CDN 地址',
    `category`       VARCHAR(64)           DEFAULT NULL  COMMENT '本次选择品类',
    `style`          VARCHAR(64)           DEFAULT NULL  COMMENT '本次选择风格标签',
    `market`         VARCHAR(16)           DEFAULT NULL  COMMENT '本次目标市场（CN/EU/US/ME）',
    `score`          INT                   DEFAULT NULL  COMMENT '设计图评分（JudgeAgent 打分）',
    `created_at`     DATETIME     NOT NULL               COMMENT '选择时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user_id`       (`user_id`),
    KEY `idx_category`      (`category`),
    KEY `idx_user_category` (`user_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 用户选图记录（SelectionAgent 写入，TrendAgent 下次优先返回同类款式）';

-- ============================================================
-- 16. deepay_hot_product — 爆款识别结果表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_hot_product` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(64)  NOT NULL               COMMENT '原始商品链码（关联 deepay_product）',
    `category`   VARCHAR(64)           DEFAULT NULL  COMMENT '品类',
    `style`      VARCHAR(64)           DEFAULT NULL  COMMENT '风格标签',
    `image_url`  VARCHAR(512)          DEFAULT NULL  COMMENT '原款主图 CDN 地址',
    `sold_count` INT          NOT NULL DEFAULT 0     COMMENT '累计销量（识别爆款依据）',
    `hot_level`  VARCHAR(16)  NOT NULL DEFAULT 'HOT' COMMENT 'HOT（>=10）或 SUPER_HOT（>=50）',
    `score`      DOUBLE                DEFAULT NULL  COMMENT '爆款评分（sold_count / 50.0）',
    `created_at` DATETIME     NOT NULL               COMMENT '记录写入时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code`   (`chain_code`),
    KEY        `idx_category`    (`category`),
    KEY        `idx_hot_level`   (`hot_level`),
    KEY        `idx_sold_count`  (`sold_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 爆款识别结果表（HotCloneAgent 写入，DeepayHotCloneScheduler 定时扫描）';

-- ============================================================
-- 17. deepay_variant — 爆款变体表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_variant` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_chain_code` VARCHAR(64)   NOT NULL               COMMENT '爆款父链码（关联 deepay_hot_product）',
    `variant_code`      VARCHAR(64)   NOT NULL               COMMENT '变体唯一编码（父链码-V序号）',
    `category`          VARCHAR(64)            DEFAULT NULL  COMMENT '品类（与父款相同）',
    `color`             VARCHAR(32)            DEFAULT NULL  COMMENT '颜色变体（黑/白/灰/米白等）',
    `fabric`            VARCHAR(64)            DEFAULT NULL  COMMENT '面料变体（棉/牛仔/针织/羊毛等）',
    `fit`               VARCHAR(32)            DEFAULT NULL  COMMENT '版型变体（宽松/修身/直筒等）',
    `style`             VARCHAR(64)            DEFAULT NULL  COMMENT '风格标签',
    `image_url`         VARCHAR(512)           DEFAULT NULL  COMMENT '变体设计图 CDN 地址（FluxService 生成）',
    `design_prompt`     VARCHAR(1024)          DEFAULT NULL  COMMENT '生成本变体所用的 Prompt',
    `created_at`        DATETIME      NOT NULL               COMMENT '变体创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_variant_code` (`variant_code`),
    KEY        `idx_parent`      (`parent_chain_code`),
    KEY        `idx_category`    (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 爆款变体表（HotCloneAgent 生成，每款爆款最多衍生 15 个变体款）';

-- ============================================================
-- 18. deepay_trend_item — 外部趋势款选品表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_trend_item` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `image_url`  VARCHAR(512)           DEFAULT NULL  COMMENT '款式图片 URL（CDN 或外部链接）',
    `brand`      VARCHAR(64)            DEFAULT NULL  COMMENT '品牌名称',
    `category`   VARCHAR(64)            DEFAULT NULL  COMMENT '品类（外套/内裤/连衣裙/裤子/上衣等）',
    `style`      VARCHAR(64)            DEFAULT NULL  COMMENT '风格标签（SEXY/MINIMAL/CASUAL/SPORT/LUXURY）',
    `price`      DECIMAL(10,2)          DEFAULT NULL  COMMENT '参考售价（元）',
    `source`     VARCHAR(32)            DEFAULT NULL  COMMENT '数据来源（1688 / tiktok / shein / brand）',
    `heat_score` INT          NOT NULL DEFAULT 0      COMMENT '热度分值（越高越流行）',
    `created_at` DATETIME     NOT NULL                COMMENT '记录创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_category`   (`category`),
    KEY `idx_source`     (`source`),
    KEY `idx_heat_score` (`heat_score`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 外部趋势款选品表（SelectionFeedAgent 读取，来源 1688/TikTok/SHEIN/品牌）';

-- ============================================================
-- 19. deepay_design_image — 设计图评分结果表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_design_image` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `url`         VARCHAR(500)      DEFAULT NULL  COMMENT '图片CDN地址',
    `category`    VARCHAR(64)       DEFAULT NULL  COMMENT '品类',
    `style`       VARCHAR(64)       DEFAULT NULL  COMMENT '风格',
    `score`       DOUBLE   NOT NULL DEFAULT 0     COMMENT '综合分',
    `trend_score` DOUBLE   NOT NULL DEFAULT 0     COMMENT '趋势分',
    `match_score` DOUBLE   NOT NULL DEFAULT 0     COMMENT '客户匹配分',
    `created_at`  DATETIME NOT NULL               COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_category` (`category`),
    KEY `idx_score`    (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 设计图评分结果表（ImageScoringAgent 写入）';

-- ============================================================
-- 20. deepay_selection_pool — 选款参考池
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_selection_pool` (
    `id`        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `image_url` VARCHAR(512)         DEFAULT NULL  COMMENT '款式图片URL',
    `category`  VARCHAR(64)          DEFAULT NULL  COMMENT '品类',
    `style`     VARCHAR(64)          DEFAULT NULL  COMMENT '风格标签',
    `source`    VARCHAR(32)          DEFAULT NULL  COMMENT '来源（1688/tiktok/shein/brand）',
    `score`     DOUBLE      NOT NULL DEFAULT 0     COMMENT '热度评分',
    `created_at` DATETIME   NOT NULL               COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_category` (`category`),
    KEY `idx_score`    (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 选款参考池（SelectionFeedAgent 读取）';

-- ============================================================
-- 21. deepay_feedback — 用户反馈学习表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_feedback` (
    `id`         BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
    `image_id`   BIGINT              DEFAULT NULL  COMMENT '关联 deepay_design_image.id',
    `image_url`  VARCHAR(512)        DEFAULT NULL  COMMENT '图片URL',
    `user_id`    VARCHAR(64)         DEFAULT NULL  COMMENT '用户ID',
    `selected`   TINYINT(1) NOT NULL DEFAULT 0     COMMENT '是否被选中（1=是 / 0=否）',
    `created_at` DATETIME   NOT NULL               COMMENT '记录时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_image_id` (`image_id`),
    KEY `idx_user_id`  (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 用户反馈学习表（FeedbackAgent 写入）';

SET FOREIGN_KEY_CHECKS = 1;
