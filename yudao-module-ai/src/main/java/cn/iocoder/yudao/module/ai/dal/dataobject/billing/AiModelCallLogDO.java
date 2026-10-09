package cn.iocoder.yudao.module.ai.dal.dataobject.billing;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiCallStatusEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiTokenSourceEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 模型调用日志 DO
 *
 * @author 芋道源码
 */
@TableName("ai_model_call_log")
@KeySequence("ai_model_call_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelCallLogDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 平台标识，例如 OPENAI/DEEP_SEEK
     */
    private String platform;
    /**
     * 模型编号，关联 ai_model.id，可为空
     */
    private Long modelId;
    /**
     * 模型标识，冗余自 ai_model.model
     */
    private String model;
    /**
     * API 密钥编号，关联 ai_api_key.id，可为空
     */
    private Long apiKeyId;
    /**
     * 业务类型
     *
     * 枚举 {@link AiBizTypeEnum}
     */
    private String bizType;
    /**
     * 业务主键，例如 ai_chat_message.id/ai_write.id 等
     */
    private Long bizId;
    /**
     * 会话编号，聊天场景填 ai_chat_conversation.id
     */
    private Long conversationId;
    /**
     * 链路追踪编号
     */
    private String traceId;
    /**
     * 请求时间
     */
    private LocalDateTime requestTime;
    /**
     * 响应时间
     */
    private LocalDateTime responseTime;
    /**
     * 耗时，单位：毫秒
     */
    private Integer durationMs;
    /**
     * 状态
     *
     * 枚举 {@link AiCallStatusEnum}
     */
    private String status;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 厂商请求编号，用于对账
     */
    private String requestId;

    // ========== Token 统计 ==========

    /**
     * 输入 Token 数（总量，含缓存命中部分）
     */
    private Integer promptTokens;
    /**
     * 输出 Token 数（总量，含推理/思考部分）
     */
    private Integer completionTokens;
    /**
     * 总 Token 数
     */
    private Integer totalTokens;
    /**
     * 缓存命中 Token 数（输入中命中上下文缓存的部分）
     */
    private Integer cachedTokens;
    /**
     * 推理/思考 Token 数（输出中用于推理的部分）
     */
    private Integer reasoningTokens;
    /**
     * Token 来源
     *
     * 枚举 {@link AiTokenSourceEnum}
     */
    private String tokenSource;

    // ========== 费用 ==========

    /**
     * 币种，首期固定为 CNY
     */
    private String currency;
    /**
     * 输入单价快照：微元/100万 tokens（缓存未命中）
     */
    @TableField("price_in_per_1m")
    private Long priceInPer1m;
    /**
     * 缓存命中输入单价快照：微元/100万 tokens，0 表示不区分
     */
    @TableField("price_cached_per_1m")
    private Long priceCachedPer1m;
    /**
     * 输出单价快照：微元/100万 tokens（标准输出）
     */
    @TableField("price_out_per_1m")
    private Long priceOutPer1m;
    /**
     * 推理/思考输出单价快照：微元/100万 tokens，0 表示不区分
     */
    @TableField("price_reasoning_per_1m")
    private Long priceReasoningPer1m;
    /**
     * 本次调用费用，单位：微元（1元=1,000,000微元）
     */
    private Long costAmount;
    /**
     * 是否被预算拦截：false 否；true 是
     */
    private Boolean blocked;

}
