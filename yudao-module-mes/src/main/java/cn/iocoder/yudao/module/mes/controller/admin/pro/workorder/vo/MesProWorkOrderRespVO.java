package cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产工单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesProWorkOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "工单编码", example = "WO-001")
    @ExcelProperty("工单编码")
    private String code;

    @Schema(description = "工单名称", example = "生产工单-A")
    @ExcelProperty("工单名称")
    private String name;

    @Schema(description = "工单类型", example = "1")
    @ExcelProperty(value = "工单类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_WORK_ORDER_TYPE)
    private Integer type;

    @Schema(description = "来源类型", example = "1")
    @ExcelProperty(value = "来源类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_WORK_ORDER_SOURCE_TYPE)
    private Integer orderSourceType;

    @Schema(description = "来源单据编号", example = "SO-001")
    @ExcelProperty("来源单据编号")
    private String orderSourceCode;

    @Schema(description = "产品编号", example = "100")
    private Long productId;

    @Schema(description = "产品名称", example = "电路板 A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品编码", example = "P-001")
    @ExcelProperty("产品编码")
    private String productCode;

    @Schema(description = "规格型号", example = "100x200mm")
    @ExcelProperty("规格型号")
    private String productSpecification;

    @Schema(description = "单位名称", example = "个")
    @ExcelProperty("单位")
    private String unitMeasureName;

    @Schema(description = "生产数量", example = "100.00")
    @ExcelProperty("生产数量")
    private BigDecimal quantity;

    @Schema(description = "已生产数量", example = "50.00")
    @ExcelProperty("已生产数量")
    private BigDecimal quantityProduced;

    @Schema(description = "调整数量", example = "0")
    @ExcelProperty("调整数量")
    private BigDecimal quantityChanged;

    @Schema(description = "已排产数量", example = "80.00")
    @ExcelProperty("已排产数量")
    private BigDecimal quantityScheduled;

    @Schema(description = "客户编号", example = "300")
    private Long clientId;

    @Schema(description = "客户编码", example = "C-001")
    @ExcelProperty("客户编码")
    private String clientCode;

    @Schema(description = "客户名称", example = "客户 A")
    @ExcelProperty("客户名称")
    private String clientName;

    @Schema(description = "供应商编号", example = "400")
    private Long vendorId;

    @Schema(description = "供应商编码", example = "V-001")
    @ExcelProperty("供应商编码")
    private String vendorCode;

    @Schema(description = "供应商名称", example = "供应商 A")
    @ExcelProperty("供应商名称")
    private String vendorName;

    @Schema(description = "批次号", example = "BATCH-001")
    @ExcelProperty("批次号")
    private String batchCode;

    @Schema(description = "需求日期")
    @ExcelProperty("需求日期")
    private LocalDateTime requestDate;

    @Schema(description = "父工单编号", example = "0")
    private Long parentId;

    @Schema(description = "父工单编码", example = "WO-001")
    private String parentCode;

    @Schema(description = "完成时间")
    @ExcelProperty("完成时间")
    private LocalDateTime finishDate;

    @Schema(description = "取消时间")
    @ExcelProperty("取消时间")
    private LocalDateTime cancelDate;

    @Schema(description = "工单状态", example = "0")
    @ExcelProperty(value = "工单状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_WORK_ORDER_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
