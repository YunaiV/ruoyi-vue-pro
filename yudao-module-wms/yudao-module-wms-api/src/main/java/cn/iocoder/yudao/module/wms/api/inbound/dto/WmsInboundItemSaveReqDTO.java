package cn.iocoder.yudao.module.wms.api.inbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WmsInboundItemSaveReqDTO {

    /**
     * 主键
     */
    private Long productId;

    /**
     * 计划入库量
     */
    private Integer planQty;

    /**
     * 实际入库量
     */
    private Integer actualQty;

    /**
     * 库存归属部门
     **/
    private Long deptId;

    /**
     * 库存财务公司ID
     **/
    private Long companyId;

    /**
     * 备注
     **/
    private String remark;

    /**
     * 来源明细行ID
     **/
    private Long upstreamId;

}
