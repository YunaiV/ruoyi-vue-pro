package cn.iocoder.yudao.module.trade.framework.delivery.core;

/**
 * 快递服务商工厂， 用于创建和缓存快递服务商服务
 * @author jason
 */
public interface ExpressQueryProviderFactory {

    /**
     * 通过枚举获取快递查询服务商， 如果不存在。就创建一个对应的快递查询服务商
     * @param queryProviderEnum 快递服务商枚举
     */
    ExpressQueryProvider getOrCreateExpressQueryProvider(ExpressQueryProviderEnum queryProviderEnum);
}
