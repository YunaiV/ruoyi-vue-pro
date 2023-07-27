package cn.iocoder.yudao.framework.pay.core.client.impl.mock;

import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import lombok.Data;

import javax.validation.Validator;

/**
 * 模拟支付的 PayClientConfig 实现类
 *
 * @author jason
 */
@Data
public class MockPayClientConfig implements PayClientConfig {

    /**
     * 配置名称,如果不加任何属性, JsonUtils.parseObject2 解析会报错. 暂时加个名称
     */
    private String name;

    @Override
    public void validate(Validator validator) {
        // 模拟支付配置无需校验
    }
}