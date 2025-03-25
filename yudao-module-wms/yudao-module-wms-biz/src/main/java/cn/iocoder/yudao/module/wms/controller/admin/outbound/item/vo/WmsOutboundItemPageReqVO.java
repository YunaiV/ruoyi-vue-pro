package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : source_item_id,actual_qty,create_time,outbound_status,bin_id,plan_qty,product_id,outbound_id
 */
@Schema(description = "管理后台 - 出库单详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsOutboundItemPageReqVO extends PageParam {

    @Schema(description = "入库单ID", example = "6602")
    private Long outboundId;

    @Schema(description = "标准产品ID", example = "20572")
    private Long productId;

    @Schema(description = "来源详情ID", example = "11448")
    private Long sourceItemId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "出库状态 ; OutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    private Integer outboundStatus;

    @Schema(description = "出库库位ID", example = "")
    private Long binId;

    @Schema(description = "实际出库量", example = "")
    private Integer actualQty;

    @Schema(description = "计划出库量", example = "")
    private Integer planQty;
}
