package cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;

@ApiModel("管理后台 - 图文消息模板 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxNewsTemplateRespVO extends WxNewsTemplateBaseVO {

    @ApiModelProperty(value = "主键 主键ID", required = true)
    private Integer id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
