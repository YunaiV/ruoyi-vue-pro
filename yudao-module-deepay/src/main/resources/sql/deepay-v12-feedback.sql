CREATE TABLE IF NOT EXISTS deepay_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NULL COMMENT '关联deepay_design_image.id',
    image_url VARCHAR(512) NULL COMMENT '图片URL',
    user_id VARCHAR(64) NULL COMMENT '用户ID',
    selected TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否被选中(1=是/0=否)',
    created_at DATETIME NOT NULL COMMENT '记录时间',
    INDEX idx_image_id (image_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈学习表（FeedbackAgent写入）';
