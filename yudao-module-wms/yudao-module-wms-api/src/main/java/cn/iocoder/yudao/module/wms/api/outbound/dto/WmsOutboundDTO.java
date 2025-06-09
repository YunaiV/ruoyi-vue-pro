package cn.iocoder.yudao.module.wms.api.outbound.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Data

@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOutboundDTO {

    /**
     * 出库单ID
     */
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 状态
     */
    private Integer outboundStatus;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 库存归属部门ID
     */
    private Long deptId;

    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;

    /**
     * 出库动作ID，与flow关联
     */
    private Long latestOutboundActionId;

    /**
     * 备注
     */
    private String remark;

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

    private List<WmsOutboundItemRespDTO> itemList;
}
