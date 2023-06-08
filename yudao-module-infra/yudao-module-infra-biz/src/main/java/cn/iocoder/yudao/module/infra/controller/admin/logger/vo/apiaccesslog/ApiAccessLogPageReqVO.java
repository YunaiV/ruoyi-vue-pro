package cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - API 访问日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiAccessLogPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "666")
    private Long userId;

    @Schema(description = "用户类型", example = "2")
    private Integer userType;

    @Schema(description = "应用名", example = "dashboard")
    private String applicationName;

    @Schema(description = "请求地址,模糊匹配", example = "/xxx/yyy")
    private String requestUrl;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "开始请求时间")
    private LocalDateTime[] beginTime;

    @Schema(description = "执行时长,大于等于，单位：毫秒", example = "100")
    private Integer duration;

    @Schema(description = "结果码", example = "0")
    private Integer resultCode;

}
