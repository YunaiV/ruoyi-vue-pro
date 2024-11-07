package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 绘画分页 Request VO")
@Data
public class AiImagePageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "28987")
    private Long userId;

    @Schema(description = "平台", example = "OpenAI")
    private String platform;

    @Schema(description = "提示词", example = "1")
    private String prompt;

    @Schema(description = "绘画状态", example = "1")
    private Integer status;

    @Schema(description = "是否发布", example = "1")
    private Boolean publicStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}