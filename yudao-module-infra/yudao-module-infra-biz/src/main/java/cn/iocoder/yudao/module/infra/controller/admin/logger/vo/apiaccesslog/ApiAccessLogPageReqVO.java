package cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - API 访问日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiAccessLogPageReqVO extends PageParam {

    @Schema(title = "用户编号", example = "666")
    private Long userId;

    @Schema(title = "用户类型", example = "2")
    private Integer userType;

    @Schema(title = "应用名", example = "dashboard")
    private String applicationName;

    @Schema(title = "请求地址", example = "/xxx/yyy", description = "模糊匹配")
    private String requestUrl;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "开始请求时间")
    private LocalDateTime[] beginTime;

    @Schema(title = "执行时长", example = "100", description = "大于等于，单位：毫秒")
    private Integer duration;

    @Schema(title = "结果码", example = "0")
    private Integer resultCode;

}
