package cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - MES 设备点检记录明细新增/修改 Request VO")
@Data
public class MesDvCheckRecordLineSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "点检记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "点检记录不能为空")
    private Long recordId;

    @Schema(description = "点检项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "点检项目不能为空")
    private Long subjectId;

    @Schema(description = "点检结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "点检结果不能为空")
    private Integer checkStatus;

    @Schema(description = "异常描述", example = "设备异响")
    private String checkResult;

    @Schema(description = "备注", example = "测试备注")
    private String remark;

}
