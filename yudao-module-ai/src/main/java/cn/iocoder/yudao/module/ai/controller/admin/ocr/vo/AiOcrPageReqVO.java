package cn.iocoder.yudao.module.ai.controller.admin.ocr.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.ocr.AiOcrStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - AI 文字识别分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 文字识别分页 Request VO")
@Data
public class AiOcrPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "平台", example = "TENCENT")
    private String platform;

    @Schema(description = "识别状态，参见 AiOcrStatusEnum 枚举", example = "20")
    @InEnum(AiOcrStatusEnum.class)
    private Integer status;

    @Schema(description = "图片文件名，模糊匹配", example = "invoice")
    private String fileName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
