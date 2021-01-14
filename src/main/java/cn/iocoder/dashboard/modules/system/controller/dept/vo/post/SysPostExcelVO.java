package cn.iocoder.dashboard.modules.system.controller.dept.vo.post;

import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import cn.iocoder.dashboard.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import static cn.iocoder.dashboard.modules.system.enums.dict.DictTypeEnum.SYS_COMMON_STATUS;

/**
 * 岗位 Excel 导出响应 VO
 */
@Data
public class SysPostExcelVO {

    @ExcelProperty("岗位序号")
    private Long id;

    @ExcelProperty("岗位编码")
    private String code;

    @ExcelProperty("岗位名称")
    private String name;

    @ExcelProperty("岗位排序")
    private String sort;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(SYS_COMMON_STATUS)
    private String status;

}
