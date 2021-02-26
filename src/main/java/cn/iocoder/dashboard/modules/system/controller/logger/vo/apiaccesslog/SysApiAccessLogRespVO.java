package cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("API 访问日志 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysApiAccessLogRespVO extends SysApiAccessLogBaseVO {

    @ApiModelProperty(value = "日志主键", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
