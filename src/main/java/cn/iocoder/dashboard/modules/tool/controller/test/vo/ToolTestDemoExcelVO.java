package cn.iocoder.dashboard.modules.tool.controller.test.vo;

import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

import static cn.iocoder.dashboard.modules.system.enums.dict.SysDictTypeEnum.*;

/**
 * 字典类型 Excel VO
 *
 * @author 芋艿
 */
@Data
public class ToolTestDemoExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("名字")
    private String name;

    @ExcelProperty("状态")
    @DictFormat(SYS_COMMON_STATUS)
    private Integer status;

    @ExcelProperty("类型")
    @DictFormat(SYS_OPERATE_TYPE)
    private Integer type;

    @ExcelProperty("分类")
    @DictFormat(INF_REDIS_TIMEOUT_TYPE)
    private Integer category;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
