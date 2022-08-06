package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 站内信模版 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyTemplateRespVO extends NotifyTemplateBaseVO {

    @ApiModelProperty(value = "ID", required = true)
    private Long id;

    @ApiModelProperty(value = "参数数组", example = "name,code")
    private List<String> params;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
