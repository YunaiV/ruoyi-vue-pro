package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 所有者库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockOwnershipPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "14322")
    private Long warehouseId;

    @Schema(description = "产品ID", example = "1919")
    private Long productId;

    @Schema(description = "产品SKU")
    private String productSku;

    @Schema(description = "库存主体ID", example = "28887")
    private Long inventorySubjectId;

    @Schema(description = "库存归属ID", example = "15579")
    private Long inventoryOwnerId;

    @Schema(description = "可用库存")
    private Integer availableQuantity;

    @Schema(description = "待出库库存")
    private Integer pendingOutboundQuantity;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}