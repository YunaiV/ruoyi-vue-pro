package cn.iocoder.yudao.framework.cola.statemachine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 审批情况
 */
@Schema(description = "审批 Request VO")
@Data
public class ApprovalReqVO {

    @Schema(description = "来源单据类型 ; BillType : 0-出库单 , 1-入库单", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "代码不能为空")
    private Integer billType;

    @Schema(description = "业务单据ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29844")
    @NotEmpty(message = "名称不能为空")
    private Long billId;

    @Schema(description = "状态类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态类型不能为空")
    private String statusType;

    @Schema(description = "审批意见")
    private String comment;
}
