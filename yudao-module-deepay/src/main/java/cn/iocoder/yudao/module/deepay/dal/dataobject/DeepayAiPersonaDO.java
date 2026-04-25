package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI Persona 配置表（deepay_ai_persona）。
 *
 * <p>支持按 tenantId + module 进行差异化配置；
 * 查询策略：tenantId+module → 仅 module（tenantId=0）→ 回退到代码默认值。</p>
 *
 * <pre>
 * CREATE TABLE deepay_ai_persona (
 *   id            BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   tenant_id     BIGINT       NOT NULL DEFAULT 0   COMMENT '租户 ID（0=全局默认）',
 *   module        VARCHAR(32)  NOT NULL              COMMENT '板块：selection/design/product/…',
 *   role_name     VARCHAR(64)  NOT NULL DEFAULT ''   COMMENT '角色名称',
 *   system_prompt TEXT                               COMMENT '注入 LLM 的 system prompt',
 *   examples      TEXT                               COMMENT '示例问答（JSON 数组）',
 *   tool_whitelist VARCHAR(512)                      COMMENT '允许使用的工具（逗号分隔）',
 *   enabled       TINYINT(1)   NOT NULL DEFAULT 1    COMMENT '是否启用',
 *   creator       VARCHAR(64)  NOT NULL DEFAULT ''   COMMENT '创建人',
 *   updater       VARCHAR(64)  NOT NULL DEFAULT ''   COMMENT '更新人',
 *   create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   deleted       TINYINT(1)   NOT NULL DEFAULT 0    COMMENT '软删除'
 * );
 * </pre>
 */
@TableName("deepay_ai_persona")
@Data
public class DeepayAiPersonaDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户 ID（0 = 全局默认配置） */
    private Long tenantId;

    /**
     * 板块标识，对应 AI 服务路由：
     * selection / design / product / inventory / finance / trend / order
     */
    private String module;

    /** 角色名称（用于 UI 展示） */
    private String roleName;

    /** 注入 LLM 的 system prompt（覆盖代码默认值） */
    private String systemPrompt;

    /** 示例问答（JSON 数组，格式：[{"q":"...","a":"..."}]），用于 few-shot */
    private String examples;

    /** 允许使用的工具列表（逗号分隔，如 "trendTool,inventoryTool"） */
    private String toolWhitelist;

    /** 是否启用（0=禁用，1=启用） */
    private Integer enabled;

    private String creator;
    private String updater;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 软删除标志（0=正常，1=已删除） */
    private Integer deleted;

}
