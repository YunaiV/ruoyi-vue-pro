package cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 图文消息文章列表表  Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxNewsArticleItemExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty("摘要")
    private String digest;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty("是否展示封面图片（0/1）")
    private String showCoverPic;

    @ExcelProperty("上传微信，封面图片标识")
    private String thumbMediaId;

    @ExcelProperty("内容")
    private String content;

    @ExcelProperty("内容链接")
    private String contentSourceUrl;

    @ExcelProperty("文章排序")
    private Integer orderNo;

    @ExcelProperty("图片路径")
    private String picPath;

    @ExcelProperty("是否可以留言")
    private String needOpenComment;

    @ExcelProperty("是否仅粉丝可以留言")
    private String onlyFansCanComment;

    @ExcelProperty("图文ID")
    private String newsId;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("创建时间")
    private Date createTime;

}
