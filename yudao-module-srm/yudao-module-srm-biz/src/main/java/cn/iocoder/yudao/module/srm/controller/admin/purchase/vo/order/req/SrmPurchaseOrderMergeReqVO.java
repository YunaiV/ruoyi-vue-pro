package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

@Schema(description = "管理后台 - ERP 采购订单合并 VO")
@Data
public class SrmPurchaseOrderMergeReqVO {

    // 1. 业务主字段
    @Schema(description = "单据日期")
    @DiffLogField(name = "单据日期")
    private LocalDateTime billTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    @DiffLogField(name = "供应商编号")
    private Long supplierId;

    @Schema(description = "收货地址")
    @DiffLogField(name = "收货地址")
    private String address;

    // 2. 结算相关
    @Schema(description = "结算日期")
    @DiffLogField(name = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "结算账户编号")
    @DiffLogField(name = "结算账户编号")
    private Long accountId;

    // 3. 金额相关
    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    @DiffLogField(name = "优惠率")
    private BigDecimal discountPercent;

    @Schema(description = "其它金额，单位：元")
    @DiffLogField(name = "其它金额")
    private BigDecimal otherPrice;

    // 4. 附件与备注
    @Schema(description = "附件地址")
    @DiffLogField(name = "附件地址")
    private String fileUrl;

    @Schema(description = "入库主单备注")
    @DiffLogField(name = "入库主单备注")
    private String remark;

    // 5. 入库清单
    @Schema(description = "入库清单列表")
    @NotNull(message = "入库项不能为空")
    @Size(min = 1, message = "入库项至少一个")
    @DiffLogField(name = "入库清单列表")
    private Collection<@Valid Item> items;

    @Data
    public static class Item {
        @Schema(description = "仓库编号")
        @NotNull(message = "仓库编号不能为空") // 合并入库时必须要仓库ID
        @DiffLogField(name = "仓库编号")
        private Long warehouseId;

        @Schema(description = "采购订单项编号")
        @NotNull(message = "采购订单项编号不能为空")
        @DiffLogField(name = "采购订单项编号")
        private Long itemId;

        @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "到货数量不能为空")
        @DiffLogField(name = "到货数量")
        private BigDecimal qty;
    }
}
