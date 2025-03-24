package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : inbound_id,outbound_available_quantity,source_item_id,inbound_status,actual_quantity,create_time,product_id,plan_quantity,shelved_quantity
 */
@Schema(description = "管理后台 - 入库单详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInboundItemPageReqVO extends PageParam {

    @Schema(description = "入库单ID", example = "29327")
    private Long inboundId;

    @Schema(description = "标准产品ID", example = "27659")
    private Long productId;

    @Schema(description = "计划入库量")
    private Integer planQuantity;

    @Schema(description = "实际入库量")
    private Integer actualQuantity;

    @Schema(description = "来源详情ID", example = "30830")
    private Long sourceItemId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "入库状态 ; InboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    private Integer inboundStatus;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    private Integer shelvedQuantity;

    @Schema(description = "批次剩余库存，出库后的剩余库存量", example = "")
    private Integer outboundAvailableQuantity;
}
