package cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 图文消息文章列表表 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxNewsArticleItemPageReqVO extends PageParam {

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

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
