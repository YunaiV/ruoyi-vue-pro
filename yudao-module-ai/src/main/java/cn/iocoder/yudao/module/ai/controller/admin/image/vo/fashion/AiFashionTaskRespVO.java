package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 服装设计流水线任务 Response VO
 *
 * <p>包含任务基本信息 + 所有步骤列表。</p>
 *
 * @author deepay
 */
@Schema(description = "管理后台 - 服装设计流水线任务 Response VO")
@Data
public class AiFashionTaskRespVO {

    @Schema(description = "任务编号", example = "1")
    private Long id;

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "正向提示词", example = "女装春夏甜酷风短款夹克")
    private String prompt;

    @Schema(description = "负向提示词", example = "低清晰度，模糊")
    private String negativePrompt;

    @Schema(description = "生成宽度（像素）", example = "768")
    private Integer width;

    @Schema(description = "生成高度（像素）", example = "1024")
    private Integer height;

    @Schema(description = "随机种子", example = "-1")
    private Long seed;

    @Schema(description = "质量预设", example = "HIGH")
    private String qualityPreset;

    @Schema(description = "SDXL checkpoint 名称", example = "sdXL_v211.safetensors")
    private String modelCheckpoint;

    @Schema(description = "姿势参考图片地址")
    private String poseImageUrl;

    @Schema(description = "面料参考图片地址")
    private String fabricRefUrl;

    @Schema(description = "面料转换强度", example = "0.70")
    private BigDecimal fabricStrength;

    @Schema(description = "是否执行超分辨率", example = "true")
    private Boolean upscale;

    @Schema(description = "超分倍数", example = "2")
    private Integer upscaleFactor;

    @Schema(description = "超分模型名称", example = "R-ESRGAN 4x+")
    private String upscalerName;

    @Schema(description = "任务状态（10进行中 20已完成 30已失败）", example = "20")
    private Integer status;

    @Schema(description = "链路追踪 ID", example = "trace-abc123")
    private String traceId;

    @Schema(description = "最终产出图片地址")
    private String finalPicUrl;

    @Schema(description = "失败原因")
    private String errorMessage;

    @Schema(description = "开始执行时间")
    private LocalDateTime startTime;

    @Schema(description = "完成/失败时间")
    private LocalDateTime finishTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "步骤列表（按执行顺序）")
    private List<Step> steps;

    /**
     * 步骤详情
     */
    @Schema(description = "步骤详情")
    @Data
    public static class Step {

        @Schema(description = "步骤编号", example = "1")
        private Long id;

        @Schema(description = "步骤顺序（0-based）", example = "0")
        private Integer stepOrder;

        @Schema(description = "步骤类型（SDXL/POSE/FABRIC/UPSCALE/THREE_D）", example = "SDXL")
        private String stepType;

        @Schema(description = "步骤状态（10进行中 20已完成 30已失败 40已跳过）", example = "20")
        private Integer status;

        @Schema(description = "输入图片地址")
        private String inputPicUrl;

        @Schema(description = "输出图片地址")
        private String outputPicUrl;

        @Schema(description = "输入参数（JSON）")
        private Map<String, Object> inputOptions;

        @Schema(description = "输出元信息（JSON）")
        private Map<String, Object> outputOptions;

        @Schema(description = "使用的模型名称")
        private String modelName;

        @Schema(description = "耗时（毫秒）", example = "3200")
        private Long durationMs;

        @Schema(description = "重试次数", example = "0")
        private Integer retryCount;

        @Schema(description = "失败原因")
        private String errorMessage;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

    }

}
