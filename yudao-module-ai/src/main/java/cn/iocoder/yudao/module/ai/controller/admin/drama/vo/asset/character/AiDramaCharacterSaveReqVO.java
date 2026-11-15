package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧角色创建 / 修改 Request VO")
@Data
public class AiDramaCharacterSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目编号（创建时必填）", example = "1")
    private Long projectId;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "林夏")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称不能超过 64 个字符")
    private String name;

    @Schema(description = "角色设定", example = "25 岁，地铁司机")
    @Size(max = 2000, message = "角色设定不能超过 2000 个字符")
    private String description;

    @Schema(description = "角色出图提示词", example = "短发女孩，深蓝制服")
    @Size(max = 2000, message = "出图提示词不能超过 2000 个字符")
    private String prompt;

    @Schema(description = "角色参考图", example = "https://www.iocoder.cn/1.png")
    @Size(max = 1024, message = "参考图地址不能超过 1024 个字符")
    private String referenceImageUrl;

}
