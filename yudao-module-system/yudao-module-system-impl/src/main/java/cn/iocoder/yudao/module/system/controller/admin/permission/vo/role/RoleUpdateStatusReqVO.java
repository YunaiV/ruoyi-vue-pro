package cn.iocoder.yudao.module.system.controller.admin.permission.vo.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 角色更新状态 Request VO")
@Data
public class RoleUpdateStatusReqVO {

    @ApiModelProperty(value = "角色编号", required = true, example = "1024")
    @NotNull(message = "角色编号不能为空")
    private Long id;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "见 CommonStatusEnum 枚举")
    @NotNull(message = "状态不能为空")
//    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    private Integer status;

}
