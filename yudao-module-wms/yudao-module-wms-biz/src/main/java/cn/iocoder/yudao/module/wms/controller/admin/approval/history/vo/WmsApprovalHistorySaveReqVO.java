package cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : bill_id,status_after,status_type,bill_type,comment,id,status_before
 */
@Schema(description = "管理后台 - 审批历史新增/修改 Request VO")
@Data
public class WmsApprovalHistorySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25351")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "代码不能为空")
    private String billType;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "29844")
    @NotEmpty(message = "名称不能为空")
    private String billId;

    @Schema(description = "状态类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态类型不能为空")
    private String statusType;

    @Schema(description = "审批前的状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审批前的状态不能为空")
    private Integer statusBefore;

    @Schema(description = "审批后状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审批后状态不能为空")
    private Integer statusAfter;

    @Schema(description = "审批意见")
    private String comment;
}
