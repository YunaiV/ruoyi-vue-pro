package cn.iocoder.dashboard.modules.system.controller.permission.vo.role;

import cn.iocoder.dashboard.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("角色分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuPageReqVO extends PageParam {

}
