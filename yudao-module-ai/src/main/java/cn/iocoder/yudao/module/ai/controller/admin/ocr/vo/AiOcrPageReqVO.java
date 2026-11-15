package cn.iocoder.yudao.module.ai.controller.admin.ocr.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.ocr.AiOcrStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// DONE @AI：应该 controller、service 类注释都删除完？（按 vm 模板与 system 模块：Controller 不写类说明，说明以 @Tag 为准，只保留原作者；Service 接口、实现类只保留「Xxx Service 接口 / 实现类」，实现类保留必要的业务说明；范围为短剧、语音识别、文字识别、语音合成、视频、PPT）
/**
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
