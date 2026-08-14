package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - HRM 工资表薪资项节点 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalaryOptionNodeRespVO extends HrmSalaryOptionRespVO {

    @Schema(description = "子薪资项")
    private List<HrmSalaryOptionNodeRespVO> children;

}
