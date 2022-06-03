package cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 微信素材上传表  Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxMediaUploadExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("图片URL")
    private String url;

    @ExcelProperty("素材ID")
    private String mediaId;

    @ExcelProperty("缩略图素材ID")
    private String thumbMediaId;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("创建时间")
    private Date createTime;

}
