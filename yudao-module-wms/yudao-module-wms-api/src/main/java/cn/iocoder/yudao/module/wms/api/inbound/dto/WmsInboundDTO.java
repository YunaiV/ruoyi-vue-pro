package cn.iocoder.yudao.module.wms.api.inbound.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 入库单类型
     */
    private Integer type;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 跟踪号
     */
    private String traceNo;

    /**
     * 运输方式，1-海运；2-火车；3-空运；4、集卡
     */
    private Integer shippingMethod;

    /**
     * 初始库龄
     */
    private Integer initAge;

    /**
     * 入库单类型 ; InboundStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过
     */
    private Integer auditStatus;

    /**
     * 入库状态
     */
    private Integer inboundStatus;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 实际到货时间
     */
    private LocalDateTime arrivalActualTime;

    /**
     * 计划到货时间
     */
    private LocalDateTime arrivalPlanTime;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 单据号
     */
    private String code;

    /**
     * 来源单据ID
     */
    private Long upstreamId;

    /**
     * 来源单据编码
     */
    private String upstreamCode;

    /**
     * WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单
     */
    private Integer upstreamType;

    /**
     * 上架状态
     */
    private Integer shelveStatus;

    /**
     * 特别说明，创建方专用
     */
    private String remark;

    /**
     * 详情列表
     */
    private List<WmsInboundItemRespDTO> itemList;
}
