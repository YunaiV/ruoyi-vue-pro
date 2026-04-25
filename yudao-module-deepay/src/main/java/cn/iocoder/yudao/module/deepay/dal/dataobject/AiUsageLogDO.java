package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 调用用量日志 {@code ai_usage_log}。
 *
 * <p>记录每次 AI 调用的关键信息，用于：
 * <ul>
 *   <li>防滥用（每日/每分钟用量分析）</li>
 *   <li>计费依据（token 估算）</li>
 *   <li>运营看板（用量趋势）</li>
 * </ul>
 * </p>
 */
@TableName("ai_usage_log")
@Data
public class AiUsageLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户 ID（预留多租户，当前默认 0）*/
    private Long tenantId;

    /** 用户 ID（空字符串表示匿名）*/
    private String userId;

    /** 模块名 */
    private String module;

    /** 会话 ID */
    private String sessionId;

    /** 上下文实体类型（order/product/customer/paymentLink 等）*/
    private String entityType;

    /** 上下文实体 ID */
    private String entityId;

    /** 预估 token 数（请求+回复合计，可选）*/
    private Integer tokensEst;

    /**
     * 调用状态。
     * <ul>
     *   <li>{@code OK}              — 成功</li>
     *   <li>{@code RATE_LIMITED}    — 触发限流</li>
     *   <li>{@code QUOTA_EXCEEDED}  — 配额耗尽</li>
     *   <li>{@code ERROR}           — 服务错误</li>
     * </ul>
     */
    private String status;

    /** 错误信息（status=ERROR 时）*/
    private String errorMsg;

    private LocalDateTime createdAt;
}
