package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 销售订单新增/修改 Request VO")
@Data
public class ErpSaleOrderSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    private Long id;

    @Schema(description = "销售单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XS001")
    @NotEmpty(message = "销售单编号不能为空")
    private String no;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    @NotNull(message = "客户编号不能为空")
    private Long customerId;

    @Schema(description = "下单时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "下单时间不能为空")
    private LocalDateTime orderTime;

    @Schema(description = "销售员编号数组")
    private List<Long> salePersonIds;

    @Schema(description = "合计价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "26094")
    @NotNull(message = "合计价格，单位：元不能为空")
    private BigDecimal totalPrice;

    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
    @NotNull(message = "优惠率，百分比不能为空")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "4452")
    @NotNull(message = "优惠金额，单位：元不能为空")
    private BigDecimal discountPrice;

    // TODO 芋艿：后面删除
//    @Schema(description = "支付金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "32240")
//    @NotNull(message = "支付金额，单位：元不能为空")
//    private BigDecimal payPrice;

    @Schema(description = "定金金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    @NotNull(message = "定金金额，单位：元不能为空")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
    private String fileUrl;

    @Schema(description = "结算账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31189")
    @NotNull(message = "结算账户编号不能为空")
    private Long accountId;

    @Schema(description = "备注", example = "你猜")
    private String description;

    // TODO 芋艿：后面删除
//    @Schema(description = "销售状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
//    @NotNull(message = "销售状态不能为空")
//    private Integer status;

    @Schema(description = "ERP 销售订单明细列表")
    private List<Item> salesOrderItems;

    @Schema(description = "管理后台 - ERP 销售订单明细新增/修改 Request VO")
    @Data
    public class Item {

        @Schema(description = "编号", example = "20704")
        private Long id;

        // TODO 芋艿：后面删除
//        @Schema(description = "销售订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30765")
//        @NotNull(message = "销售订单编号不能为空")
//        private Long orderId;

//        @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5574")
//        @NotNull(message = "商品 SPU 编号不能为空")
//        private Long productSpuId;

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21273")
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long productSkuId;

        @Schema(description = "商品单位", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "商品单位不能为空")
        private String productUnit;

        @Schema(description = "商品单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "6897")
        @NotNull(message = "商品单价不能为空")
        private BigDecimal productPrice;

        @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "22100")
        @NotNull(message = "数量不能为空")
        private Integer count;

        // TODO 芋艿：后面删除
//        @Schema(description = "总价", requiredMode = Schema.RequiredMode.REQUIRED, example = "26868")
//        @NotNull(message = "总价不能为空")
//        private BigDecimal totalPrice;

        @Schema(description = "备注", example = "你说的对")
        private String description;

        @Schema(description = "税率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "税率，百分比不能为空")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "15791")
        @NotNull(message = "税额，单位：元不能为空")
        private BigDecimal taxPrice;

        // TODO 芋艿：后面删除
//        @Schema(description = "支付金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "21930")
//        @NotNull(message = "支付金额，单位：元不能为空")
//        private BigDecimal payPrice;

    }

}