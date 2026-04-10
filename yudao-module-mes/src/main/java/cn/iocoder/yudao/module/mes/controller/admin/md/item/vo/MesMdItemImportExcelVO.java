package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 物料产品 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MesMdItemImportExcelVO {

    @ExcelProperty("物料编码")
    private String code;

    @ExcelProperty("物料名称")
    private String name;

    @ExcelProperty("规格型号")
    private String specification;

    @ExcelProperty("单位编码")
    private String unitMeasureCode;

    @ExcelProperty("物料分类编号")
    private Long itemTypeId;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty("是否启用安全库存")
    private Boolean safeStockFlag;

    @ExcelProperty("最低库存量")
    private BigDecimal minStock;

    @ExcelProperty("最高库存量")
    private BigDecimal maxStock;

    @ExcelProperty("是否高值物料")
    private Boolean highValue;

    @ExcelProperty("是否启用批次管理")
    private Boolean batchFlag;

    @ExcelProperty("备注")
    private String remark;

}
