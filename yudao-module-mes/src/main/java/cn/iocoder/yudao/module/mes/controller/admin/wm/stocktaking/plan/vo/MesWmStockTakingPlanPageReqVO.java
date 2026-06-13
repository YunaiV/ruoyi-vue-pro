package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - MES 盘点方案分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmStockTakingPlanPageReqVO extends PageParam {

    @Schema(description = "方案编码", example = "STP202603080001")
    private String code;

    @Schema(description = "方案名称", example = "原料仓月度盘点方案")
    private String name;

    @Schema(description = "盘点类型", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
