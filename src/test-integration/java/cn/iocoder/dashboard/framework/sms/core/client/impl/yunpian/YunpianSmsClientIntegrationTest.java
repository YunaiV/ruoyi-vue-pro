package cn.iocoder.dashboard.framework.sms.core.client.impl.yunpian;

import cn.iocoder.dashboard.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link YunpianSmsClient} 的集成测试
 */
public class YunpianSmsClientIntegrationTest {

    @Test
    public void testSend() {
        // 创建配置类
        SmsChannelProperties properties = new SmsChannelProperties();
        properties.setId(1L);
        properties.setSignature("芋道");
        properties.setCode(SmsChannelEnum.YUN_PIAN.getCode());
        properties.setApiKey("1555a14277cb8a608cf45a9e6a80d510");
        // 创建客户端
        YunpianSmsClient smsClient = new YunpianSmsClient(properties);
        smsClient.init();
        // 发送短信
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("code", "1024");
        templateParams.put("operation", "嘿嘿");
//        SmsResult result = smsClient.send(1L, "15601691399", "4372216", templateParams);
        SmsCommonResult result = smsClient.send(1L, "15601691399", "4383920", templateParams);
        System.out.println(result);
    }

}
