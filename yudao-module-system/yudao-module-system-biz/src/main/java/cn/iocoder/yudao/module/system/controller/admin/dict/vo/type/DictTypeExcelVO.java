package cn.iocoder.yudao.module.system.controller.admin.dict.vo.type;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 字典类型 Excel 导出响应 VO
 */
@Data
public class DictTypeExcelVO {

    @ExcelProperty("字典主键")
    private Long id;

    @ExcelProperty("字典名称")
    private String name;

    @ExcelProperty("字典类型")
    private String type;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
