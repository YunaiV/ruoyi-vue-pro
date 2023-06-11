package cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kd100;

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

    // TODO @jaosn：要不改成 expressCode；项目里使用这个哈
    /**
     * 快递公司编码
     */
    @JsonProperty("com")
    private String expressCompanyCode;

    /**
     * 快递单号
     */
    @JsonProperty("num")
    private String logisticsNo;

    /**
     * 收、寄件人的电话号码
     */
    private String phone;
    /**
     * 出发地城市
     */
    private String from;
    /**
     * 目的地城市，到达目的地后会加大监控频率
     */
    private String to;

    /**
     * 返回结果排序
     *
     * desc 降序（默认）, asc 升序
     */
    private String order;

}
