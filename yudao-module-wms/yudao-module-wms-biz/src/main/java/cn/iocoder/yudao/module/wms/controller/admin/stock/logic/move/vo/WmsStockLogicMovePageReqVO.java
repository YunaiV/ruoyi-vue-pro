package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author jisencai
 * @table-fields : no,create_time,execute_status,warehouse_id
 */
@Schema(description = "管理后台 - 逻辑库存移动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockLogicMovePageReqVO extends PageParam {

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "库存移动的执行状态 ; WmsMoveExecuteStatus : 0-草稿 , 1-已执行", example = "1")
    private Integer executeStatus;

    @Schema(description = "仓库ID", example = "13471")
    private Long warehouseId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
