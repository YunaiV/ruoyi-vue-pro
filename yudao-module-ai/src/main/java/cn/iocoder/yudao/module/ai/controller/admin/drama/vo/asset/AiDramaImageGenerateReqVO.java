package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧资产出图 Request VO：角色形象、场景、视觉资产共用")
@Data
public class AiDramaImageGenerateReqVO {

    @Schema(description = "编号：角色形象、场景或视觉资产的编号；一键补齐时为项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "绘画模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "绘画模型不能为空")
    private Long modelId;

}
