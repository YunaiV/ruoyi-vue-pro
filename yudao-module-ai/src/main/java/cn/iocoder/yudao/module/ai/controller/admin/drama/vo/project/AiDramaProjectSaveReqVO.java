package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧项目创建 / 修改 Request VO")
@Data
public class AiDramaProjectSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "末班地铁")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 64, message = "项目名称不能超过 64 个字符")
    private String name;

    @Schema(description = "故事想法", example = "末班地铁上，女孩发现对面的乘客是十年后的自己")
    @Size(max = 5000, message = "故事想法不能超过 5000 个字符")
    private String idea;

    @Schema(description = "画风 / 视觉风格", example = "电影感写实，冷色调")
    @Size(max = 255, message = "风格不能超过 255 个字符")
    private String style;

    @Schema(description = "画幅", example = "9:16")
    @Size(max = 16, message = "画幅不能超过 16 个字符")
    private String aspectRatio;

}
