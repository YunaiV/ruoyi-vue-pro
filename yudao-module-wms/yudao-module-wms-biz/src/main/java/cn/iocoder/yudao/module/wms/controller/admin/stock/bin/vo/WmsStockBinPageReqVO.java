package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 仓位库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockBinPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "748")
    private Long warehouseId;

    @Schema(description = "库位ID", example = "10839")
    private Long binId;

    @Schema(description = "产品ID", example = "11713")
    private String productId;

    @Schema(description = "可用量，在库的良品数量")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量")
    private Integer sellableQuantity;

    @Schema(description = "待出库量")
    private Integer outboundPendingQuantity;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}