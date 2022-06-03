package cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;

@ApiModel("管理后台 - 图文消息文章列表表  Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxNewsArticleItemRespVO extends WxNewsArticleItemBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Integer id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
