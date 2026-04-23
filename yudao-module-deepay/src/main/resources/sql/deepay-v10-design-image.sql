CREATE TABLE IF NOT EXISTS deepay_design_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    url VARCHAR(500) NULL COMMENT '图片CDN地址',
    category VARCHAR(64) NULL COMMENT '品类',
    style VARCHAR(64) NULL COMMENT '风格',
    score DOUBLE NOT NULL DEFAULT 0 COMMENT '综合分',
    trend_score DOUBLE NOT NULL DEFAULT 0 COMMENT '趋势分',
    match_score DOUBLE NOT NULL DEFAULT 0 COMMENT '客户匹配分',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_category (category),
    INDEX idx_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计图评分结果表（ImageScoringAgent 写入）';
