package cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : inbound_id,quantity,create_time,bin_id,product_id,inbound_item_id,pickup_id
 */
@Schema(description = "管理后台 - 拣货单详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsPickupItemPageReqVO extends PageParam {

    @Schema(description = "拣货单ID", example = "22404")
    private Long pickupId;

    @Schema(description = "入库单ID", example = "20253")
    private Long inboundId;

    @Schema(description = "入库单明细ID", example = "21563")
    private Long inboundItemId;

    @Schema(description = "产品ID", example = "17566")
    private Long productId;

    @Schema(description = "拣货数量")
    private Integer quantity;

    @Schema(description = "仓位ID，拣货到目标仓位", example = "8732")
    private Long binId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
