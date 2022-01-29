package cn.iocoder.yudao.adminserver.modules.tool.controller.test.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.adminserver.modules.infra.enums.InfDictTypeConstants;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

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
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty(value = "类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.OPERATE_TYPE)
    private Integer type;

    @ExcelProperty(value = "分类", converter = DictConvert.class)
    @DictFormat(InfDictTypeConstants.REDIS_TIMEOUT_TYPE)
    private Integer category;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
