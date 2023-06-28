package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户签到积分 Excel 导出 Request VO，参数和 SignInRecordPageReqVO 是一致的")
@Data
public class MemberSignInRecordExportReqVO {

    @Schema(description = "签到用户", example = "6507")
    private Integer userId;

    @Schema(description = "第几天签到")
    private Integer day;

    @Schema(description = "签到时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
