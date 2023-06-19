package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kdniao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 快递鸟快递查询 Req DTO
 *
 * @author jason
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KdNiaoExpressQueryReqDTO {

    /**
     * 快递公司编码
     */
    @JsonProperty("ShipperCode")
    private String expressCode;
    /**
     * 快递单号
     */
    @JsonProperty("LogisticCode")
    private String logisticsNo;
    /**
     * 订单编号
     */
    @JsonProperty("OrderCode")
    private String orderNo;

}
