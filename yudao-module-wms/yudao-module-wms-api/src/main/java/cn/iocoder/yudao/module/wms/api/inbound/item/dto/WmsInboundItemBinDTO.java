package cn.iocoder.yudao.module.wms.api.inbound.item.dto;

import lombok.Data;

/**
 * WMS - 入库单批次库存 DTO
 * 字段来源：
 * - WmsInboundItemDO: 基础入库单详情字段
 * - WmsInboundItemBinQueryDO: 入库单批次库存查询特有字段
 *
 * @author wdy
 */
@Data
public class WmsInboundItemBinDTO {


    /**
     * WmsInboundItemDO 主键
     */
    private Long id;

    /**
     * 入库单ID
     */
    private Long inboundId;

    /**
     * 标准产品ID
     */
    private Long productId;

    /**
     * 入库状态 ; InboundStatus : 0-未入库 , 1-部分入库 , 2-已入库
     */
    private Integer inboundStatus;

    /**
     * 实际入库量
     */
    private Integer actualQty;

    /**
     * 批次剩余库存，出库后的剩余库存量
     */
    private Integer outboundAvailableQty;

    /**
     * 计划入库量
     */
    private Integer planQty;

    /**
     * 已上架量，已经拣货到仓位的库存量
     */
    private Integer shelveClosedQty;

    /**
     * 最新的流水ID
     */
    private Long latestFlowId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源明细行ID
     */
    private Long upstreamId;

    /**
     * 入库的财务公司ID
     */
    private Long inboundCompanyId;

    /**
     * 入库的归属部门ID,由用户指定
     */
    private Long inboundDeptId;

    //WmsInboundItemBinQueryDO
    /**
     * 仓位ID
     */
    private Long binId;

    /**
     * 仓库ID
     */
    private String warehouseId;
    /**
     * 仓位可用库存
     */
    private Integer binAvailableQty;
    /**
     * 仓位可售库存
     */
    private Integer binSellableQty;
    /**
     * 仓位待出库库存
     */
    private Integer binOutboundPendingQty;
    /**
     * 上架单ID
     */
    private String pickupId;
    /**
     * 上架数量
     */
    private Integer pickupQty;
    /**
     * 上架单号
     */
    private String pickupCode;

    /**
     * 入库单号
     */
    private String inboundCode;

    /**
     * 库龄
     */
    private Integer age;

} 