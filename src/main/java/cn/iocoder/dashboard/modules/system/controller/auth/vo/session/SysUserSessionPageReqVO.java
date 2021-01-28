package cn.iocoder.dashboard.modules.system.controller.auth.vo.session;

import cn.iocoder.dashboard.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("在线用户 Session 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserSessionPageReqVO extends PageParam {
}
