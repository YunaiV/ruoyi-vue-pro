package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAspectRatioEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTransitionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧成片合成 Request VO")
@Data
public class AiDramaComposeReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "镜头编号：按顺序拼接，至少 2 个已完成的镜头", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "镜头不能为空")
    @Size(min = 2, message = "至少选择 2 个已完成的镜头")
    private List<Long> storyboardIds;

    @Schema(description = "画幅", requiredMode = Schema.RequiredMode.REQUIRED, example = "9:16")
    @NotBlank(message = "画幅不能为空")
    @InEnum(AiDramaAspectRatioEnum.class)
    private String aspectRatio;

    @Schema(description = "转场类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转场类型不能为空")
    @InEnum(AiDramaTransitionTypeEnum.class)
    private Integer transitionType;

    @Schema(description = "转场时长（秒）", example = "0.5")
    @DecimalMin(value = "0.1", message = "转场时长最小 0.1 秒")
    @DecimalMax(value = "2", message = "转场时长最大 2 秒")
    private Double transitionDuration;

    @Schema(description = "旁白音频编号（可选）", example = "1")
    private Long narrationAudioId;

    @Schema(description = "水印文字（可选）", example = "芋道短剧")
    @Size(max = 32, message = "水印文字不能超过 32 个字符")
    private String watermarkText;

}
