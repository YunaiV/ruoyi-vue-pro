package cn.iocoder.yudao.module.srm.config;

import cn.iocoder.yudao.framework.common.util.concurrent.AsyncTask;
import cn.iocoder.yudao.module.srm.enums.SrmChannelEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

/**
 * 消息通道配置类，生产端维护，TODO 待优化
 */
@Configuration
public class SrmMessageChannelConfig {

    /**
     * 供应商消息通道
     */
    @Bean(SrmChannelEnum.SUPPLIER)
    public MessageChannel supplierChannel() {
        return new PublishSubscribeChannel(AsyncTask.DEFAULT.getExecutor().getThreadPoolExecutor());
    }

    /**
     * 采购订单消息通道
     */
    @Bean(SrmChannelEnum.PURCHASE_ORDER)
    public MessageChannel purchaseOrderChannel() {
        return new PublishSubscribeChannel(AsyncTask.DEFAULT.getExecutor().getThreadPoolExecutor());
    }

    /**
     * 采购入库单消息通道
     */
    @Bean(SrmChannelEnum.PURCHASE_IN)
    public MessageChannel purchaseInChannel() {
        return new PublishSubscribeChannel(AsyncTask.DEFAULT.getExecutor().getThreadPoolExecutor());
    }

    /**
     * 采购退货单消息通道
     */
    @Bean(SrmChannelEnum.PURCHASE_RETURN)
    public MessageChannel purchaseReturnChannel() {
        return new PublishSubscribeChannel(AsyncTask.DEFAULT.getExecutor().getThreadPoolExecutor());
    }

} 