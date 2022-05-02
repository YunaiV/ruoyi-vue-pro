package cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* API 错误日志 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ApiErrorLogBaseVO {

    @ApiModelProperty(value = "链路追踪编号", required = true, example = "66600cb6-7852-11eb-9439-0242ac130002")
    @NotNull(message = "链路追踪编号不能为空")
    private String traceId;

    @ApiModelProperty(value = "用户编号", required = true, example = "666")
    @NotNull(message = "用户编号不能为空")
    private Integer userId;

    @ApiModelProperty(value = "用户类型", required = true, example = "1")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @ApiModelProperty(value = "应用名", required = true, example = "dashboard")
    @NotNull(message = "应用名不能为空")
    private String applicationName;

    @ApiModelProperty(value = "请求方法名", required = true, example = "GET")
    @NotNull(message = "请求方法名不能为空")
    private String requestMethod;

    @ApiModelProperty(value = "请求地址", required = true, example = "/xx/yy")
    @NotNull(message = "请求地址不能为空")
    private String requestUrl;

    @ApiModelProperty(value = "请求参数", required = true)
    @NotNull(message = "请求参数不能为空")
    private String requestParams;

    @ApiModelProperty(value = "用户 IP", required = true, example = "127.0.0.1")
    @NotNull(message = "用户 IP不能为空")
    private String userIp;

    @ApiModelProperty(value = "浏览器 UA", required = true, example = "Mozilla/5.0")
    @NotNull(message = "浏览器 UA不能为空")
    private String userAgent;

    @ApiModelProperty(value = "异常发生时间", required = true)
    @NotNull(message = "异常发生时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date exceptionTime;

    @ApiModelProperty(value = "异常名", required = true)
    @NotNull(message = "异常名不能为空")
    private String exceptionName;

    @ApiModelProperty(value = "异常导致的消息", required = true)
    @NotNull(message = "异常导致的消息不能为空")
    private String exceptionMessage;

    @ApiModelProperty(value = "异常导致的根消息", required = true)
    @NotNull(message = "异常导致的根消息不能为空")
    private String exceptionRootCauseMessage;

    @ApiModelProperty(value = "异常的栈轨迹", required = true)
    @NotNull(message = "异常的栈轨迹不能为空")
    private String exceptionStackTrace;

    @ApiModelProperty(value = "异常发生的类全名", required = true)
    @NotNull(message = "异常发生的类全名不能为空")
    private String exceptionClassName;

    @ApiModelProperty(value = "异常发生的类文件", required = true)
    @NotNull(message = "异常发生的类文件不能为空")
    private String exceptionFileName;

    @ApiModelProperty(value = "异常发生的方法名", required = true)
    @NotNull(message = "异常发生的方法名不能为空")
    private String exceptionMethodName;

    @ApiModelProperty(value = "异常发生的方法所在行", required = true)
    @NotNull(message = "异常发生的方法所在行不能为空")
    private Integer exceptionLineNumber;

    @ApiModelProperty(value = "处理状态", required = true, example = "0")
    @NotNull(message = "处理状态不能为空")
    private Integer processStatus;

}
