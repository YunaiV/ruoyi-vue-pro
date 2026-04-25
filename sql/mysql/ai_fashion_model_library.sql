-- ============================================================
-- 服装设计模特库 & 增强采集器相关表
-- 数据库：MySQL 8.x
-- 版本：v1.0.0  2026-04-25
-- ============================================================

-- ----------------------------
-- 1. 时装图片素材库
-- ----------------------------
CREATE TABLE `ai_fashion_model_library`  (
  `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '编号',
  `source_url`       varchar(2000) NULL     DEFAULT NULL  COMMENT '原始来源 URL',
  `local_path`       varchar(1000) NULL     DEFAULT NULL  COMMENT '服务器本地路径（或对象存储 key）',
  `file_id`          bigint        NULL     DEFAULT NULL  COMMENT '关联的文件 ID（FileApi）',
  `title`            varchar(500)  NULL     DEFAULT NULL  COMMENT '标题',
  `description`      varchar(2000) NULL     DEFAULT NULL  COMMENT '描述',
  `category`         varchar(100)  NULL     DEFAULT NULL  COMMENT '品类（dress/suit/shirt/pants/jacket/skirt/accessories）',
  `style_tags`       json          NULL     DEFAULT NULL  COMMENT '风格标签（JSON 数组）',
  `color_tags`       json          NULL     DEFAULT NULL  COMMENT '颜色标签（JSON 数组）',
  `brand`            varchar(200)  NULL     DEFAULT NULL  COMMENT '品牌名称',
  `season`           varchar(100)  NULL     DEFAULT NULL  COMMENT '季节（spring_summer_2024/autumn_winter_2024 等）',
  `source_type`      varchar(50)   NOT NULL               COMMENT '来源类型（fashion_show/brand/model_agency/street_style）',
  `collection_source_id` varchar(100) NULL  DEFAULT NULL  COMMENT '关联采集源 ID',
  `image_hash`       varchar(64)   NULL     DEFAULT NULL  COMMENT '图片 MD5 哈希（去重用）',
  `width`            int           NULL     DEFAULT NULL  COMMENT '图片宽度（像素）',
  `height`           int           NULL     DEFAULT NULL  COMMENT '图片高度（像素）',
  `file_size`        bigint        NULL     DEFAULT NULL  COMMENT '文件大小（字节）',
  `file_format`      varchar(20)   NULL     DEFAULT NULL  COMMENT '文件格式（jpg/png/webp）',
  `is_model`         bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否含模特',
  `model_pose`       varchar(100)  NULL     DEFAULT NULL  COMMENT '模特姿势（walking/standing/sitting/front/side/back）',
  `model_body_type`  varchar(100)  NULL     DEFAULT NULL  COMMENT '模特体型（slim/athletic/curvy/plus_size）',
  `indexed_at`       datetime      NULL     DEFAULT NULL  COMMENT '索引时间',
  `creator`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
  `update_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_image_hash` (`image_hash`) USING BTREE,
  INDEX `idx_source_type`  (`source_type`) USING BTREE,
  INDEX `idx_category`     (`category`) USING BTREE,
  INDEX `idx_brand`        (`brand`) USING BTREE,
  INDEX `idx_is_model`     (`is_model`) USING BTREE,
  INDEX `idx_create_time`  (`create_time`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 服装设计素材库（时装秀/品牌/模特图片）';

-- ----------------------------
-- 2. 模特身体特征表
-- ----------------------------
CREATE TABLE `ai_fashion_model_features`  (
  `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '编号',
  `library_image_id` bigint        NOT NULL               COMMENT '关联素材库图片编号',
  `height_cm`        int           NULL     DEFAULT NULL  COMMENT '身高（厘米）',
  `weight_kg`        decimal(5,1)  NULL     DEFAULT NULL  COMMENT '体重（公斤）',
  `bust_cm`          int           NULL     DEFAULT NULL  COMMENT '胸围（厘米）',
  `waist_cm`         int           NULL     DEFAULT NULL  COMMENT '腰围（厘米）',
  `hips_cm`          int           NULL     DEFAULT NULL  COMMENT '臀围（厘米）',
  `skin_tone`        varchar(50)   NULL     DEFAULT NULL  COMMENT '肤色（fair/medium/tan/dark）',
  `hair_color`       varchar(50)   NULL     DEFAULT NULL  COMMENT '发色（black/brown/blonde/red/white）',
  `hair_length`      varchar(50)   NULL     DEFAULT NULL  COMMENT '发长（short/medium/long）',
  `pose_type`        varchar(100)  NULL     DEFAULT NULL  COMMENT '姿势类型（front/side/back/walking/sitting/dynamic）',
  `creator`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
  `update_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_library_image_id` (`library_image_id`) USING BTREE,
  INDEX `idx_pose_type`        (`pose_type`) USING BTREE,
  INDEX `idx_body_type`        (`skin_tone`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 服装设计模特身体特征';

-- ----------------------------
-- 3. 采集源配置表
-- ----------------------------
CREATE TABLE `ai_fashion_collection_source`  (
  `id`              varchar(100)  NOT NULL               COMMENT '来源标识（唯一 key）',
  `name`            varchar(200)  NOT NULL               COMMENT '来源名称',
  `url`             varchar(1000) NOT NULL               COMMENT '来源 URL',
  `source_type`     varchar(50)   NOT NULL               COMMENT '来源类型（fashion_show/brand/model_agency/street_style）',
  `category`        varchar(100)  NULL     DEFAULT NULL  COMMENT '分类说明',
  `priority`        int           NOT NULL DEFAULT 1     COMMENT '优先级（1 最高）',
  `status`          varchar(20)   NOT NULL DEFAULT 'active' COMMENT '状态（active/paused/disabled）',
  `last_collected`  datetime      NULL     DEFAULT NULL  COMMENT '最近一次采集时间',
  `collect_count`   int           NOT NULL DEFAULT 0     COMMENT '累计采集图片数',
  `config`          json          NULL     DEFAULT NULL  COMMENT '采集配置（interval_hours/max_pages/selectors 等）',
  `creator`         varchar(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
  `create_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`         varchar(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
  `update_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_source_type` (`source_type`) USING BTREE,
  INDEX `idx_status`      (`status`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 服装设计素材采集源配置';

-- ----------------------------
-- 4. 初始化采集源数据
-- 10大时装秀 + 10大品牌 + 5大模特机构
-- ----------------------------

-- 10大时装秀媒体
INSERT INTO `ai_fashion_collection_source` (`id`, `name`, `url`, `source_type`, `category`, `priority`, `config`) VALUES
('vogue_runway',        'Vogue Runway',             'https://www.vogue.com/runway',           'fashion_show', '时装秀媒体', 1,  '{"interval_hours":24,"max_pages":10}'),
('wwd_runway',          'WWD Runway',               'https://wwd.com/runway/',                'fashion_show', '时装秀媒体', 2,  '{"interval_hours":24,"max_pages":10}'),
('elle_runway',         'Elle Runway',              'https://www.elle.com/runway/',           'fashion_show', '时装秀媒体', 3,  '{"interval_hours":24,"max_pages":8}'),
('harpersbazaar_runway','Harper\'s Bazaar Runway',  'https://www.harpersbazaar.com/runway/', 'fashion_show', '时装秀媒体', 4,  '{"interval_hours":24,"max_pages":8}'),
('nymag_runway',        'New York Magazine Runway', 'https://nymag.com/runway/',             'fashion_show', '时装秀媒体', 5,  '{"interval_hours":48,"max_pages":5}'),
('refinery29_runway',   'Refinery29 Runway',        'https://www.refinery29.com/runway',     'fashion_show', '时装秀媒体', 6,  '{"interval_hours":48,"max_pages":5}'),
('whowhatwear_runway',  'Who What Wear Runway',     'https://www.whowhatwear.com/runway',    'fashion_show', '时装秀媒体', 7,  '{"interval_hours":48,"max_pages":5}'),
('fashionweekonline',   'Fashion Week Online',      'https://fashionweekonline.com',          'fashion_show', '时装周专站', 8,  '{"interval_hours":24,"max_pages":10}'),
('nowfashion',          'NowFashion',               'https://nowfashion.com',                'fashion_show', '时装秀专站', 9,  '{"interval_hours":24,"max_pages":10}'),
('style_runway',        'Style.com Runway',         'https://www.style.com/runway',          'fashion_show', '时装秀媒体', 10, '{"interval_hours":48,"max_pages":8}');

-- 10大奢侈品牌
INSERT INTO `ai_fashion_collection_source` (`id`, `name`, `url`, `source_type`, `category`, `priority`, `config`) VALUES
('gucci',         'Gucci',        'https://www.gucci.com',        'brand', '奢侈品牌', 1,  '{"interval_hours":72,"max_pages":20}'),
('chanel',        'Chanel',       'https://www.chanel.com',       'brand', '奢侈品牌', 2,  '{"interval_hours":72,"max_pages":20}'),
('dior',          'Dior',         'https://www.dior.com',         'brand', '奢侈品牌', 3,  '{"interval_hours":72,"max_pages":20}'),
('prada',         'Prada',        'https://www.prada.com',        'brand', '奢侈品牌', 4,  '{"interval_hours":72,"max_pages":15}'),
('louisvuitton',  'Louis Vuitton','https://www.louisvuitton.com', 'brand', '奢侈品牌', 5,  '{"interval_hours":72,"max_pages":20}'),
('balenciaga',    'Balenciaga',   'https://www.balenciaga.com',   'brand', '奢侈品牌', 6,  '{"interval_hours":72,"max_pages":15}'),
('versace',       'Versace',      'https://www.versace.com',      'brand', '奢侈品牌', 7,  '{"interval_hours":72,"max_pages":15}'),
('armani',        'Armani',       'https://www.armani.com',       'brand', '奢侈品牌', 8,  '{"interval_hours":72,"max_pages":15}'),
('burberry',      'Burberry',     'https://www.burberry.com',     'brand', '英伦品牌', 9,  '{"interval_hours":72,"max_pages":15}'),
('hermes',        'Hermès',       'https://www.hermes.com',       'brand', '奢侈品牌', 10, '{"interval_hours":72,"max_pages":10}');

-- 5大模特机构
INSERT INTO `ai_fashion_collection_source` (`id`, `name`, `url`, `source_type`, `category`, `priority`, `config`) VALUES
('img_models',   'IMG Models',    'https://www.imgmodels.com',   'model_agency', '国际顶级模特机构', 1, '{"interval_hours":168,"max_pages":5}'),
('ford_models',  'Ford Models',   'https://www.fordmodels.com',  'model_agency', '国际顶级模特机构', 2, '{"interval_hours":168,"max_pages":5}'),
('elite_model',  'Elite Model',   'https://www.elitemodel.com',  'model_agency', '国际顶级模特机构', 3, '{"interval_hours":168,"max_pages":5}'),
('wilhelmina',   'Wilhelmina',    'https://www.wilhelmina.com',  'model_agency', '国际模特机构',     4, '{"interval_hours":168,"max_pages":5}'),
('next_models',  'Next Models',   'https://www.nextmodels.com',  'model_agency', '国际模特机构',     5, '{"interval_hours":168,"max_pages":5}');
