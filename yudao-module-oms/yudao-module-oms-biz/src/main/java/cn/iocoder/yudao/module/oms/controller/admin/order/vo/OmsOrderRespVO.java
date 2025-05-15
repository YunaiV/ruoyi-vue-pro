package cn.iocoder.yudao.module.oms.controller.admin.order.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OMS订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OmsOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "16261")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "所属平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("所属平台")
    private String platformCode;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单号")
    private String code;

    @Schema(description = "外部来源号，即平台订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部来源号，即平台订单号")
    private String externalCode;

    @Schema(description = "店铺id", example = "8058")
    @ExcelProperty("店铺id")
    private Long shopId;

    @Schema(description = "运费")
    @ExcelProperty("运费")
    private BigDecimal shippingFee;

    @Schema(description = "总金额", example = "10077")
    @ExcelProperty("总金额")
    private BigDecimal totalPrice;

    @Schema(description = "买家姓名", example = "李四")
    @ExcelProperty("买家姓名")
    private String buyerName;

    @Schema(description = "邮箱")
    @ExcelProperty("邮箱")
    private String email;

    @Schema(description = "最晚出库时间")
    @ExcelProperty("最晚出库时间")
    private LocalDateTime outboundLatestTime;

    @Schema(description = "最晚到货时间")
    @ExcelProperty("最晚到货时间")
    private LocalDateTime receiveLatestTime;

    @Schema(description = "订单创建时间")
    @ExcelProperty("订单创建时间")
    private LocalDateTime orderCreateTime;

    @Schema(description = "付款时间")
    @ExcelProperty("付款时间")
    private LocalDateTime payTime;

    @Schema(description = "电话")
    @ExcelProperty("电话")
    private String phone;

    @Schema(description = "公司名", example = "张三")
    @ExcelProperty("公司名")
    private String companyName;

    @Schema(description = "收件人国家")
    @ExcelProperty("收件人国家")
    private String recipientCountryCode;

    @Schema(description = "收件人省【或为州】")
    @ExcelProperty("收件人省【或为州】")
    private String state;

    @Schema(description = "城市")
    @ExcelProperty("城市")
    private String city;

    @Schema(description = "区/县")
    @ExcelProperty("区/县")
    private String district;

    @Schema(description = "外部来源原地址，用作备份")
    @ExcelProperty("外部来源原地址，用作备份")
    private String externalAddress;

    @Schema(description = "地址1")
    @ExcelProperty("地址1")
    private String address1;

    @Schema(description = "地址2")
    @ExcelProperty("地址2")
    private String address2;

    @Schema(description = "地址3")
    @ExcelProperty("地址3")
    private String address3;

    @Schema(description = "门牌号")
    @ExcelProperty("门牌号")
    private String houseNo;

    @Schema(description = "邮编")
    @ExcelProperty("邮编")
    private String postalCode;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "产品信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品信息")
    private String productNames;


    @Schema(description = "所属店铺", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("所属店铺")
    private String shopName;

    @Data
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "产品单位单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productUnitId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal price;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "产品数量不能为空")
        private Integer qty;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "100.00")
        private BigDecimal taxPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

        // ========== 销售出库 ==========

        @Schema(description = "销售出库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal outCount;

        // ========== 销售退货（入库）） ==========

        @Schema(description = "销售退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal returnCount;

        // ========== 关联字段 ==========

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "巧克力")
        private String productName;
        @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "A9985")
        private String productBarCode;
        @Schema(description = "产品单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "盒")
        private String productUnitName;

        @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal stockCount; // 该字段仅仅在“详情”和“编辑”时使用

    }

}