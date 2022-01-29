package cn.iocoder.yudao.module.system.controller.tenant.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("租户 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysTenantRespVO extends SysTenantBaseVO {

    @ApiModelProperty(value = "租户编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
