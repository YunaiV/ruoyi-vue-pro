package cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备台账 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MesDvMachineryImportExcelVO {

    @ExcelProperty("设备编码")
    private String code;

    @ExcelProperty("设备名称")
    private String name;

    @ExcelProperty("品牌")
    private String brand;

    @ExcelProperty("规格型号")
    private String specification;

    @ExcelProperty("设备类型编码")
    private String machineryTypeCode;

    @ExcelProperty("所属车间编码")
    private String workshopCode;

    @ExcelProperty(value = "设备状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_MACHINERY_STATUS)
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}
