package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采购合同入参VO
 */
@Data
public class SrmPurchaseOrderGenerateContractReqVO {
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板名称不能为空")
    @DiffLogField(name = "模板名称")
    private String templateName;

    @Schema(description = "采购订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购订单编号不能为空")
    @DiffLogField(name = "采购订单编号")
    private Long orderId;

    //签订地点
    @Schema(description = "签订地点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "签订地点不能为空")
    @DiffLogField(name = "签订地点")
    private String signingPlace;
    //订立日期
    @Schema(description = "订立日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订立日期不能为空")
    @DiffLogField(name = "订立日期")
    private LocalDateTime signingDate;

    //甲方乙方
    //甲方主体id
    @Schema(description = "甲方主体id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "甲方主体id不能为空")
    @DiffLogField(name = "甲方财务主体编号")
    private Long partyAId;

    @Schema(description = "乙方供应商ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乙方供应商ID不能为空")
    @DiffLogField(name = "乙方供应商编号")
    private Long partyBId;

    @Schema(description = "付款条款", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "付款条款不能是空格字符串")
    @NotNull(message = "付款条款不能为空")
    @DiffLogField(name = "付款条款")
    private String paymentTerms;
}
