package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 短剧项目分页 Request VO")
@Data
public class AiDramaProjectPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "项目名称，模糊匹配", example = "地铁")
    private String name;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "成片状态", example = "40")
    private Integer composeStatus;

    @Schema(description = "是否公开", example = "true")
    private Boolean publicStatus;

    @Schema(description = "画幅", example = "9:16")
    private String aspectRatio;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
