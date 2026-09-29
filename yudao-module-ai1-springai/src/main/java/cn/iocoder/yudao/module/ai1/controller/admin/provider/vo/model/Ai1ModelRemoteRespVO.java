package cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - AI1 远程模型 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ai1ModelRemoteRespVO {

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "deepseek-chat")
    private String model;

    @Schema(description = "是否已导入当前 Provider", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean imported;

}
