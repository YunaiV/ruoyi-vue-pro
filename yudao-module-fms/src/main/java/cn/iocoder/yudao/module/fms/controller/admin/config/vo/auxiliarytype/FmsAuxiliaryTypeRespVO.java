package cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliarytype;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * FMS 辅助核算类别 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 辅助核算类别 Response VO")
@Data
public class FmsAuxiliaryTypeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户")
    private String name;

    @Schema(description = "是否系统预置", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean systemPreset;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountSetId;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

}
