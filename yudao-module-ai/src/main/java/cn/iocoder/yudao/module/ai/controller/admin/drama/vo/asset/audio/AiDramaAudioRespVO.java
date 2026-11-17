package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 短剧音频 Response VO")
@Data
public class AiDramaAudioRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "角色编号", example = "1")
    private Long characterId;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "名称", example = "雨夜钢琴")
    private String name;

    @Schema(description = "文本", example = "这是最后一班车了。")
    private String text;

    @Schema(description = "音色", example = "longxiaochun")
    private String voice;

    @Schema(description = "分镜编号：对白所属的镜头", example = "1")
    private Long storyboardId;

    @Schema(description = "时长（秒）", example = "3.2")
    private Double duration;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.mp3")
    private String fileUrl;

    @Schema(description = "使用的分镜编号：配乐、参考音频使用；为空表示候选")
    private List<Long> storyboardIds;

    @Schema(description = "音量", example = "0.6")
    private Double volume;

    @Schema(description = "起始偏移（秒）", example = "0")
    private Double startOffset;

    @Schema(description = "AI 音乐编号：由 AI 音乐选用的配乐", example = "1")
    private Long musicId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
