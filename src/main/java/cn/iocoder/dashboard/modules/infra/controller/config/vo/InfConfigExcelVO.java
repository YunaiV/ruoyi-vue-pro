package cn.iocoder.dashboard.modules.infra.controller.config.vo;

import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import cn.iocoder.dashboard.framework.excel.core.convert.DictConvert;
import cn.iocoder.dashboard.modules.system.enums.dict.SysDictTypeEnum;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 参数配置 Excel 导出响应 VO
 */
@Data
public class InfConfigExcelVO {

    @ExcelProperty("参数配置序号")
    private Long id;

    @ExcelProperty("参数键名")
    private String key;

    @ExcelProperty("参数分组")
    private String group;

    @ExcelProperty("参数名称")
    private String name;

    @ExcelProperty("参数键值")
    private String value;

    @ExcelProperty(value = "参数类型", converter = DictConvert.class)
    @DictFormat(SysDictTypeEnum.SYS_CONFIG_TYPE)
    private Integer type;

    @ExcelProperty(value = "是否敏感", converter = DictConvert.class)
    @DictFormat(SysDictTypeEnum.SYS_BOOLEAN_STRING)
    private Boolean sensitive;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
