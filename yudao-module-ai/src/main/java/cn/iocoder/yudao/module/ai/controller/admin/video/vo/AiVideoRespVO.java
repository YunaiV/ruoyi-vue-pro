package cn.iocoder.yudao.module.ai.controller.admin.video.vo;

import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 视频 Response VO")
@Data
public class AiVideoRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道源码")
    private String userNickname;

    @Schema(description = "创作类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "生成方式", example = "text")
    private String mode;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "MINIMAX")
    private String platform;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long modelId;

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "MiniMax-H3")
    private String model;

    @Schema(description = "API 密钥编号", example = "12")
    private Long apiKeyId;

    @Schema(description = "API 密钥名称（仅管理端）", example = "MiniMax")
    private String apiKeyName;

    @Schema(description = "提示词", example = "一只橘猫在雨夜的霓虹街头奔跑")
    private String prompt;

    @Schema(description = "分辨率", example = "720p")
    private String resolution;

    @Schema(description = "画面比例", example = "16:9")
    private String ratio;

    @Schema(description = "时长（秒）", example = "5")
    private Integer duration;

    @Schema(description = "其它生成参数")
    private AiVideoDO.Options options;

    @Schema(description = "输入文件（本人与管理端）")
    private Map<String, List<String>> inputUrls;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    private Integer status;

    @Schema(description = "进度", example = "100")
    private Integer progress;

    @Schema(description = "服务商任务编号", example = "cgt-2026")
    private String taskId;

    @Schema(description = "错误信息", example = "服务商审核未通过")
    private String errorMessage;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "视频地址", example = "https://www.iocoder.cn/1.mp4")
    private String videoUrl;

    @Schema(description = "封面地址", example = "https://www.iocoder.cn/1.png")
    private String coverUrl;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean publicStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
