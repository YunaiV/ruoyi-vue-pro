package cn.iocoder.yudao.framework.pay.core.client;

/**
 * 支付客户端的工厂接口
 *
 * @author 芋道源码
 */
public interface PayClientFactory {

    /**
     * 获得支付客户端
     *
     * @param channelId 渠道编号
     * @return 支付客户端
     */
     PayClient getPayClient(Long channelId);

    /**
     * 创建支付客户端
     *
     * @param channelId 渠道编号
     * @param channelCode 渠道编码
     * @param config 支付配置
     */
     void createOrUpdatePayClient(Long channelId, String channelCode, PayClientConfig config);

}
