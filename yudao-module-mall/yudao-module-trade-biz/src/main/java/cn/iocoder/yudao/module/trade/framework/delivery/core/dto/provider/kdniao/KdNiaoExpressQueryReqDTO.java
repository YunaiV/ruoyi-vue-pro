package cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kdniao;

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

    // TODO @jaosn：要不改成 expressCode；项目里使用这个哈
    /**
     * 快递公司编码
     */
    @JsonProperty("ShipperCode")
    private String expressCompanyCode;
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
