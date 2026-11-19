package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaSceneTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - AI 短剧分镜创建 / 修改 Request VO")
@Data
public class AiDramaStoryboardSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目编号（创建时必填）", example = "1")
    private Long projectId;

    @Schema(description = "剧本编号：创建时有效，为空时为当前剧本；用于在历史剧本版本下新增镜头", example = "1")
    private Long scriptId;

    @Schema(description = "镜头序号", example = "1")
    @Min(value = 1, message = "镜头序号最小为 1")
    private Integer sceneNo;

    @Schema(description = "镜头标题", example = "末班车进站")
    @Size(max = 128, message = "镜头标题不能超过 128 个字符")
    private String title;

    @Schema(description = "剧本原文", example = "末班车缓缓驶入站台")
    private String sourceText;

    @Schema(description = "画面描述", example = "空荡站台，列车灯光由远及近")
    private String sceneText;

    @Schema(description = "场景类型", example = "1")
    @InEnum(AiDramaSceneTypeEnum.class)
    private Integer sceneType;

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

    @Schema(description = "摄影规则")
    private Map<String, Object> photographyRules;

    @Schema(description = "表演指导")
    private List<AiDramaStoryboardDO.ActingNote> actingNotes;

    @Schema(description = "图片提示词", example = "列车停靠站台，冷色灯光")
    @Size(max = 2000, message = "图片提示词不能超过 2000 个字符")
    private String imagePrompt;

    @Schema(description = "视频提示词", example = "列车驶入空荡站台")
    @Size(max = 2000, message = "视频提示词不能超过 2000 个字符")
    private String videoPrompt;

    @Schema(description = "台词")
    private List<AiDramaStoryboardDO.Dialogue> dialogues;

    @Schema(description = "出场角色：形象与站位")
    private List<AiDramaStoryboardDO.CharacterRef> characters;

    @Schema(description = "引用的场景编号", example = "1")
    private Long locationId;

    @Schema(description = "道具编号：为空时按道具关联的镜头序号，空数组表示本镜没有道具", example = "[1]")
    private List<Long> visualAssetIds;

    @Schema(description = "连续性设置")
    private AiDramaStoryboardDO.Continuity continuity;

    @Schema(description = "镜头的视频模型编号：为空时使用生成时所选的", example = "1")
    private Long videoModelId;

    @Schema(description = "镜头的分辨率：与视频模型一起生效，空串表示不传", example = "720p")
    @Size(max = 16, message = "分辨率不能超过 16 个字符")
    private String videoResolution;

}
