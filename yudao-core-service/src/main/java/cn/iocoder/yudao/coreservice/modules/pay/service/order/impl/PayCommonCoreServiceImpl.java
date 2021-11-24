package cn.iocoder.yudao.coreservice.modules.pay.service.order.impl;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayChannelCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayCommonCoreService;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.PAY_CHANNEL_CLIENT_NOT_FOUND;
import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.PAY_CHANNEL_NOTIFY_VERIFY_FAILED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 支付通用 Core Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayCommonCoreServiceImpl implements PayCommonCoreService {

    @Resource
    private PayChannelCoreService payChannelCoreService;

    @Resource
    private PayClientFactory payClientFactory;

    @Override
    public void verifyNotifyData(Long channelId, PayNotifyDataDTO notifyData) {
        // 校验支付渠道是否有效
        PayChannelDO channel = payChannelCoreService.validPayChannel(channelId);
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[notifyPayOrder][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }
        boolean verifyResult = client.verifyNotifyData(notifyData);
        if(!verifyResult){
            //渠道通知验证失败
            throw exception(PAY_CHANNEL_NOTIFY_VERIFY_FAILED);
        }
    }

    @Override
    public boolean isRefundNotify(Long channelId, PayNotifyDataDTO notifyData) {
        // 校验支付渠道是否有效
        PayChannelDO channel = payChannelCoreService.validPayChannel(channelId);
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[notifyPayOrder][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }
        return client.isRefundNotify(notifyData);
    }
}
