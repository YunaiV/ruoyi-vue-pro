package cn.iocoder.yudao.module.tool.controller.admin.test.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 测试示例 Excel VO
 *
 * @author 芋艿
 */
@Data
public class TestDemoExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("名字")
    private String name;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("")
    private Integer status;

    @ExcelProperty(value = "类型", converter = DictConvert.class)
    @DictFormat("sys_common_status")
    private Integer type;

    @ExcelProperty(value = "分类", converter = DictConvert.class)
    @DictFormat("inf_redis_timeout_type")
    private Integer category;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
