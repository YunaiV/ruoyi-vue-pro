package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - HRM 绩效归档分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmPerformanceArchivePageReqVO extends PageParam {

    @Schema(description = "绩效计划编号", example = "1024")
    private Long planId;

    @Schema(description = "员工编号", example = "2048")
    private Long employeeId;

    @Schema(description = "员工编号列表", example = "[2048, 2049]")
    private List<Long> employeeIds;

}
