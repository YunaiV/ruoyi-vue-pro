package cn.iocoder.yudao.module.wms.api.outbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WmsOutboundSaveReqDTO {


    /**
     * 仓库ID
     **/
    private Long warehouseId;

    /**
     * WMS出库单类型
     **/
    private Integer type;

    /**
     * 详情清单
     **/
    private List<WmsOutboundItemSaveReqDTO> itemList;


    /**
     * 库存财务公司ID
     **/
    private Long companyId;

    /**
     * 库存归属部门ID
     **/
    private Long deptId;

    /**
     * 备注
     **/
    private String remark;

    /**
     * 来源单据ID
     **/
    private Long upstreamId;

    /**
     * 来源单据编码
     **/
    private String upstreamCode;

    /**
     * 来源单据类型
     **/
    private Integer upstreamType;
}
