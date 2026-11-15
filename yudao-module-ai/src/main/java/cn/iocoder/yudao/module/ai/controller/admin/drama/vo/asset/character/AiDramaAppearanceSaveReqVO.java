package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧角色形象创建 / 修改 Request VO")
@Data
public class AiDramaAppearanceSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "角色编号（创建时必填）", example = "1")
    private Long characterId;

    @Schema(description = "形象设定", example = "雨夜，湿透的风衣")
    @Size(max = 2000, message = "形象设定不能超过 2000 个字符")
    private String description;

    @Schema(description = "出图提示词", example = "短发女孩，湿透的米色风衣")
    @Size(max = 2000, message = "出图提示词不能超过 2000 个字符")
    private String prompt;

    @Schema(description = "选用的候选下标", example = "0")
    private Integer selectedIndex;

}
