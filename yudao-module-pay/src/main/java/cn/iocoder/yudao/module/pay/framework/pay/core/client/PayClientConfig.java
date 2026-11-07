package cn.iocoder.yudao.module.pay.framework.pay.core.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Validator;

/**
 * 支付客户端的配置，本质是支付渠道的配置
 * 每个不同的渠道，需要不同的配置，通过子类来定义
 *
 * @author 芋道源码
 */
// 类型名沿用完整类名，兼容已有数据库配置；仅允许显式注册的配置类型，不按输入加载任意类。
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AlipayPayClientConfig.class, name = "cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay.AlipayPayClientConfig"),
        @JsonSubTypes.Type(value = WxPayClientConfig.class, name = "cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin.WxPayClientConfig"),
        @JsonSubTypes.Type(value = NonePayClientConfig.class, name = "cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.NonePayClientConfig")
})
@JsonIgnoreProperties(ignoreUnknown = true) // 目的：忽略未知的属性，避免反序列化失败
public interface PayClientConfig {

    /**
     * 参数校验
     *
     * @param validator 校验对象
     */
    void validate(Validator validator);

}
