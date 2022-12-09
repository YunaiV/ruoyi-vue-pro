package cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 敏感词 Excel 导出 Request VO", description = "参数和 SensitiveWordPageReqVO 是一致的")
@Data
public class SensitiveWordExportReqVO {

    @Schema(title = "敏感词", example = "敏感词")
    private String name;

    @Schema(title = "标签", example = "短信,评论")
    private String tag;

    @Schema(title = "状态", example = "1", description = "参见 CommonStatusEnum 枚举类")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "创建时间")
    private LocalDateTime[] createTime;

}
