package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
* 租户套餐 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class TenantPackageBaseVO {

    @ApiModelProperty(value = "套餐名", required = true, example = "VIP")
    @NotNull(message = "套餐名不能为空")
    private String name;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "好")
    private String remark;

    @ApiModelProperty(value = "关联的菜单编号", required = true)
    @NotNull(message = "关联的菜单编号不能为空")
    private Set<Long> menuIds;

}
