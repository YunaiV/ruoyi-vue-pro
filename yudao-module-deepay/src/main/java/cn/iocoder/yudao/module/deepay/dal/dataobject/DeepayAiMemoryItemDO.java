package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 长久记忆条目（deepay_ai_memory_item）。
 *
 * <p>按 tenantId + customerId + module + key 存储结构化记忆，
 * 每条记忆有独立类型（profile/fact/task）和置信度。</p>
 *
 * <pre>
 * CREATE TABLE deepay_ai_memory_item (
 *   id                BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   tenant_id         BIGINT       NOT NULL DEFAULT 0,
 *   customer_id       VARCHAR(64)  NOT NULL             COMMENT '前台用户/客户 ID',
 *   module            VARCHAR(32)  NOT NULL DEFAULT ''  COMMENT 'design/sales/finance/order',
 *   memory_type       VARCHAR(16)  NOT NULL DEFAULT 'fact' COMMENT 'profile/fact/task',
 *   mem_key           VARCHAR(128) NOT NULL             COMMENT '记忆键，如 preferredStyle',
 *   mem_value         TEXT                              COMMENT '记忆值（JSON 或文本）',
 *   confidence        DECIMAL(4,3) NOT NULL DEFAULT 1.0 COMMENT '置信度 0~1',
 *   source_session_id VARCHAR(64)           DEFAULT NULL COMMENT '来源会话 ID',
 *   expires_at        DATETIME              DEFAULT NULL COMMENT '过期时间（NULL=永久）',
 *   deleted           TINYINT(1)   NOT NULL DEFAULT 0   COMMENT '软删除',
 *   created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   INDEX idx_customer_module (tenant_id, customer_id, module),
 *   UNIQUE KEY uk_customer_module_key (tenant_id, customer_id, module, mem_key)
 * );
 * </pre>
 */
@TableName("deepay_ai_memory_item")
@Data
public class DeepayAiMemoryItemDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户 ID */
    private Long tenantId;

    /** 前台用户/客户 ID */
    private String customerId;

    /** 板块：design / sales / finance / order / selection */
    private String module;

    /** 记忆类型：profile（画像偏好）/ fact（稳定事实）/ task（进行中任务） */
    private String memoryType;

    /**
     * 记忆键，例如：
     * design: preferredStyle, favoriteColor, budgetRange, dislikeReasons
     * sales: motivation, objections, decisionStyle, priceRange
     * finance: paymentMethod, preferredChain, invoicePreference
     * order: deliveryPreference, aftersaleHistory
     */
    private String memKey;

    /** 记忆值（JSON 或纯文本） */
    private String memValue;

    /** 置信度（0.0~1.0，低于 0.5 的记忆在决策时权重降低） */
    private java.math.BigDecimal confidence;

    /** 来源会话 ID（便于追溯该记忆是从哪次对话中提取的） */
    private String sourceSessionId;

    /** 过期时间（NULL=永久保留，默认 365 天后过期） */
    private LocalDateTime expiresAt;

    /** 软删除标志 */
    private Integer deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
