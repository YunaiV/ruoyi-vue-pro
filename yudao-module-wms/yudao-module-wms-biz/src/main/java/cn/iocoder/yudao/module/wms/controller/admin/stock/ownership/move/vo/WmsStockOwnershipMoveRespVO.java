package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemRespVO;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,no,creator,update_time,create_time,execute_status,id,warehouse_id,updater
 */
@Schema(description = "管理后台 - 所有者库存移动 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockOwnershipMoveRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2070")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String no;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("执行状态")
    private Integer executeStatus;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13471")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "详情清单", example = "")
    @ExcelProperty("详情清单")
    private List<WmsStockOwnershipMoveItemRespVO> itemList;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;
}
