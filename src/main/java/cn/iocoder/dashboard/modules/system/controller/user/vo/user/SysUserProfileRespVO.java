package cn.iocoder.dashboard.modules.system.controller.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;


@ApiModel("用户个人中心信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysUserProfileRespVO extends SysUserRespVO {

    /**
     * 所属角色
     */
    @ApiModelProperty(value = "所属角色", required = true, example = "123456")
    private Set<Role> roles;

    @ApiModel("角色")
    @Data
    public static class Role {

        @ApiModelProperty(value = "角色编号", required = true, example = "1")
        private Long id;

        @ApiModelProperty(value = "角色名称", required = true, example = "普通角色")
        private String name;

    }
}
