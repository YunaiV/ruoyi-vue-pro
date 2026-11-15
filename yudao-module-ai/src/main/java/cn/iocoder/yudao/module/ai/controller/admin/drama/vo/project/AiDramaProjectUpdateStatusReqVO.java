package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaProjectStepEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧项目步骤修改 Request VO")
@Data
public class AiDramaProjectUpdateStatusReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "步骤，参见 AiDramaProjectStepEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "步骤不能为空")
    @InEnum(AiDramaProjectStepEnum.class)
    private Integer status;

}
