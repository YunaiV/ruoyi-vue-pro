package cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.common.WmsBillType;

/**
 * @table-fields : bill_id,status_after,status_type,bill_type,comment,id,status_before
 */
@Schema(description = "管理后台 - 审批历史新增/修改 Request VO")
@Data
public class WmsApprovalHistorySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25351")
    private Long id;

    @Schema(description = "来源单据类型 ; BillType : 0-入库单 , 1-出库单", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "来源单据类型不能为空")
    @InEnum(WmsBillType.class)
    private Integer billType;

    @Schema(description = "业务单据ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29844")
    @NotNull(message = "业务单据ID不能为空")
    private Long billId;

    @Schema(description = "状态类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态类型不能为空")
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
