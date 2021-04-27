package cn.iocoder.dashboard.modules.system.controller.dict.vo.type;

import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import cn.iocoder.dashboard.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import static cn.iocoder.dashboard.modules.system.enums.dict.SysDictTypeEnum.SYS_COMMON_STATUS;

/**
 * 字典类型 Excel 导出响应 VO
 */
@Data
public class SysDictTypeExcelVO {

    @ExcelProperty("字典主键")
    private Long id;

    @ExcelProperty("字典名称")
    private String name;

    @ExcelProperty("字典类型")
    private String type;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(SYS_COMMON_STATUS)
    private Integer status;

}
