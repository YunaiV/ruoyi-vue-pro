package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - MES 盘点结果分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmStockTakingTaskResultPageReqVO extends PageParam {

    @Schema(description = "任务编号", example = "1")
    private Long taskId;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

}
