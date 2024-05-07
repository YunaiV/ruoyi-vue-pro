package cn.iocoder.yudao.module.ai.controller.admin.model.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * modal list
 *
 * @author fansili
 * @time 2024/4/24 19:56
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatModalRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "API 秘钥编号")
    private Long keyId;

    @Schema(description = "模型名字")
    private String name;

    @Schema(description = "模型类型(qianwen、yiyan、xinghuo、openai)")
    private String model;

    @Size(max = 32, message = "模型平台最大32个字符")
    private String platform;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    // ========== 会话配置 ==========

    @Schema(description = "温度参数")
    private Integer temperature;

    @Schema(description = "单条回复的最大 Token 数量")
    private Integer maxTokens;

    @Schema(description = "上下文的最大 Message 数量")
    private Integer maxContexts;
}
