package cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 模型 Response VO")
@Data
public class Ai1ModelRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long providerId;

    @Schema(description = "展示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "DeepSeek Chat")
    private String name;

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "deepseek-chat")
    private String model;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer type;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
