package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台 - 创建服装设计流水线任务 Request VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - 创建服装设计流水线任务 Request VO")
@Data
public class AiFashionTaskCreateReqVO {

    @Schema(description = "正向提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "女装春夏甜酷风短款夹克，高腰A字裙，白底电商棚拍，细节清晰")
    @NotBlank(message = "提示词不能为空")
    @Size(max = 2000, message = "提示词最大 2000 字符")
    private String prompt;

    @Schema(description = "负向提示词", example = "低清晰度，模糊，畸形手，文字水印")
    @Size(max = 2000, message = "负向提示词最大 2000 字符")
    private String negativePrompt;

    @Schema(description = "生成宽度（像素）", example = "768")
    @Min(value = 64, message = "宽度最小 64px")
    @Max(value = 2048, message = "宽度最大 2048px")
    private Integer width = 768;

    @Schema(description = "生成高度（像素）", example = "1024")
    @Min(value = 64, message = "高度最小 64px")
    @Max(value = 2048, message = "高度最大 2048px")
    private Integer height = 1024;

    @Schema(description = "随机种子，-1 表示随机", example = "-1")
    private Long seed = -1L;

    @Schema(description = "质量预设（HIGH / MEDIUM / LOW）", example = "HIGH")
    private String qualityPreset;

    @Schema(description = "SDXL checkpoint 名称（留空使用服务器当前模型）", example = "sdXL_v211.safetensors")
    private String modelCheckpoint;

    // ===== ControlNet 姿势控制（可选）=====

    @Schema(description = "姿势参考图片地址（有值则启用 ControlNet Pose 步骤）", example = "https://example.com/pose.png")
    private String poseImageUrl;

    // ===== 面料转换（可选）=====

    @Schema(description = "面料参考图片地址（有值则启用 Fabric 步骤）", example = "https://example.com/fabric_denim.png")
    private String fabricRefUrl;

    @Schema(description = "面料转换强度，0~1，默认 0.70", example = "0.70")
    @Min(value = 0, message = "面料转换强度最小 0")
    @Max(value = 1, message = "面料转换强度最大 1")
    private BigDecimal fabricStrength = new BigDecimal("0.70");

    // ===== 超分辨率（可选）=====

    @Schema(description = "是否执行超分辨率提升", example = "true")
    private Boolean upscale = Boolean.TRUE;

    @Schema(description = "超分倍数，默认 2", example = "2")
    @Min(value = 2, message = "超分倍数最小 2")
    @Max(value = 4, message = "超分倍数最大 4")
    private Integer upscaleFactor = 2;

    @Schema(description = "超分模型名称", example = "R-ESRGAN 4x+")
    private String upscalerName = "R-ESRGAN 4x+";

}
