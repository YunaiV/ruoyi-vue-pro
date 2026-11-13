package cn.iocoder.yudao.module.ai.controller.admin.speech.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.speech.AiSpeechStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - AI 语音分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 语音分页 Request VO")
@Data
public class AiSpeechPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "平台", example = "MINIMAX")
    private String platform;

    @Schema(description = "状态，参见 AiSpeechStatusEnum 枚举", example = "20")
    @InEnum(AiSpeechStatusEnum.class)
    private Integer status;

    @Schema(description = "朗读文本，模糊匹配", example = "欢迎")
    private String inputText;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
