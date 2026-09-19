package cn.iocoder.yudao.module.oa.controller.admin.note.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OA 笔记目录 Response VO")
@Data
public class OaNoteCategoryRespVO {

    @Schema(description = "目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "目录名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工作")
    private String name;

    @Schema(description = "显示顺序", example = "10")
    private Integer sort;

    @Schema(description = "创建时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
