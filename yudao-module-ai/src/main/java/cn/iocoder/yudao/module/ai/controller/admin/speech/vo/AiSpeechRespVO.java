package cn.iocoder.yudao.module.ai.controller.admin.speech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 语音 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 语音 Response VO")
@Data
public class AiSpeechRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    private Long modelId;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "MINIMAX")
    private String platform;

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "speech-2.8-hd")
    private String model;

    @Schema(description = "朗读文本", requiredMode = Schema.RequiredMode.REQUIRED, example = "欢迎使用芋道语音合成")
    private String inputText;

    @Schema(description = "音色", requiredMode = Schema.RequiredMode.REQUIRED, example = "male-qn-qingse")
    private String voice;

    @Schema(description = "音频格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "mp3")
    private String format;

    @Schema(description = "语速倍数", example = "1.2")
    private Double speed;

    @Schema(description = "状态，参见 AiSpeechStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "音频访问地址（私有存储时为签名地址）", example = "https://www.iocoder.cn/speech-1024.mp3")
    private String audioUrl;

    @Schema(description = "音频大小，单位：字节", example = "102400")
    private Integer audioSize;

    @Schema(description = "错误信息", example = "MiniMax 语音合成失败")
    private String errorMessage;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

}
