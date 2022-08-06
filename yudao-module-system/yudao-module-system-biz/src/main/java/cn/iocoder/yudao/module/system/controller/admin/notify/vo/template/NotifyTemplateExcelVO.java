package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 站内信模版 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class NotifyTemplateExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("模版编码")
    private String code;

    @ExcelProperty("模版标题")
    private String title;

    @ExcelProperty("模版内容")
    private String content;

    @ExcelProperty(value = "状态：1-启用 0-禁用", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private String status;

    @ExcelProperty("备注")
    private String remarks;

    @ExcelProperty("创建时间")
    private Date createTime;

}
