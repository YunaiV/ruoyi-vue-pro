package cn.iocoder.yudao.framework.sms.core.client.impl.yunpian;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.impl.yunpian.YunpianSmsClient;
import cn.iocoder.yudao.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link YunpianSmsClient} 的集成测试
 */
public class YunpianSmsClientIntegrationTest {

    private static YunpianSmsClient smsClient;

    @BeforeAll
    public static void init() {
        // 创建配置类
        SmsChannelProperties properties = new SmsChannelProperties();
        properties.setId(1L);
        properties.setSignature("芋道");
        properties.setCode(SmsChannelEnum.YUN_PIAN.getCode());
        properties.setApiKey("1555a14277cb8a608cf45a9e6a80d510");
        // 创建客户端
        smsClient = new YunpianSmsClient(properties);
        smsClient.init();
    }

    @Test
    public void testSendSms() {
        List<KeyValue<String, Object>> templateParams = new ArrayList<>();
        templateParams.add(new KeyValue<>("code", "1024"));
        templateParams.add(new KeyValue<>("operation", "嘿嘿"));
//        SmsResult result = smsClient.send(1L, "15601691399", "4372216", templateParams);
        SmsCommonResult<SmsSendRespDTO> result = smsClient.sendSms(1L, "15601691399", "4383920", templateParams);
        System.out.println(result);
    }

    @Test
    public void testGetSmsTemplate() {
        String apiTemplateId = "4383920";
        SmsCommonResult<SmsTemplateRespDTO> result = smsClient.getSmsTemplate(apiTemplateId);
        System.out.println(result);
    }

}
