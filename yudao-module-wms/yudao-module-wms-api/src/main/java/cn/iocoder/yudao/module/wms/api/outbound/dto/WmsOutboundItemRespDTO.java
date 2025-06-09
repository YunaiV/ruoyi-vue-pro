package cn.iocoder.yudao.module.wms.api.outbound.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @table-fields : tenant_id,creator,company_id,create_time,bin_id,plan_qty,upstream_id,remark,outbound_id,updater,update_time,outbound_status,actual_qty,product_id,id,dept_id
 */
@Data
public class WmsOutboundItemRespDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 出库单ID
     */
    private Long outboundId;

    /**
     * 标准产品ID
     */
    private Long productId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 更新人姓名
     */
    private String updaterName;

//    /**
//     * 产品
//     */
//    private WmsProductRespSimpleVO product;
/// *
//     * 出库单
//     */
//    private WmsInboundRespVO outbound;

    /**
     * WMS出库状态 ; WmsOutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库
     */
    private Integer outboundStatus;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 更新者
     */
    private String updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 出库库位ID，在创建时指定；bin_id 和 inbount_item_id 需要指定其中一个，优先使用 inbount_item_id
     */
    private Long binId;

    /**
     * 实际出库量
     */
    private Integer actualQty;

    /**
     * 计划出库量
     */
    private Integer planQty;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 库位
     */
//    private WmsWarehouseBinRespVO bin;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源明细行ID
     */
    private Long upstreamId;

//    /**
//     * 部门
//     */
//    private DeptSimpleRespVO dept;
/// *
//     * 财务公司
//     */
//    private FmsCompanySimpleRespVO company;
}
