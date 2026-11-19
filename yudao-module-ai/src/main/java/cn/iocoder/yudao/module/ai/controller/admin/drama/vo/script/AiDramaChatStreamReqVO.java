package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaChatTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - AI 短剧对话模型流式生成 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDramaChatStreamReqVO extends AiDramaChatReqVO {

    @Schema(description = "生成类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生成类型不能为空")
    @InEnum(AiDramaChatTypeEnum.class)
    private Integer type;

    @Schema(description = "打磨意见：打磨剧本时必填", example = "结尾加一个反转")
    @Size(max = 1000, message = "打磨意见不能超过 1000 个字符")
    private String note;

}
