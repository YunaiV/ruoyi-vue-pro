package cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 绘画生成（Midjourney 文生图、图生图、一致性） Request VO")
@Data
public class AiMidjourneyImagineReqVO {

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型编号不能为空")
    private Long modelId;

    @Schema(description = "生成方式：20 文生图、21 图生图、24 一致性", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "生成方式不能为空")
    private Integer mode;

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "中国神龙")
    @NotEmpty(message = "提示词不能为空")
    @Size(max = 1200, message = "提示词最大 1200")
    private String prompt;

    @Schema(description = "不希望出现的内容（拼接为 --no）", example = "文字、水印")
    @Size(max = 2000, message = "不希望出现的内容最大 2000")
    private String negativePrompt;

    @Schema(description = "版本参数", requiredMode = Schema.RequiredMode.REQUIRED, example = "--v 7")
    @NotEmpty(message = "版本不能为空")
    @Pattern(regexp = "^--(v|niji) [0-9.]+$", message = "版本参数格式不正确")
    private String version;

    @Schema(description = "比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "1:1")
    @NotEmpty(message = "比例不能为空")
    @Pattern(regexp = "^[1-9][0-9]*:[1-9][0-9]*$", message = "比例格式不正确")
    private String aspectRatio;

    @Schema(description = "画质 --q", example = "1")
    @DecimalMin(value = "0", message = "画质最小 0")
    private Double quality;

    @Schema(description = "风格化 --s", example = "100")
    @Min(value = 0, message = "风格化最小 0")
    @Max(value = 1000, message = "风格化最大 1000")
    private Integer stylize;

    @Schema(description = "创意度 --c", example = "10")
    @Min(value = 0, message = "创意度最小 0")
    @Max(value = 100, message = "创意度最大 100")
    private Integer chaos;

    @Schema(description = "随机种子 --seed，-1 表示随机", example = "-1")
    @Min(value = -1, message = "随机种子最小 -1")
    @Max(value = 4294967295L, message = "随机种子最大 4294967295")
    private Long seed;

    @Schema(description = "平铺 --tile", example = "false")
    private Boolean tile;

    @Schema(description = "RAW --raw", example = "false")
    private Boolean raw;

    @Schema(description = "HD --hd", example = "false")
    private Boolean hd;

    // ========== 图生图 ==========

    @Schema(description = "参考图地址数组（图生图 1-5 张）")
    @Size(max = 5, message = "参考图最多 5 张")
    private List<String> referenceImageUrls;

    @Schema(description = "图片权重 --iw", example = "1")
    @DecimalMin(value = "0", message = "图片权重最小 0")
    @DecimalMax(value = "3", message = "图片权重最大 3")
    private Double imageWeight;

    // ========== 一致性 ==========

    @Schema(description = "角色参考图地址 --cref", example = "https://www.iocoder.cn/1.png")
    private String crefUrl;

    @Schema(description = "角色权重 --cw", example = "100")
    @Min(value = 0, message = "角色权重最小 0")
    @Max(value = 100, message = "角色权重最大 100")
    private Integer characterWeight;

    @Schema(description = "风格参考图地址 --sref", example = "https://www.iocoder.cn/2.png")
    private String srefUrl;

    @Schema(description = "风格权重 --sw", example = "100")
    @Min(value = 0, message = "风格权重最小 0")
    @Max(value = 1000, message = "风格权重最大 1000")
    private Integer styleWeight;

    @Schema(description = "风格版本 --sv", example = "6")
    @Min(value = 1, message = "风格版本最小 1")
    private Integer styleVersion;

}
