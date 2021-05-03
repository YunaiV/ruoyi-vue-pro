package cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("登陆日志 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysLoginLogRespVO extends SysLoginLogBaseVO {

    @ApiModelProperty(value = "日志编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "登陆时间", required = true)
    private Date createTime;

}
