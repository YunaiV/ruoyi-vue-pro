package cn.iocoder.dashboard.framework.sms.core.client.impl.aliyun;

import cn.iocoder.dashboard.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link AliyunSmsClient} 的集成测试
 */
public class AliyunSmsClientTest {

    @Test
    public void testSend() {
        // 创建配置类
        SmsChannelProperties properties = new SmsChannelProperties();
        properties.setId(1L);
        properties.setSignature("Ballcat");
        properties.setCode(SmsChannelEnum.ALIYUN.getCode());
        properties.setApiKey(System.getenv("ALIYUN_ACCESS_KEY"));
        properties.setApiSecret(System.getenv("ALIYUN_SECRET_KEY"));
        // 创建客户端
        AliyunSmsClient smsClient = new AliyunSmsClient(properties);
        smsClient.init();
        // 发送短信
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("code", "1024");
//        templateParams.put("operation", "嘿嘿");
//        SmsResult result = smsClient.send(1L, "15601691399", "4372216", templateParams);
        SmsCommonResult result = smsClient.send(1L, "15601691399", "SMS_207945135", templateParams);
        System.out.println(result);
    }

}
