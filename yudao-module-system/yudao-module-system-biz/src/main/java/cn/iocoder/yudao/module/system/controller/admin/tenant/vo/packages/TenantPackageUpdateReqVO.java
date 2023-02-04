package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 租户套餐更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantPackageUpdateReqVO extends TenantPackageBaseVO {

    @Schema(description = "套餐编号", required = true, example = "1024")
    @NotNull(message = "套餐编号不能为空")
    private Long id;

}
