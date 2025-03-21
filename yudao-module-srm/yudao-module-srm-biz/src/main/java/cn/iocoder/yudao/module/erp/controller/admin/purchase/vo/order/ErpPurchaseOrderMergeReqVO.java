package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

@Schema(description = "管理后台 - ERP 采购订单 合并 VO")
@Data
public class ErpPurchaseOrderMergeReqVO {

    /**
     * 订单项ids
     */
    @NotNull(message = "采购订单项不能为空")
    Collection<Long> itemIds;
    @Schema(description = "单据日期")
    private LocalDateTime noTime;
    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;
    //收获地址
    @Schema(description = "收获地址")
    private String address;
    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;
    @Schema(description = "结算账户编号")
    private Long accountId;
    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal discountPercent;
    @Schema(description = "其它金额，单位：元")
    private BigDecimal otherPrice;
    @Schema(description = "附件地址")
    private String fileUrl;
    @Schema(description = "入库备注")
    private String remark;
}
