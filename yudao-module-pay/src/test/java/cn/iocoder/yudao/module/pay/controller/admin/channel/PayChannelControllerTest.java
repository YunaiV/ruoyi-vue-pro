package cn.iocoder.yudao.module.pay.controller.admin.channel;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.filter.ApiAccessLogFilter;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;

class PayChannelControllerTest {

    @Test
    void shouldSanitizeConfigWhenCreateChannel() throws NoSuchMethodException {
        // 准备参数：微信支付配置使用真实的客户端配置类序列化成 JSON 字符串
        WxPayClientConfig config = new WxPayClientConfig();
        config.setMchId("fake-mch-id");
        config.setMchKey("FAKE_MCH_KEY");
        config.setKeyContent("FAKE_KEY_CONTENT");
        config.setPrivateKeyContent("FAKE_PRIVATE_KEY_CONTENT");
        config.setApiV3Key("FAKE_API_V3_KEY");
        PayChannelCreateReqVO reqVO = new PayChannelCreateReqVO();
        reqVO.setCode("wx_pub");
        reqVO.setRemark("keep-remark");
        reqVO.setConfig(JsonUtils.toJsonString(config));

        // 调用
        String result = sanitize("createChannel", PayChannelCreateReqVO.class, JsonUtils.toJsonString(reqVO));

        // 断言：config 字符串整体移除，其它字段保留
        assertFalse(result.contains("FAKE_MCH_KEY"));
        assertFalse(result.contains("FAKE_KEY_CONTENT"));
        assertFalse(result.contains("FAKE_PRIVATE_KEY_CONTENT"));
        assertFalse(result.contains("FAKE_API_V3_KEY"));
        assertTrue(result.contains("wx_pub"));
        assertTrue(result.contains("keep-remark"));
    }

    @Test
    void shouldSanitizeConfigWhenUpdateChannel() throws NoSuchMethodException {
        // 准备参数：支付宝配置使用真实的客户端配置类序列化成 JSON 字符串
        AlipayPayClientConfig config = new AlipayPayClientConfig();
        config.setAppId("fake-app-id");
        config.setPrivateKey("FAKE_PRIVATE_KEY");
        config.setEncryptKey("FAKE_ENCRYPT_KEY");
        PayChannelUpdateReqVO reqVO = new PayChannelUpdateReqVO();
        reqVO.setId(1L);
        reqVO.setRemark("keep-remark");
        reqVO.setConfig(JsonUtils.toJsonString(config));

        // 调用
        String result = sanitize("updateChannel", PayChannelUpdateReqVO.class, JsonUtils.toJsonString(reqVO));

        // 断言
        assertFalse(result.contains("FAKE_PRIVATE_KEY"));
        assertFalse(result.contains("FAKE_ENCRYPT_KEY"));
        assertTrue(result.contains("keep-remark"));
    }

    private static String sanitize(String methodName, Class<?> parameterType, String body)
            throws NoSuchMethodException {
        ApiAccessLog annotation = ApiAccessLogFilter.getApiAccessLogAnnotation(new HandlerMethod(
                new PayChannelController(), PayChannelController.class.getMethod(methodName, parameterType)));
        assertNotNull(annotation);
        String result = ApiAccessLogFilter.sanitizeJson(body, annotation.sanitizeKeys());
        assertNotNull(result);
        return result;
    }

}
