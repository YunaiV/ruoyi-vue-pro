package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧场景创建 / 修改 Request VO")
@Data
public class AiDramaLocationSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目编号（创建时必填）", example = "1")
    private Long projectId;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "末班地铁车厢")
    @NotBlank(message = "场景名称不能为空")
    @Size(max = 64, message = "场景名称不能超过 64 个字符")
    private String name;

    @Schema(description = "场景设定", example = "深夜，空荡的车厢")
    @Size(max = 2000, message = "场景设定不能超过 2000 个字符")
    private String description;

    @Schema(description = "是否有群众演员", example = "false")
    private Boolean crowd;

    @Schema(description = "群众演员描述", example = "零星几名乘客")
    @Size(max = 500, message = "群众演员描述不能超过 500 个字符")
    private String crowdDescription;

    @Schema(description = "可用站位")
    private List<String> availableSlots;

    @Schema(description = "场景描述：一个场景可有多段描述")
    private List<String> descriptions;

    @Schema(description = "用户参考图", example = "https://www.iocoder.cn/1.png")
    @Size(max = 1024, message = "参考图地址不能超过 1024 个字符")
    private String referenceImageUrl;

    @Schema(description = "选用的候选下标", example = "0")
    private Integer selectedIndex;

}
