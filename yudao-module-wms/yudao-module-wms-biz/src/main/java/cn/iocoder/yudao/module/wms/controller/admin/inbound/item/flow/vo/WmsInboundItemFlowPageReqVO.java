package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : inbound_id,product_sku,create_time,changed_quantity,product_id,inbound_item_id,outbound_id,outbound_item_id
 */
@Schema(description = "管理后台 - 入库单库存详情扣减分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInboundItemFlowPageReqVO extends PageParam {

    @Schema(description = "入库单ID", example = "23778")
    private Long inboundId;

    @Schema(description = "入库单明细ID", example = "25263")
    private Long inboundItemId;

    @Schema(description = "标准产品ID", example = "30952")
    private Long productId;

    @Schema(description = "标准产品SKU")
    private String productSku;

    @Schema(description = "出库单ID", example = "11015")
    private Long outboundId;

    @Schema(description = "出库单明细ID", example = "28163")
    private Long outboundItemId;

    @Schema(description = "变化的数量，出库量")
    private Integer changedQuantity;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
