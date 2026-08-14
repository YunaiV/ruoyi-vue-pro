package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工端绩效处理 Response VO")
@Data
public class HrmPortalPerformanceProcessRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "下一运行阶段编号")
    private Long nextStageId;

}
