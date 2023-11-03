package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kd100;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 快递 100 快递查询 Req DTO
 *
 * @author jason
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Kd100ExpressQueryReqDTO {

    /**
     * 快递公司编码
     */
    @JsonProperty("com")
    private String expressCode;

    /**
     * 快递单号
     */
    @JsonProperty("num")
    private String logisticsNo;

    /**
     * 收、寄件人的电话号码
     */
    private String phone;

}
