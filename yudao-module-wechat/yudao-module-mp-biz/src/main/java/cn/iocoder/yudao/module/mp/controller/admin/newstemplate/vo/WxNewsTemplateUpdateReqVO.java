package cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo;

import lombok.*;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@ApiModel("管理后台 - 图文消息模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxNewsTemplateUpdateReqVO extends WxNewsTemplateBaseVO {

    @ApiModelProperty(value = "主键 主键ID", required = true)
    @NotNull(message = "主键 主键ID不能为空")
    private Integer id;

}
