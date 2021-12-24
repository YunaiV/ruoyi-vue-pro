package cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("租户更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysTenantUpdateReqVO extends SysTenantBaseVO {

    @ApiModelProperty(value = "租户编号", required = true, example = "1024")
    @NotNull(message = "租户编号不能为空")
    private Long id;

}
