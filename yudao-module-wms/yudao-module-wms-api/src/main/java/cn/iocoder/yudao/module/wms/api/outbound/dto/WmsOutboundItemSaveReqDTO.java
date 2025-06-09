package cn.iocoder.yudao.module.wms.api.outbound.dto;

import lombok.Data;
import lombok.NonNull;


@Data
public class WmsOutboundItemSaveReqDTO {

    /**
     * 标准产品ID
     **/
    private Long productId;

    /**
     * 实际出库量
     **/
    private Integer actualQty;

    /**
     * 计划出库量
     **/
    private Integer planQty;

    /**
     * 库存财务公司ID
     **/
    @NonNull
    private Long companyId;

    /**
     * 库存归属部门ID
     **/
    @NonNull
    private Long deptId;

    /**
     * 备注
     **/
    private String remark;

    /**
     * 来源明细行ID
     **/
    private Long upstreamId;

    public WmsOutboundItemSaveReqDTO() {

    }
}
