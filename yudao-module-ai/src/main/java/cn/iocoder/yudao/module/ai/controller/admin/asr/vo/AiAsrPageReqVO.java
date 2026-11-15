package cn.iocoder.yudao.module.ai.controller.admin.asr.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.asr.AiAsrStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 语音识别分页 Request VO")
@Data
public class AiAsrPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "平台", example = "TENCENT")
    private String platform;

    @Schema(description = "识别状态，参见 AiAsrStatusEnum 枚举", example = "20")
    @InEnum(AiAsrStatusEnum.class)
    private Integer status;

    @Schema(description = "音频文件名，模糊匹配", example = "meeting")
    private String fileName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
