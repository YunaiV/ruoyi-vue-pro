package cn.iocoder.yudao.module.trade.framework.delivery.core.client;

import cn.iocoder.yudao.module.trade.framework.delivery.core.enums.ExpressClientEnum;

/**
 * 快递客户端工厂接口：用于创建和缓存快递客户端
 *
 * @author jason
 */
public interface ExpressClientFactory {

    /**
     * 获取默认的快递客户端
     */
    ExpressClient getDefaultExpressClient();

    /**
     * 通过枚举获取快递客户端，如果不存在，就创建一个对应快递客户端
     *
     * @param clientEnum 快递客户端枚举
     */
    ExpressClient getOrCreateExpressClient(ExpressClientEnum clientEnum);

}
