package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 会员注册数量 Response VO")
@Data
public class MemberRegisterCountRespVO {

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY, timezone = TIME_ZONE_DEFAULT)
    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private LocalDate date;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer count;

}
