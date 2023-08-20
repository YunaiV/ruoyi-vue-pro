package cn.iocoder.yudao.module.member.controller.admin.level.vo.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author owen
 */
@Schema(description = "管理后台 - 会员经验记录 Excel 导出 Request VO，参数和 MemberExperienceLogPageReqVO 是一致的")
@Data
public class MemberExperienceLogExportReqVO {

    @Schema(description = "用户编号", example = "3638")
    private Long userId;

    @Schema(description = "业务类型", example = "1")
    private Integer bizType;

    @Schema(description = "业务编号", example = "12164")
    private String bizId;

    @Schema(description = "标题", example = "增加经验")
    private String title;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
