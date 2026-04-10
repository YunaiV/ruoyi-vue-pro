package cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.machinery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 点检保养方案设备 Response VO")
@Data
public class MesDvCheckPlanMachineryRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long planId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long machineryId;

    @Schema(description = "设备编码", example = "EQ001")
    private String machineryCode;

    @Schema(description = "设备名称", example = "注塑机A")
    private String machineryName;

    @Schema(description = "品牌", example = "海天")
    private String machineryBrand;

    @Schema(description = "规格型号", example = "HTF120")
    private String machinerySpec;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
