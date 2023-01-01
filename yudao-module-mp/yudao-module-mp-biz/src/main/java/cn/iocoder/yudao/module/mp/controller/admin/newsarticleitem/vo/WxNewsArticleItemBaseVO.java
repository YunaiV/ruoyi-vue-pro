package cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 图文消息文章列表表  Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxNewsArticleItemBaseVO {

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "摘要")
    private String digest;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "是否展示封面图片（0/1）")
    private String showCoverPic;

    @ApiModelProperty(value = "上传微信，封面图片标识")
    private String thumbMediaId;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "内容链接")
    private String contentSourceUrl;

    @ApiModelProperty(value = "文章排序")
    private Integer orderNo;

    @ApiModelProperty(value = "图片路径")
    private String picPath;

    @ApiModelProperty(value = "是否可以留言")
    private String needOpenComment;

    @ApiModelProperty(value = "是否仅粉丝可以留言")
    private String onlyFansCanComment;

    @ApiModelProperty(value = "图文ID")
    private String newsId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

}
