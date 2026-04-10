package cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 发货通知单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmSalesNoticeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "通知单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SN202603010001")
    @ExcelProperty("通知单编码")
    private String noticeCode;

    @Schema(description = "通知单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试发货通知")
    @ExcelProperty("通知单名称")
    private String noticeName;

    @Schema(description = "销售订单编号", example = "SO202603010001")
    @ExcelProperty("销售订单编号")
    private String salesOrderCode;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long clientId;

    @Schema(description = "客户编码", example = "C001")
    @ExcelProperty("客户编码")
    private String clientCode;

    @Schema(description = "客户名称", example = "某客户")
    @ExcelProperty("客户名称")
    private String clientName;

    @Schema(description = "发货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("发货日期")
    private LocalDateTime salesDate;

    @Schema(description = "收货人", example = "张三")
    @ExcelProperty("收货人")
    private String recipientName;

    @Schema(description = "联系方式", example = "13800138000")
    @ExcelProperty("联系方式")
    private String recipientTelephone;

    @Schema(description = "收货地址", example = "北京市朝阳区xxx")
    @ExcelProperty("收货地址")
    private String recipientAddress;

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
