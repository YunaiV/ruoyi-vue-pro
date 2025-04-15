package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : outbound_available_qty,inbound_status,company_id,create_time,plan_qty,shelved_qty,remark,latest_flow_id,inbound_id,source_item_id,actual_qty,product_id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInboundItemPageReqVO extends PageParam {

    @Schema(description = "入库单ID", example = "29327")
    private Long inboundId;

    @Schema(description = "入库单编号", example = "IN20250325001")
    private String inboundNo;

    @Schema(description = "标准产品ID", example = "66")
    private Long productId;

    @Schema(description = "仓库ID", example = "32")
    private Long warehouseId;

    @Schema(description = "仓位ID", example = "1")
    private Long binId;

    @Schema(description = "产品代码", example = "27659")
    private String productCode;

    @Schema(description = "来源详情ID", example = "30830", hidden = true)
    private Long sourceItemId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "WMS入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    private Integer inboundStatus;

    @Schema(description = "实际入库量", example = "")
    private Integer[] actualQty;

    @Schema(description = "批次剩余库存，出库后的剩余库存量", example = "")
    private Integer[] outboundAvailableQty;

    @Schema(description = "计划入库量", example = "")
    private Integer[] planQty;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    private Integer[] shelvedQty;

    @Schema(description = "最新的流水ID", example = "", hidden = true)
    private Long latestFlowId;

    @Schema(description = "库龄", example = "")
    private Integer[] age;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "库存财务公司ID", example = "")
    private Long companyId;

    @Schema(description = "备注", example = "")
    private String remark;
}
