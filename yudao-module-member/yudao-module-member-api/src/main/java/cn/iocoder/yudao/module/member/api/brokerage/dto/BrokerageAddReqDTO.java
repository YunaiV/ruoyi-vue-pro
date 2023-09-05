package cn.iocoder.yudao.module.member.api.brokerage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 佣金 增加 Request DTO
 * @author owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageAddReqDTO {
    /**
     * 业务ID
     */
    private String bizId;
    /**
     * 商品支付价格
     */
    private Integer payPrice;
    /**
     * SKU 一级佣金
     */
    private Integer skuFirstBrokeragePrice;
    /**
     * SKU 二级佣金
     */
    private Integer skuSecondBrokeragePrice;
    /**
     * 购买数量
     */
    private Integer count;
}
