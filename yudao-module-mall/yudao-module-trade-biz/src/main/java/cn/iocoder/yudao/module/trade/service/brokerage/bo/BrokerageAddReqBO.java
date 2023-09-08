package cn.iocoder.yudao.module.trade.service.brokerage.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 佣金 增加 Request BO
 *
 * @author owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageAddReqBO {

    // TODO @疯狂：bo 的话，也可以考虑加下 @Validated 注解，校验下参数；防御性下哈，虽然不一定用的到

    /**
     * 业务ID
     */
    private String bizId;
    /**
     * 佣金基数
     */
    private Integer basePrice;
    /**
     * 一级佣金（固定）
     */
    private Integer firstBrokeragePrice;
    /**
     * 二级佣金（固定）
     */
    private Integer secondBrokeragePrice;

}
