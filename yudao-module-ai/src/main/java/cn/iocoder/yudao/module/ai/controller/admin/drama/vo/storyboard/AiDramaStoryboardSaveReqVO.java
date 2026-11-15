package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧分镜创建 / 修改 Request VO")
@Data
public class AiDramaStoryboardSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目编号（创建时必填）", example = "1")
    private Long projectId;

    @Schema(description = "场次", example = "1")
    @Min(value = 1, message = "场次最小为 1")
    private Integer sceneNo;

    @Schema(description = "镜头标题", example = "末班车进站")
    @Size(max = 128, message = "镜头标题不能超过 128 个字符")
    private String title;

    @Schema(description = "景别", example = "中景")
    @Size(max = 32, message = "景别不能超过 32 个字符")
    private String shotType;

    @Schema(description = "运镜", example = "缓慢推进")
    @Size(max = 64, message = "运镜不能超过 64 个字符")
    private String cameraMove;

    @Schema(description = "预计时长（秒）", example = "5")
    @Min(value = 1, message = "时长最小为 1 秒")
    @Max(value = 60, message = "时长最大为 60 秒")
    private Integer duration;

    @Schema(description = "视频提示词", example = "列车驶入空荡站台")
    @Size(max = 2000, message = "视频提示词不能超过 2000 个字符")
    private String videoPrompt;

    @Schema(description = "台词")
    private List<AiDramaStoryboardDO.Dialogue> dialogues;

    @Schema(description = "引用的角色形象编号", example = "[1, 2]")
    private List<Long> appearanceIds;

    @Schema(description = "引用的场景编号", example = "1")
    private Long locationId;

    @Schema(description = "引用的视觉资产编号", example = "[1]")
    private List<Long> visualAssetIds;

    @Schema(description = "起始帧", example = "https://www.iocoder.cn/1.png")
    @Size(max = 1024, message = "起始帧地址不能超过 1024 个字符")
    private String firstFrameUrl;

}
