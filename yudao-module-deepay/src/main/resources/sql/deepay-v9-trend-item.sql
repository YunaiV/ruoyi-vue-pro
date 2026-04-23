-- ============================================================
-- Deepay Phase 9 — 外部趋势款选品库（SelectionFeed）
-- 文件：deepay-v9-trend-item.sql
-- ============================================================

-- ============================================================
-- 1. 趋势款选品表 deepay_trend_item
--    来源：1688 / TikTok / SHEIN / 品牌官网爬取或人工录入
--    SelectionFeedAgent 读取后注入 ctx.trendItems，
--    供 TrendAgent 与 AIDecisionAgent 消费
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_trend_item (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    image_url   VARCHAR(512)    NULL                     COMMENT '款式图片 URL（CDN 或外部链接）',
    brand       VARCHAR(64)     NULL                     COMMENT '品牌名称',
    category    VARCHAR(64)     NULL                     COMMENT '品类（外套/内裤/连衣裙/裤子/上衣等）',
    style       VARCHAR(64)     NULL                     COMMENT '风格标签（SEXY/MINIMAL/CASUAL/SPORT/LUXURY）',
    price       DECIMAL(10,2)   NULL                     COMMENT '参考售价（元）',
    source      VARCHAR(32)     NULL                     COMMENT '数据来源（1688 / tiktok / shein / brand）',
    heat_score  INT             NOT NULL DEFAULT 0       COMMENT '热度分值（越高越流行）',
    created_at  DATETIME        NOT NULL                 COMMENT '记录创建时间',
    PRIMARY KEY (id),
    INDEX idx_category   (category),
    INDEX idx_source     (source),
    INDEX idx_heat_score (heat_score),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='外部趋势款选品表（SelectionFeedAgent 读取，来源 1688/TikTok/SHEIN/品牌）';
