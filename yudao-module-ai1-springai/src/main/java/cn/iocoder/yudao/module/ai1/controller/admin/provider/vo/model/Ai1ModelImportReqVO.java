package cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI1 远程模型导入 Request VO")
@Data
public class Ai1ModelImportReqVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商编号不能为空")
    private Long providerId;

    @Schema(description = "模型标识列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"deepseek-chat\"]")
    @NotEmpty(message = "请选择要导入的模型")
    private List<String> models;

}
