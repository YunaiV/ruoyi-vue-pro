package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order;

import cn.hutool.core.date.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 采购合同入参VO
 */
@Data
public class ErpPurchaseOrderGenerateContractReqVO {
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "采购订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购订单编号不能为空")
    private Long orderId;

    //签订地点
    @Schema(description = "签订地点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "签订地点不能为空")
    private String signingPlace;
    //订立日期
    @Schema(description = "订立日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订立日期不能为空")
    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private LocalDateTime signingDate;

    @Schema(description = "币别名称(计价单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "CNY")
    @NotBlank(message = "币别名称(计价单位)不能为空")
    private String currencyName;

    //甲方乙方
    //甲方主体id
    @Schema(description = "甲方主体id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "甲方主体id不能为空")
    private Long partyAId;

    //乙方主体id
    @Schema(description = "乙方主体id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乙方主体id不能为空")
    private Long partyBId;

    @Schema(description = "付款条款", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "付款条款不能为空")
    private String paymentTerms;
}
