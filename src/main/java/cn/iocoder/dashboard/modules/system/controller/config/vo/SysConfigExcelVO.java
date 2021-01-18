package cn.iocoder.dashboard.modules.system.controller.config.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 参数配置 Excel 导出响应 VO
 */
@Data
public class SysConfigExcelVO {

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

    @ExcelProperty("参数类型")
    private String type;

    @ExcelProperty("是否敏感")
    private Boolean sensitive;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
