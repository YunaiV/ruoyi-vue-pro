package cn.iocoder.dashboard.modules.system.controller.user.vo.user;


import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("用户创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserCreateReqVO extends SysUserBaseVO {
}
