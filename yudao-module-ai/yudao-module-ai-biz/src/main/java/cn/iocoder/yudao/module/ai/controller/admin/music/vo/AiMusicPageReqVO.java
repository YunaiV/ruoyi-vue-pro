package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 音乐分页 Request VO")
@Data
public class AiMusicPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "12212")
    private Long userId;

    @Schema(description = "音乐名称", example = "夜空中最亮的星")
    private String title;

    @Schema(description = "音乐状态", example = "20")
    @InEnum(AiMusicStatusEnum.class)
    private Integer status;

    @Schema(description = "生成模式", example = "1")
    @InEnum(AiMusicGenerateModeEnum.class)
    private Integer generateMode;

    @Schema(description = "是否发布", example = "true")
    private Boolean publicStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}