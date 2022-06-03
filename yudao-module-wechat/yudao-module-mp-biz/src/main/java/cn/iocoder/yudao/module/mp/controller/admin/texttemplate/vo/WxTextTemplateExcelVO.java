package cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 文本模板 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxTextTemplateExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("模板名字")
    private String tplName;

    @ExcelProperty("模板内容")
    private String content;

    @ExcelProperty("创建时间")
    private Date createTime;

}
