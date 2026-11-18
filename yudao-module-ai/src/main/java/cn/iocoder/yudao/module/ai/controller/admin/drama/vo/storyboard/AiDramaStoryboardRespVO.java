package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - AI 短剧分镜 Response VO")
@Data
public class AiDramaStoryboardRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "剧本编号", example = "1")
    private Long scriptId;

    @Schema(description = "镜头序号", example = "1")
    private Integer sceneNo;

    @Schema(description = "镜头标题", example = "末班车进站")
    private String title;

    @Schema(description = "剧本原文", example = "末班车缓缓驶入站台")
    private String sourceText;

    @Schema(description = "画面描述", example = "空荡站台，列车灯光由远及近")
    private String sceneText;

    @Schema(description = "场景类型", example = "1")
    private Integer sceneType;

    @Schema(description = "景别", example = "中景")
    private String shotType;

    @Schema(description = "运镜", example = "缓慢推进")
    private String cameraMove;

    @Schema(description = "预计时长（秒）", example = "5")
    private Integer duration;

    @Schema(description = "摄影规则")
    private Map<String, Object> photographyRules;

    @Schema(description = "表演指导")
    private List<AiDramaStoryboardDO.ActingNote> actingNotes;

    @Schema(description = "图片提示词", example = "列车停靠站台，冷色灯光")
    private String imagePrompt;

    @Schema(description = "视频提示词", example = "列车驶入空荡站台")
    private String videoPrompt;

    @Schema(description = "台词")
    private List<AiDramaStoryboardDO.Dialogue> dialogues;

    @Schema(description = "出场角色：形象与站位")
    private List<AiDramaStoryboardDO.CharacterRef> characters;

    @Schema(description = "引用的场景编号", example = "1")
    private Long locationId;

    @Schema(description = "道具编号：为空时按道具关联的镜头序号", example = "[1]")
    private List<Long> visualAssetIds;

    @Schema(description = "连续性设置")
    private AiDramaStoryboardDO.Continuity continuity;

    @Schema(description = "上游视频任务编号", example = "cgt-xxx")
    private String taskId;

    @Schema(description = "镜头视频状态", example = "40")
    private Integer videoStatus;

    @Schema(description = "镜头视频地址", example = "https://www.iocoder.cn/1.mp4")
    private String videoUrl;

    @Schema(description = "镜头视频尾帧地址", example = "https://www.iocoder.cn/1.png")
    private String lastFrameUrl;

    @Schema(description = "视频模型编号：镜头的设置，或上次生成使用的", example = "1")
    private Long videoModelId;

    @Schema(description = "视频模型标识", example = "doubao-seedance-1-0-pro")
    private String videoModel;

    @Schema(description = "分辨率：空串表示不传", example = "720p")
    private String videoResolution;

    @Schema(description = "失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "规划提示：只在规划分镜时返回，例如角色指定的形象无法唯一匹配", example = "[\"林夏的形象「受伤」无法唯一匹配，已使用初始形象，请检查\"]")
    private List<String> planWarnings;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
