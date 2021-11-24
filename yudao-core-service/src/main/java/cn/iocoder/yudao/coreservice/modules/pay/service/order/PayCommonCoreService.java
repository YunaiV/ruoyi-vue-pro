package cn.iocoder.yudao.coreservice.modules.pay.service.order;

import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;

/**
 * 支付通用 Core Service
 *
 * @author jason
 */
public interface PayCommonCoreService {

    /**
     * 验证是否是渠道通知
     * @param notifyData 通知数据
     */
    void verifyNotifyData(Long channelId, PayNotifyDataDTO notifyData);

    /**
     * 支付宝的支付回调通知，和退款回调通知 地址是同一个
     * 是否是退款回调通知
     * @param notifyData  通知数据
     * @return
     */
    boolean isRefundNotify(Long channelId, PayNotifyDataDTO notifyData);
}
