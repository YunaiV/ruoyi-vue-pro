package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧配乐、参考音频修改 Request VO：名称、使用的分镜、音量、起始偏移")
@Data
public class AiDramaAudioUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "名称", example = "雨夜钢琴")
    @Size(max = 128, message = "名称不能超过 128 个字符")
    private String name;

    @Schema(description = "使用的分镜编号：为空表示候选", example = "[1, 2]")
    private List<Long> storyboardIds;

    @Schema(description = "音量", example = "0.6")
    @DecimalMin(value = "0", message = "音量最小为 0")
    @DecimalMax(value = "2", message = "音量最大为 2")
    private Double volume;

    @Schema(description = "起始偏移（秒）", example = "0")
    @DecimalMin(value = "0", message = "起始偏移不能小于 0")
    private Double startOffset;

}
