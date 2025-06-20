package cn.iocoder.yudao.module.wms.api.outbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jisencai
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WmsOutboundValidateReqDTO {

    /**
     * 产品ID
     **/
    private Long productId;

    /**
     * 产品名称
     **/
    private String productName;

    /**
     * 数量
     **/
    private Integer quantity;
}
