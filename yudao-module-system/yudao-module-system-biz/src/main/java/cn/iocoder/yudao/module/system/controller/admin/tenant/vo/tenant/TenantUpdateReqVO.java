package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 租户更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantUpdateReqVO extends TenantBaseVO {

    @ApiModelProperty(value = "租户编号", required = true, example = "1024")
    @NotNull(message = "租户编号不能为空")
    private Long id;

}
