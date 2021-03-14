package cn.iocoder.dashboard.modules.tool.controller.test.vo;

import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import cn.iocoder.dashboard.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

import static cn.iocoder.dashboard.modules.system.enums.dict.SysDictTypeEnum.*;

/**
 * 测试示例 Excel VO
 *
 * @author 芋艿
 */
@Data
public class ToolTestDemoExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("名字")
    private String name;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(SYS_COMMON_STATUS)
    private Integer status;

    @ExcelProperty(value = "类型", converter = DictConvert.class)
    @DictFormat(SYS_OPERATE_TYPE)
    private Integer type;

    @ExcelProperty(value = "分类", converter = DictConvert.class)
    @DictFormat(INF_REDIS_TIMEOUT_TYPE)
    private Integer category;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
