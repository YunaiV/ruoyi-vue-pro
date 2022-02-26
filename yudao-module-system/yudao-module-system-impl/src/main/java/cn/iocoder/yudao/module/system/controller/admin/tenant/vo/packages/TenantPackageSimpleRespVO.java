package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 租户套餐精简 Response VO")
@Data
public class TenantPackageSimpleRespVO {

    @ApiModelProperty(value = "套餐编号", required = true, example = "1024")
    @NotNull(message = "套餐编号不能为空")
    private Long id;

    @ApiModelProperty(value = "套餐名", required = true, example = "VIP")
    @NotNull(message = "套餐名不能为空")
    private String name;

}
