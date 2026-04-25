package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 使用量记录表（deepay_ai_usage）。
 *
 * <p>按用户 + 日期聚合记录每日 AI 调用量，用于每日用量上限控制与运营分析。</p>
 *
 * <pre>
 * CREATE TABLE deepay_ai_usage (
 *   id          BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   tenant_id   BIGINT      NOT NULL DEFAULT 0  COMMENT '租户 ID',
 *   user_id     VARCHAR(64) NOT NULL             COMMENT '用户标识',
 *   usage_date  DATE        NOT NULL             COMMENT '统计日期',
 *   daily_count INT         NOT NULL DEFAULT 0  COMMENT '当日调用次数',
 *   module      VARCHAR(32)          DEFAULT ''  COMMENT '最后使用的板块',
 *   created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   UNIQUE KEY uk_user_date (tenant_id, user_id, usage_date)
 * );
 * </pre>
 */
@TableName("deepay_ai_usage")
@Data
public class DeepayAiUsageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户 ID（0 = 默认） */
    private Long tenantId;

    /** 用户标识（管理端用户 ID 或前台 customerId） */
    private String userId;

    /** 统计日期 */
    private LocalDate usageDate;

    /** 当日调用次数（原子自增） */
    private Integer dailyCount;

    /** 最后使用的板块 */
    private String module;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
