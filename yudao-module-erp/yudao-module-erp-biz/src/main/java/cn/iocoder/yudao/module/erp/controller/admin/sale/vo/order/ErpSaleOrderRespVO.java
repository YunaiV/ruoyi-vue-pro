package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// TODO 芋艿：导出最后搞
@Schema(description = "管理后台 - ERP 销售订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpSaleOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "销售单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XS001")
    @ExcelProperty("销售单编号")
    private String no;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    @ExcelProperty("客户编号")
    private Long customerId;

    @Schema(description = "下单时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("下单时间")
    private LocalDateTime orderTime;

    // TODO 芋艿：example 后面
    @Schema(description = "销售员编号数组")
    @ExcelProperty("销售员编号数组")
    private String salePersonIds;

    @Schema(description = "合计价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "26094")
    @ExcelProperty("合计价格，单位：元")
    private BigDecimal totalPrice;

    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
    @ExcelProperty("优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "44.52")
    @ExcelProperty("优惠金额，单位：元")
    private BigDecimal discountPrice;

    @Schema(description = "支付金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "322.40")
    @ExcelProperty("支付金额，单位：元")
    private BigDecimal payPrice;

    @Schema(description = "定金金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "71.27")
    @ExcelProperty("定金金额，单位：元")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
    @ExcelProperty("附件地址")
    private String fileUrl;

    @Schema(description = "结算账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "311.89")
    @ExcelProperty("结算账户编号")
    private Long accountId;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String description;

    @Schema(description = "销售状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("销售状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}