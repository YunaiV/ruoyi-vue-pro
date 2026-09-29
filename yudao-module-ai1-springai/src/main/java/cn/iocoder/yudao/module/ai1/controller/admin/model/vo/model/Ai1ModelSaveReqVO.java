package cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI1 模型新增/修改 Request VO")
@Data
public class Ai1ModelSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商编号不能为空")
    private Long providerId;

    @Schema(description = "展示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "DeepSeek Chat")
    @NotEmpty(message = "展示名称不能为空")
    @Size(max = 50, message = "展示名称长度不能超过 50 个字符")
    private String name;

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "deepseek-chat")
    @NotEmpty(message = "模型标识不能为空")
    @Size(max = 100, message = "模型标识长度不能超过 100 个字符")
    private String model;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "类型不能为空")
    @InEnum(Ai1ModelTypeEnum.class)
    private Integer type;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
