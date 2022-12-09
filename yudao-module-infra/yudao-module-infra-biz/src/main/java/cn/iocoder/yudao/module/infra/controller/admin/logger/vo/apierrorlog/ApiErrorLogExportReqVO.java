package cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - API 错误日志 Excel 导出 Request VO", description = "参数和 ApiErrorLogPageReqVO 是一致的")
@Data
public class ApiErrorLogExportReqVO {

    @Schema(title = "用户编号", example = "666")
    private Long userId;

    @Schema(title = "用户类型", example = "1")
    private Integer userType;

    @Schema(title = "应用名", example = "dashboard")
    private String applicationName;

    @Schema(title = "请求地址", example = "/xx/yy")
    private String requestUrl;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "异常发生时间")
    private LocalDateTime[] exceptionTime;

    @Schema(title = "处理状态", example = "0")
    private Integer processStatus;

}
