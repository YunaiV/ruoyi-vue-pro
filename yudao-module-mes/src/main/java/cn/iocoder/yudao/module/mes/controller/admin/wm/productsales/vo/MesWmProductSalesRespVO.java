package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 销售出库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmProductSalesRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "出库单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PS2026030001")
    @ExcelProperty("出库单号")
    private String code;

    @Schema(description = "出库单名称", example = "产品出库单")
    @ExcelProperty("出库单名称")
    private String name;

    @Schema(description = "客户ID", example = "1")
    private Long clientId;

    @Schema(description = "客户名称", example = "某客户")
    @ExcelProperty("客户")
    private String clientName;

    @Schema(description = "销售订单号", example = "SO2026030001")
    @ExcelProperty("销售订单号")
    private String salesOrderCode;

    @Schema(description = "出库日期")
    @ExcelProperty("出库日期")
    private LocalDateTime salesDate;

    @Schema(description = "发货通知单ID", example = "1")
    private Long noticeId;

    @Schema(description = "发货通知单编号", example = "SN2026030001")
    @ExcelProperty("发货通知单编号")
    private String noticeCode;

    @Schema(description = "客户编码", example = "C001")
    @ExcelProperty("客户编码")
    private String clientCode;

    @Schema(description = "收货人", example = "张三")
    @ExcelProperty("收货人")
    private String contactName;

    @Schema(description = "联系方式", example = "13800138000")
    @ExcelProperty("联系方式")
    private String contactTelephone;

    @Schema(description = "收货地址", example = "北京市朝阳区XX路XX号")
    @ExcelProperty("收货地址")
    private String contactAddress;

    @Schema(description = "承运商", example = "顺丰快递")
    @ExcelProperty("承运商")
    private String carrier;

    @Schema(description = "运输单号", example = "SF1234567890")
    @ExcelProperty("运输单号")
    private String shippingNumber;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
