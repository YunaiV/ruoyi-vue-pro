package cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 到货通知单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmArrivalNoticeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "通知单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "AN2026020001")
    @ExcelProperty("通知单编码")
    private String code;

    @Schema(description = "通知单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2 月份钢板到货")
    @ExcelProperty("通知单名称")
    private String name;

    @Schema(description = "采购订单编号", example = "PO20260101")
    @ExcelProperty("采购订单编号")
    private String purchaseOrderCode;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long vendorId;

    @Schema(description = "供应商编码", example = "V001")
    @ExcelProperty("供应商编码")
    private String vendorCode;

    @Schema(description = "供应商名称", example = "某供应商")
    @ExcelProperty("供应商名称")
    private String vendorName;

    @Schema(description = "到货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("到货日期")
    private LocalDateTime arrivalDate;

    @Schema(description = "联系人", example = "张三")
    @ExcelProperty("联系人")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    @ExcelProperty("联系电话")
    private String contactTelephone;

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
