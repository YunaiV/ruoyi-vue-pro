package cn.iocoder.yudao.module.deepay.service.tool;

import lombok.Builder;
import lombok.Data;

/**
 * 工具调用执行上下文 — 贯穿整条链路。
 */
@Data
@Builder
public class ToolCallContext {

    /** 租户 ID */
    private Long tenantId;

    /** 客户 / 用户 ID */
    private Long customerId;

    /** 会话 ID */
    private String sessionId;

    /** 板块（selection / design / order / …）*/
    private String module;

    /** 操作人（管理端用户名或 system） */
    private String operator;

}
