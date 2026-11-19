package cn.iocoder.yudao.module.ai.controller.admin.video.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoModeEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 视频生成 Request VO")
@Data
public class AiVideoGenerateReqVO {

    @Schema(description = "创作类型，参见 AiVideoTypeEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "创作类型不能为空")
    @InEnum(AiVideoTypeEnum.class)
    private Integer type;

    @Schema(description = "生成方式（视频生成），参见 AiVideoModeEnum 枚举类", example = "text")
    @InEnum(AiVideoModeEnum.class)
    private String mode;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "模型不能为空")
    private Long modelId;

    @Schema(description = "提示词", example = "一只橘猫在雨夜的霓虹街头奔跑")
    @Size(max = 2000, message = "提示词不能超过 2000 个字符")
    private String prompt;

    @Schema(description = "分辨率", example = "720p")
    @Size(max = 16, message = "分辨率不能超过 16 个字符")
    private String resolution;

    @Schema(description = "画面比例", example = "16:9")
    @Size(max = 16, message = "画面比例不能超过 16 个字符")
    private String ratio;

    @Schema(description = "时长（秒）", example = "5")
    private Integer duration;

    @Schema(description = "其它生成参数：随机种子、水印、生成音频等，按模型开放")
    private AiVideoDO.Options options;

    @Schema(description = "输入文件：键为角色（first_frame、last_frame、reference_image、reference_video、reference_audio、image、audio、video），值为文件地址")
    private Map<String, List<String>> inputUrls;

}
