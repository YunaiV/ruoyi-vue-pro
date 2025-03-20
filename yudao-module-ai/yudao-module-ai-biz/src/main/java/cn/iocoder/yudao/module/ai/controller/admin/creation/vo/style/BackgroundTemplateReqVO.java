package cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI API 密钥新增/修改 Request VO")
@Data
public class BackgroundTemplateReqVO {

    @Schema(description = "一级级次名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "通用")
    private String fistLevelName;

    @Schema(description = "二级级次名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "色彩背景")
    private String secondLevelName;

}