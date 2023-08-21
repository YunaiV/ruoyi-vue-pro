package cn.iocoder.yudao.module.member.controller.admin.level.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author owen
 */
@Schema(description = "管理后台 - 会员等级记录 Excel 导出 Request VO，参数和 MemberLevelLogPageReqVO 是一致的")
@Data
public class MemberLevelLogExportReqVO {

    @Schema(description = "用户编号", example = "25923")
    private Long userId;

    @Schema(description = "等级编号", example = "25985")
    private Long levelId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
