package cn.iocoder.dashboard.modules.system.controller.permission.vo.role;

import cn.iocoder.dashboard.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("角色分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePageReqVO extends PageParam {

    @ApiModelProperty(value = "角色名称", example = "芋道", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "展示状态", example = "1", notes = "参见 SysCommonStatusEnum 枚举类")
    private Integer status;

}
