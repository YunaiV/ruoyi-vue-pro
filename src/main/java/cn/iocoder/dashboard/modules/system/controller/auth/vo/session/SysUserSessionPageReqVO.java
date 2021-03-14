package cn.iocoder.dashboard.modules.system.controller.auth.vo.session;

import cn.iocoder.dashboard.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@ApiModel("在线用户 Session 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserSessionPageReqVO extends PageParam {

    @ApiModelProperty(value = "用户 IP", example = "127.0.0.1", notes = "模糊匹配")
    private String userIp;

    @ApiModelProperty(value = "用户账号", example = "yudao", notes = "模糊匹配")
    private String username;

}
