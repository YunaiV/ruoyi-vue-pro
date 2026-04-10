package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 盘点方案参数分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmStockTakingPlanParamPageReqVO extends PageParam {

    @Schema(description = "方案 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "方案 ID 不能为空")
    private Long planId;

}
