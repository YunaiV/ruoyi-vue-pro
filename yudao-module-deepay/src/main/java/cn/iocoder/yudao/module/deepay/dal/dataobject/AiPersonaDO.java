package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 角色人设配置表 {@code ai_persona}。
 *
 * <p>支持按 tenant_id + module 配置差异化 system prompt，后端读取策略：
 * <ol>
 *   <li>优先 {@code tenant_id=当前租户 + module=目标模块}</li>
 *   <li>其次 {@code tenant_id=0 + module=目标模块}（全局默认）</li>
 *   <li>最后回退到代码中硬编码的 prompt</li>
 * </ol>
 * </p>
 */
@TableName("ai_persona")
@Data
public class AiPersonaDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户 ID（0=全局默认，预留多租户） */
    private Long tenantId;

    /**
     * 模块名（selection/design/product/inventory/finance/trend/order/global）。
     * {@code global} 表示全站 Copilot 入口。
     */
    private String module;

    /** 角色显示名称（前端头像标题）*/
    private String roleName;

    /** System Prompt（角色指令）*/
    private String systemPrompt;

    /** 示例对话（JSON 数组，few-shot，可选）*/
    private String examples;

    /** 允许调用的工具列表（逗号分隔，预留）*/
    private String toolWhitelist;

    /** 是否启用：1=启用，0=禁用 */
    private Integer enabled;

    /** 同 module 下排序（数字越小优先级越高）*/
    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
