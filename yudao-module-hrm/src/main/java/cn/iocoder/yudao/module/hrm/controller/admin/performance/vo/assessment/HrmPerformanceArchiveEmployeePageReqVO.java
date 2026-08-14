package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 员工绩效档案分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmPerformanceArchiveEmployeePageReqVO extends PageParam {

    @Schema(description = "员工姓名或工号，模糊匹配", example = "张三")
    private String search;

}
