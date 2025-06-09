package cn.iocoder.yudao.module.wms.api.inbound.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jisencai
 */
@Data
public class WmsInboundItemRespDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 入库单ID
     */
    private Long inboundId;

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

    /**
     * 创建者
     */
    private String creator;

    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新者
     */
    private String updater;

//    /**
//     * 产品
//     */
//    private WmsProductRespSimpleVO product;

    /**
     * WMS入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库
     */
    private Integer inboundStatus;

//    /**
//     * 入库单
//     */
//    private WmsInboundSimpleRespVO inbound;

    /**
     * 实际入库量
     */
    private Integer actualQty;

    /**
     * 库龄
     */
    private Integer age;

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
     * 上架可用量，已上架量 - 出库可用量
     */
    private Integer shelveAvailableQty;

    /**
     * 最新的流水ID
     */
    private Long latestFlowId;

//    /**
//     * 仓库
//     */
//    private WmsWarehouseSimpleRespVO warehouse;

    /**
     * 库存归属部门ID,由用户指定
     */
    private Long deptId;

//    /**
//     * 部门
//     */
//    private DeptSimpleRespVO dept;

    /**
     * 库存财务公司ID,由用户指定
     */
    private Long companyId;

    /**
     * 备注
     */
    private String remark;

//    /**
//     * 财务公司
//     */
//    private FmsCompanySimpleRespVO company;

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

//    /**
//     * 入库的部门
//     */
//    private DeptSimpleRespVO inboundDept;

    /**
     * 入库财务公司
     */
    private WmsCompanyDTO inboundCompany;

    /**
     * 当前仓库库存
     */
    private WmsStockWarehouseSimpleDTO stockWarehouse;

//    /**
//     * 上架的货位清单
//     */
//    private List<WmsWarehouseBinSimpleRespVO> warehouseBinList;

    /**
     * 上架的货位清单
     */
    private Integer stockType;

}
