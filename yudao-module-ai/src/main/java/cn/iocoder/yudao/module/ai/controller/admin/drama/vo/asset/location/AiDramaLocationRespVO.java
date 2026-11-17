package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 短剧场景 Response VO")
@Data
public class AiDramaLocationRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "末班地铁车厢")
    private String name;

    @Schema(description = "场景设定", example = "深夜")
    private String description;

    @Schema(description = "是否有群众演员", example = "false")
    private Boolean crowd;

    @Schema(description = "群众演员描述", example = "零星几名乘客")
    private String crowdDescription;

    @Schema(description = "可用站位")
    private List<String> availableSlots;

    @Schema(description = "场景描述：一个场景可有多段描述")
    private List<String> descriptions;

    @Schema(description = "用户参考图", example = "https://www.iocoder.cn/1.png")
    private String referenceImageUrl;

    @Schema(description = "候选图地址")
    private List<String> imageUrls;

    @Schema(description = "候选图描述：与候选图一一对应")
    private List<String> imageDescriptions;

    @Schema(description = "选用的候选下标", example = "0")
    private Integer selectedIndex;

    @Schema(description = "上一版候选图地址：不为空时可撤销")
    private List<String> previousImageUrls;

    @Schema(description = "绘画模型编号", example = "1")
    private Long modelId;

    @Schema(description = "最近一次生成的状态", example = "40")
    private Integer status;

    @Schema(description = "生成失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
