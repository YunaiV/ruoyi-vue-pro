package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - BPM 流程分类 Response VO")
@Data
public class BpmCategoryRespVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3167")
    private Long id;

    @Schema(description = "分类名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String name;

    @Schema(description = "分类标志", requiredMode = Schema.RequiredMode.REQUIRED, example = "OA")
    private String code;

    @Schema(description = "分类描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你猜")
    private String description;

    @Schema(description = "分类状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "分类排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}