package cn.iocoder.yudao.module.wms.api.outbound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


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
    private Long companyId;

    /**
     * 库存归属部门ID
     **/
    @NotNull(message = "出库单明细行归属部门ID不能为空")
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
