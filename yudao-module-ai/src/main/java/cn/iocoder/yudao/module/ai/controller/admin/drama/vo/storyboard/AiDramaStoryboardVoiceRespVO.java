package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧镜头声音预览 Response VO：生成镜头视频时使用的发言角色、参考音频")
@Data
public class AiDramaStoryboardVoiceRespVO {

    @Schema(description = "是否手动指定了发言角色：否则按台词自动识别", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean explicit;

    @Schema(description = "发言角色：按顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Speaker> speakers;

    @Schema(description = "参考音频：按提交时的编号顺序；视频模型不支持参考音频时不带", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ReferenceAudio> referenceAudios;

    @Schema(description = "发言角色")
    @Data
    public static class Speaker {

        @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long characterId;

        @Schema(description = "角色名", requiredMode = Schema.RequiredMode.REQUIRED, example = "林夏")
        private String name;

        @Schema(description = "声音描述：作为文字说明写入视频提示词", example = "清亮的少女音")
        private String voiceDescription;

        @Schema(description = "选用样音的地址：为空表示没有选用样音", example = "https://www.iocoder.cn/1.mp3")
        private String sampleAudioUrl;

    }

    @Schema(description = "参考音频")
    @Data
    public static class ReferenceAudio {

        @Schema(description = "音频地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.mp3")
        private String url;

        @Schema(description = "说明", requiredMode = Schema.RequiredMode.REQUIRED, example = "参考音频1为林夏的音色")
        private String description;

    }

}
