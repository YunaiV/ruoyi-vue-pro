package cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 图文消息模板 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxNewsTemplateExcelVO {

    @ExcelProperty("主键 主键ID")
    private Integer id;

    @ExcelProperty("模板名称")
    private String tplName;

    @ExcelProperty("是否已上传微信")
    private String isUpload;

    @ExcelProperty("素材ID")
    private String mediaId;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("创建时间")
    private Date createTime;

}
