package cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;

@ApiModel("管理后台 - 文本模板 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxTextTemplateRespVO extends WxTextTemplateBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Integer id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
