package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 绘画 Response VO")
@Data
public class AiImageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "OpenAI")
    private String platform;  // 参见 AiPlatformEnum 枚举

    @Schema(description = "生成方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer mode; // 参见 AiImageModeEnum 枚举

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "stable-diffusion-v1-6")
    private String model;

    @Schema(description = "API 密钥编号", example = "1")
    private Long apiKeyId;

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "南极的小企鹅")
    private String prompt;

    @Schema(description = "不希望出现的内容", example = "文字")
    private String negativePrompt;

    @Schema(description = "输入图片")
    private List<AiImageDO.InputImage> inputImages;

    @Schema(description = "图片宽度", example = "1024")
    private Integer width;

    @Schema(description = "图片高度", example = "1024")
    private Integer height;

    @Schema(description = "绘画状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "进度", requiredMode = Schema.RequiredMode.REQUIRED, example = "45")
    private Integer progress;

    @Schema(description = "是否发布", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean publicStatus;

    @Schema(description = "图片地址", example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "绘画错误信息", example = "图片错误信息")
    private String errorMessage;

    @Schema(description = "通用模型的绘制参数")
    private AiImageDO.Options options;

    @Schema(description = "Midjourney 的绘制参数")
    private AiImageDO.MidjourneyOptions midjourneyOptions;

    @Schema(description = "Seedream 的绘制参数")
    private AiImageDO.SeedreamOptions seedreamOptions;

    @Schema(description = "上游任务编号", example = "1712345678")
    private String taskId;

    @Schema(description = "来源记录编号", example = "1")
    private Long parentId;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 详情专属 ==========

    @Schema(description = "派生记录（按钮动作、局部重绘生成的子任务）；只在详情返回")
    private List<AiImageRespVO> children;

}
