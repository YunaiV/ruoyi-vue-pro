package cn.iocoder.yudao.module.trade.framework.delivery.core.dto;

import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import lombok.Data;

/**
 * 快递查询 Req DTO
 *
 * @author jason
 */
@Data
public class ExpressQueryReqDTO {

    /**
     * 快递公司编码
     *
     * 对应 {@link DeliveryExpressDO#getCode()} }
     */
    private String expressCompanyCode;

    /**
     * 发货快递单号
     */
    private String logisticsNo;

    /**
     * 收、寄件人的电话号码
     */
    private String phone;
}
