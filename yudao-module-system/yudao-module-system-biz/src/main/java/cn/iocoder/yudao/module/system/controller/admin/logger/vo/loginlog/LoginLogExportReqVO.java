package cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 登录日志分页列表 Request VO")
@Data
public class LoginLogExportReqVO {

    @Schema(title = "用户 IP", example = "127.0.0.1", description = "模拟匹配")
    private String userIp;

    @Schema(title = "用户账号", example = "芋道", description = "模拟匹配")
    private String username;

    @Schema(title = "操作状态", example = "true")
    private Boolean status;

    @Schema(title = "登录时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
