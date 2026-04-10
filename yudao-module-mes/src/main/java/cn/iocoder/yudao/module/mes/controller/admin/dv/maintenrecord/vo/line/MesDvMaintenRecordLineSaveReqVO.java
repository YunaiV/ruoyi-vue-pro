package cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - MES 设备保养记录明细新增/修改 Request VO")
@Data
public class MesDvMaintenRecordLineSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "保养记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "保养记录不能为空")
    private Long recordId;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目不能为空")
    private Long subjectId;

    @Schema(description = "保养结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "保养结果不能为空")
    private Integer status;

    @Schema(description = "异常描述", example = "发现损坏")
    private String result;

    @Schema(description = "备注", example = "测试备注")
    private String remark;

}
