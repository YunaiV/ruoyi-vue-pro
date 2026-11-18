package cn.iocoder.yudao.module.ai.controller.admin.video.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoStatusEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 视频分页 Request VO")
@Data
public class AiVideoPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "创作类型", example = "1")
    @InEnum(AiVideoTypeEnum.class)
    private Integer type;

    @Schema(description = "平台", example = "MINIMAX")
    private String platform;

    @Schema(description = "状态", example = "40")
    @InEnum(AiVideoStatusEnum.class)
    private Integer status;

    @Schema(description = "是否公开", example = "true")
    private Boolean publicStatus;

    @Schema(description = "提示词，模糊匹配", example = "橘猫")
    private String prompt;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
