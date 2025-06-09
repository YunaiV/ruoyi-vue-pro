package cn.iocoder.yudao.module.wms.api.inbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuppressWarnings("ALL")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WmsInboundSaveReqDTO {

    /**
     * WMS入库单类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单 , 3-换货单
     * <p>
     * {@link  cn.iocoder.yudao.module.system.enums.somle.BillType}
     **/
    private Integer type;

    /**
     * 仓库ID
     **/
    private Long warehouseId;

    /**
     * 跟踪号
     **/
    private String traceNo;

    /**
     * WMS运输方式 ; WmsShippingMethod : 0-海运 , 1-铁路 , 2-空运 , 3-集卡
     **/
    private Integer shippingMethod;

    /**
     * 初始库龄
     **/
    private Integer initAge;

    /**
     * 详情清单
     **/
    private List<WmsInboundItemSaveReqDTO> itemList;


    /**
     * 库存财务公司ID
     **/
    private Long companyId;

    /**
     * 实际到货时间
     **/
    private LocalDateTime arrivalActualTime;

    /**
     * 计划到货时间
     **/
    private LocalDateTime arrivalPlanTime;

    /**
     * 入库时间
     **/
    private LocalDateTime inboundTime;

    /**
     * 库存归属部门ID
     **/
    private Long deptId;

    /**
     * 来源单据ID
     **/
    private Long upstreamId;

    /**
     * 来源单据编码
     **/
    private String upstreamCode;

    /**
     * WMS来源单据类型
     **/
    private Integer upstreamType;

    /**
     * WMS入库单上架状态
     **/
    private Integer shelveStatus;

    /**
     * 备注
     **/
    private String remark;

    /**
     * WMS入库单审批状态 ; WmsInboundAuditStatus : 0-草稿 , 1-待入库 , 2-驳回 , 3-已入库 , 4-强制入库 , 5-作废
     **/
    private Integer auditStatus;
}
