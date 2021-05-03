package cn.iocoder.yudao.framework.sms.core.client.impl.aliyun;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.impl.aliyun.AliyunSmsClient;
import cn.iocoder.yudao.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AliyunSmsClient} 的集成测试
 */
public class AliyunSmsClientIntegrationTest {

    private static AliyunSmsClient smsClient;

    @BeforeAll
    public static void before() {
        // 创建配置类
        SmsChannelProperties properties = new SmsChannelProperties();
        properties.setId(1L);
        properties.setSignature("Ballcat");
        properties.setCode(SmsChannelEnum.ALIYUN.getCode());
        properties.setApiKey(System.getenv("ALIYUN_ACCESS_KEY"));
        properties.setApiSecret(System.getenv("ALIYUN_SECRET_KEY"));
        // 创建客户端
        smsClient = new AliyunSmsClient(properties);
        smsClient.init();
    }

    @Test
    public void testSendSms() {
        List<KeyValue<String, Object>> templateParams = new ArrayList<>();
        templateParams.add(new KeyValue<>("code", "1024"));
//        templateParams.put("operation", "嘿嘿");
//        SmsResult result = smsClient.send(1L, "15601691399", "4372216", templateParams);
        SmsCommonResult<SmsSendRespDTO> result = smsClient.sendSms(1L, "15601691399",
                "SMS_207945135", templateParams);
        System.out.println(result);
    }

    @Test
    public void testGetSmsTemplate() {
        String apiTemplateId = "SMS_2079451351";
        SmsCommonResult<SmsTemplateRespDTO> result = smsClient.getSmsTemplate(apiTemplateId);
        System.out.println(result);
    }

}
