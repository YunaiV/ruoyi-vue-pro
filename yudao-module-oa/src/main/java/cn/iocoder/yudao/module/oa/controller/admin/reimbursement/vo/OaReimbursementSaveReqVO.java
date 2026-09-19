package cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo;

import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 费用报销新增/修改 Request VO")
@Data
public class OaReimbursementSaveReqVO {

    @Schema(description = "申请编号", example = "1024")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户项目交通费报销")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过 255 个字符")
    private String title;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "紧急程度不能为空")
    @InDict(type = DictTypeConstants.APPLY_URGENCY, message = "紧急程度必须是 {value}")
    private Integer urgency;

    @Schema(description = "证明人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "证明人不能为空")
    private Long witnessUserId;

    @Schema(description = "相关客户", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道科技有限公司")
    @NotBlank(message = "相关客户不能为空")
    @Size(max = 255, message = "相关客户不能超过 255 个字符")
    private String customerName;

    @Schema(description = "报销方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "报销方式不能为空")
    @InDict(type = DictTypeConstants.REIMBURSEMENT_PAYMENT_METHOD, message = "报销方式必须是 {value}")
    private Integer paymentMethod;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    @Size(max = 5, message = "附件不能超过 5 个")
    private List<@NotBlank(message = "附件地址不能为空") @Size(max = 1024, message = "附件地址不能超过 1024 个字符") String> fileUrls;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "报销客户项目验收期间的交通费用")
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 5000, message = "申请原因不能超过 5000 个字符")
    private String reason;

    @Schema(description = "报销明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "报销明细不能为空")
    @Valid
    private List<@NotNull(message = "报销明细不能为空") Item> items;


    @Schema(description = "管理后台 - 报销明细 Request VO")
    @Data
    public static class Item {

        @Schema(description = "费用发生时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
        @NotNull(message = "费用发生时间不能为空")
        private LocalDateTime expenseTime;

        @Schema(description = "费用类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "费用类型不能为空")
        @InDict(type = DictTypeConstants.EXPENSE_TYPE, message = "费用类型必须是 {value}")
        private Integer expenseType;

        @Schema(description = "费用说明", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户项目验收往返交通费")
        @NotBlank(message = "费用说明不能为空")
        private String description;

        @Schema(description = "票据张数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "票据张数不能为空")
        @Min(value = 0, message = "票据张数不能小于 0")
        private Integer invoiceCount;

        @Schema(description = "报销金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "报销金额不能为空")
        @DecimalMin(value = "0", message = "报销金额不能小于 0")
        @Digits(integer = 16, fraction = 2, message = "金额最多 16 位整数、2 位小数")
        private BigDecimal price;

    }

}
