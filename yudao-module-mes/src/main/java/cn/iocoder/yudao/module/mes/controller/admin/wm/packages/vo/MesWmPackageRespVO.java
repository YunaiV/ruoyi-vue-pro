package cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 装箱单 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 装箱单 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class MesWmPackageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "装箱单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PKG20260307001")
    @ExcelProperty("装箱单编号")
    private String code;

    @Schema(description = "父箱 ID", example = "0")
    private Long parentId;

    @Schema(description = "装箱日期")
    @ExcelProperty("装箱日期")
    private LocalDateTime packageDate;

    @Schema(description = "销售订单编号", example = "SO20260301")
    @ExcelProperty("销售订单编号")
    private String salesOrderCode;

    @Schema(description = "发票编号", example = "INV20260301")
    @ExcelProperty("发票编号")
    private String invoiceCode;

    @Schema(description = "客户 ID", example = "1")
    private Long clientId;

    @Schema(description = "客户编码", example = "C001")
    @ExcelProperty("客户编码")
    private String clientCode;

    @Schema(description = "客户名称", example = "某客户")
    @ExcelProperty("客户名称")
    private String clientName;

    @Schema(description = "客户简称", example = "某客户简称")
    private String clientNickname;

    @Schema(description = "箱长度", example = "60.00")
    @ExcelProperty("箱长度")
    private BigDecimal length;

    @Schema(description = "箱宽度", example = "40.00")
    @ExcelProperty("箱宽度")
    private BigDecimal width;

    @Schema(description = "箱高度", example = "30.00")
    @ExcelProperty("箱高度")
    private BigDecimal height;

    @Schema(description = "尺寸单位 ID", example = "1")
    private Long sizeUnitId;

    @Schema(description = "尺寸单位名称", example = "厘米")
    @ExcelProperty("尺寸单位")
    private String sizeUnitName;

    @Schema(description = "净重", example = "10.00")
    @ExcelProperty("净重")
    private BigDecimal netWeight;

    @Schema(description = "毛重", example = "12.00")
    @ExcelProperty("毛重")
    private BigDecimal grossWeight;

    @Schema(description = "重量单位 ID", example = "2")
    private Long weightUnitId;

    @Schema(description = "重量单位名称", example = "千克")
    @ExcelProperty("重量单位")
    private String weightUnitName;

    @Schema(description = "检查员用户 ID", example = "1")
    private Long inspectorUserId;

    @Schema(description = "检查员姓名", example = "张三")
    @ExcelProperty("检查员")
    private String inspectorName;

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
