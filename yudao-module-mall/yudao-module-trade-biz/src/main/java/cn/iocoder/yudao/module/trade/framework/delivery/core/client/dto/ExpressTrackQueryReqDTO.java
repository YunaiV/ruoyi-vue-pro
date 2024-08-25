package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import lombok.Data;

/**
 * 快递轨迹的查询 Req DTO
 *
 * @author jason
 */
@Data
public class ExpressTrackQueryReqDTO {

    /**
     * 快递公司编码
     * <p>
     * 对应 {@link DeliveryExpressDO#getCode()}
     */
    private String expressCode;

    /**
     * 发货快递单号
     */
    private String logisticsNo;

    /**
     * 收、寄件人的电话号码
     */
    private String phone;

    /**
     * 自定义名称（顺丰专用）
     */
    private String customerName;

    public ExpressTrackQueryReqDTO setExpressCode(String expressCode) {
        this.expressCode = expressCode;
        updateCustomerName();
        return this; // 返回实体对象
    }

    public ExpressTrackQueryReqDTO setPhone(String phone) {
        this.phone = phone;
        updateCustomerName();
        return this; // 返回实体对象
    }

    private void updateCustomerName() {
        if ("SF".equals(expressCode) && phone != null && phone.length() >= 4) {
            this.customerName = phone.substring(phone.length() - 4);
        }
    }
}
