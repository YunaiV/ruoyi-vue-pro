package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat req
 *
 * @author fansili
 * @time 2024/4/14 16:12
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatReq {

    @Schema(description = "ai模型(查看 AiClientNameEnum)")
    @NotNull(message = "模型不能为空!")
    @Size(max = 30, message = "模型字符最大 30个字符!")
    private String modal;

    @Schema(description = "对话Id")
    @NotNull(message = "对话id不能为空")
    private Long conversationId;

    @Schema(description = "chat角色模板")
    private Long chatRoleId;

    @NotNull(message = "提示词不能为空!")
    @Size(max = 5000, message = "提示词最大5000个字符!")
    @Schema(description = "填入固定值，1 issues, 2 pr")
    private String prompt;

    @Schema(description = "用于控制随机性和多样性的温度参数")
    private Double temperature;

    @Schema(description = "生成时，核采样方法的概率阈值。例如，取值为0.8时，仅保留累计概率之和大于等于0.8的概率分布中的token，\n" +
            "     * 作为随机采样的候选集。取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的随机性越低。\n" +
            "     * 默认值为0.8。注意，取值不要大于等于1\n")
    private Double topP;

    @Schema(description = "在生成消息时采用的Top-K采样大小，表示模型生成回复时考虑的候选项集合的大小")
    private Double topK;

}
