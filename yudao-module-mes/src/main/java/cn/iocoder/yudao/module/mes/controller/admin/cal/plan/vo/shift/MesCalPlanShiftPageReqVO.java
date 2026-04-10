package cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.shift;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 计划班次分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesCalPlanShiftPageReqVO extends PageParam {

    @Schema(description = "排班计划编号", example = "1")
    private Long planId;

    @Schema(description = "班次名称", example = "白班")
    private String name;

}
