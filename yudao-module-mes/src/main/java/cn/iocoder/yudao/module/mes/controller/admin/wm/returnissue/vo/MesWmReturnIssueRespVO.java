package cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产退料单 Response VO")
@Data
public class MesWmReturnIssueRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "退料单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "RI20250226001")
    private String code;

    @Schema(description = "退料单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "生产退料")
    private String name;

    @Schema(description = "生产工单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WO20250226001")
    private String workOrderCode;

    @Schema(description = "工作站 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workstationId;

    @Schema(description = "工作站名称", example = "装配工作站")
    private String workstationName;

    @Schema(description = "退料类型", example = "1")
    private Integer type;

    @Schema(description = "退料日期")
    private LocalDateTime returnDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
