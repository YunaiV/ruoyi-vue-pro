package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author jisencai
 * @table-fields : outbound_available_qty,inbound_status,company_id,plan_qty,shelve_closed_qty,upstream_id,remark,inbound_dept_id,latest_flow_id,inbound_id,inbound_company_id,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情新增/修改 Request VO")
@Data
public class WmsInboundItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30520")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29327")
    private Long inboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27659")
    @NotNull(message = "标准产品ID不能为空", groups = { ValidationGroup.create.class, ValidationGroup.update.class })
    private Long productId;

    @Schema(description = "WMS入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    @InEnum(WmsInboundStatus.class)
    private Integer inboundStatus;

    @Schema(description = "实际入库量", example = "")
    private Integer actualQty;

    @Schema(description = "批次剩余库存，出库后的剩余库存量", example = "")
    private Integer outboundAvailableQty;

    @Schema(description = "计划入库量", example = "")
    private Integer planQty;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    private Integer shelveClosedQty;

    @Schema(description = "最新的流水ID", example = "")
    private Long latestFlowId;

    @Schema(description = "库存归属部门ID,由用户指定", example = "")
    private Long deptId;

    @Schema(description = "库存财务公司ID,由用户指定", example = "")
    private Long companyId;

    @Schema(description = "备注", example = "")
    private String remark;

    @Schema(description = "来源明细行ID", example = "")
    private Long upstreamId;

}
