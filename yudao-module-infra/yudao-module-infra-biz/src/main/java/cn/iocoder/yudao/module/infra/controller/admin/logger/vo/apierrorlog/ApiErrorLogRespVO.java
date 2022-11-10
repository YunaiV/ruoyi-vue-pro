package cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - API 错误日志 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiErrorLogRespVO extends ApiErrorLogBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Integer id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "处理时间", required = true)
    private LocalDateTime processTime;

    @ApiModelProperty(value = "处理用户编号", example = "233")
    private Integer processUserId;

}
