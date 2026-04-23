CREATE TABLE IF NOT EXISTS deepay_selection_pool (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_url VARCHAR(512) NULL COMMENT '款式图片URL',
    category VARCHAR(64) NULL COMMENT '品类',
    style VARCHAR(64) NULL COMMENT '风格标签',
    source VARCHAR(32) NULL COMMENT '来源(1688/tiktok/shein/brand)',
    score DOUBLE NOT NULL DEFAULT 0 COMMENT '热度评分',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_category (category),
    INDEX idx_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选款参考池（SelectionFeedAgent读取）';
