package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧角色形象创建 / 修改 Request VO")
@Data
public class AiDramaAppearanceSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "角色编号（创建时必填）", example = "1")
    private Long characterId;

    @Schema(description = "变化原因", example = "雨夜换装")
    @Size(max = 255, message = "变化原因不能超过 255 个字符")
    private String changeReason;

    @Schema(description = "形象设定", example = "雨夜，湿透的风衣")
    @Size(max = 2000, message = "形象设定不能超过 2000 个字符")
    private String description;

    @Schema(description = "形象参考图", example = "https://www.iocoder.cn/1.png")
    @Size(max = 1024, message = "参考图地址不能超过 1024 个字符")
    private String referenceImageUrl;

    @Schema(description = "选用的候选下标", example = "0")
    private Integer selectedIndex;

}
