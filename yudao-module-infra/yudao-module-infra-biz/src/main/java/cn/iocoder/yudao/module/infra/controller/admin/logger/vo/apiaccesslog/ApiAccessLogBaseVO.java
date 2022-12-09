package cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* API 访问日志 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ApiAccessLogBaseVO {

    @Schema(title = "链路追踪编号", required = true, example = "66600cb6-7852-11eb-9439-0242ac130002")
    @NotNull(message = "链路追踪编号不能为空")
    private String traceId;

    @Schema(title = "用户编号", required = true, example = "666")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(title = "用户类型", required = true, example = "2", description = "参见 UserTypeEnum 枚举")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @Schema(title = "应用名", required = true, example = "dashboard")
    @NotNull(message = "应用名不能为空")
    private String applicationName;

    @Schema(title = "请求方法名", required = true, example = "GET")
    @NotNull(message = "请求方法名不能为空")
    private String requestMethod;

    @Schema(title = "请求地址", required = true, example = "/xxx/yyy")
    @NotNull(message = "请求地址不能为空")
    private String requestUrl;

    @Schema(title = "请求参数")
    private String requestParams;

    @Schema(title = "用户 IP", required = true, example = "127.0.0.1")
    @NotNull(message = "用户 IP不能为空")
    private String userIp;

    @Schema(title = "浏览器 UA", required = true, example = "Mozilla/5.0")
    @NotNull(message = "浏览器 UA不能为空")
    private String userAgent;

    @Schema(title = "开始请求时间", required = true)
    @NotNull(message = "开始请求时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime beginTime;

    @Schema(title = "结束请求时间", required = true)
    @NotNull(message = "结束请求时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(title = "执行时长", required = true, example = "100")
    @NotNull(message = "执行时长不能为空")
    private Integer duration;

    @Schema(title = "结果码", required = true, example = "0")
    @NotNull(message = "结果码不能为空")
    private Integer resultCode;

    @Schema(title = "结果提示", example = "芋道源码，牛逼！")
    private String resultMsg;

}
