package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 人力资源 Response VO")
@Data
public class MesMdWorkstationWorkerRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工作站编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workstationId;

    @Schema(description = "岗位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long postId;

    @Schema(description = "岗位名称", example = "操作员")
    private String postName;

    @Schema(description = "数量", example = "1")
    private Integer quantity;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
