package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "管理后台 - 用品发放 Request VO")
@Data
public class OaSupplyIssueReqVO {

    @Schema(description = "明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "明细编号不能为空")
    private Long id;

    @Schema(description = "实发数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "实发数量不能为空")
    @Min(value = 1, message = "实发数量不能小于 {value}")
    private Integer issuedQuantity;

    @Schema(description = "发放备注", example = "已按申请明细发放")
    @Size(max = 500, message = "发放备注长度不能超过 {max} 个字符")
    private String issueRemark;

}
