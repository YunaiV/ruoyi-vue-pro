package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 项目分组 Response VO")
@Data
public class PmsProjectGroupRespVO {

    @Schema(description = "项目分组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "重点项目")
    private String name;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "分组类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer type;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "项目数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer projectCount;

}
