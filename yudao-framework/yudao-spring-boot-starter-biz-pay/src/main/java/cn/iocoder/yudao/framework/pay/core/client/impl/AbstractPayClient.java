package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayCodeMapping;

/**
 * 支付客户端的抽象类，提供模板方法，减少子类的冗余代码
 *
 * @author 芋道源码
 */
public abstract class AbstractPayClient implements PayClient {

    /**
     * 错误码枚举类
     */
    protected PayCodeMapping codeMapping;

    protected Double calculateAmount(Integer amount) {
        return amount / 100.0;
    }

}
