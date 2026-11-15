package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧分镜 Response VO")
@Data
public class AiDramaStoryboardRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "剧本编号", example = "1")
    private Long scriptId;

    @Schema(description = "场次", example = "1")
    private Integer sceneNo;

    @Schema(description = "镜头标题", example = "末班车进站")
    private String title;

    @Schema(description = "景别", example = "中景")
    private String shotType;

    @Schema(description = "运镜", example = "缓慢推进")
    private String cameraMove;

    @Schema(description = "预计时长（秒）", example = "5")
    private Integer duration;

    @Schema(description = "视频提示词", example = "列车驶入空荡站台")
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
    private String firstFrameUrl;

    @Schema(description = "上游视频任务编号", example = "cgt-xxx")
    private String taskId;

    @Schema(description = "镜头视频状态，参见 AiDramaStatusEnum 枚举类", example = "40")
    private Integer videoStatus;

    @Schema(description = "镜头视频地址", example = "https://www.iocoder.cn/1.mp4")
    private String videoUrl;

    @Schema(description = "视频模型编号", example = "1")
    private Long videoModelId;

    @Schema(description = "失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
