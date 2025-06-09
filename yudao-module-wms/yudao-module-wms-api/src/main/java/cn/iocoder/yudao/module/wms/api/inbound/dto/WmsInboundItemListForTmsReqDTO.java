package cn.iocoder.yudao.module.wms.api.inbound.dto;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

/**
 * @author jisencai
 */
@Data
public class WmsInboundItemListForTmsReqDTO {

    /**
     * 仓库ID
     */
    @NonNull
    private Long warehouseId;

    /**
     * 公司ID
     */
    @NonNull
    private Long companyId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 货物详情
     */
    private List<WmsInboundItemRespDTO> inboundDoList;

}
