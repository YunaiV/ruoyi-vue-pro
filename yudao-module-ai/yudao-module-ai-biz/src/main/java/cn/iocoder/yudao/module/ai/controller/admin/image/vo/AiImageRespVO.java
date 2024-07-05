package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - AI 绘画 Response VO")
@Data
public class AiImageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "OpenAI")
    private String platform;  // 参见 AiPlatformEnum 枚举

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "stable-diffusion-v1-6")
    private String model;

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "南极的小企鹅")
    private String prompt;

    @Schema(description = "图片宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer width;

    @Schema(description = "图片高度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer height;

    @Schema(description = "绘画状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "是否发布", requiredMode = Schema.RequiredMode.REQUIRED, example = "public")
    private Boolean publicStatus;

    @Schema(description = "图片地址", example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "绘画错误信息", example = "图片错误信息")
    private String errorMessage;

    @Schema(description = "绘制参数")
    private Map<String, String> options;

    @Schema(description = "mj buttons 按钮")
    private List<MidjourneyApi.Button> buttons;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
