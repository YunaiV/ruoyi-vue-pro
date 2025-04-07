package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,update_time,bin_move_id,create_time,product_id,qty,id,from_bin_id,to_bin_id,updater
 */
@Schema(description = "管理后台 - 库位移动详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockBinMoveItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26371")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "库位移动表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13416")
    @ExcelProperty("库位移动表ID")
    private Long binMoveId;

    @Schema(description = "产品ID", example = "4832")
    @ExcelProperty("产品ID")
    private Integer productId;

    @Schema(description = "调出库位ID", example = "1149")
    @ExcelProperty("调出库位ID")
    private Long fromBinId;

    @Schema(description = "调入库位ID", example = "28214")
    @ExcelProperty("调入库位ID")
    private Long toBinId;

    @Schema(description = "移动数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("移动数量")
    private Integer qty;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;
}
