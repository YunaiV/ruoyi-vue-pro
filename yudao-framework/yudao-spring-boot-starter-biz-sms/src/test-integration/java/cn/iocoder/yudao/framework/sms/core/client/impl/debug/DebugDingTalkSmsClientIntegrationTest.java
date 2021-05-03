package cn.iocoder.yudao.framework.sms.core.client.impl.debug;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.impl.debug.DebugDingTalkSmsClient;
import cn.iocoder.yudao.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DebugDingTalkSmsClient} 的集成测试
 */
public class DebugDingTalkSmsClientIntegrationTest {

    private static DebugDingTalkSmsClient smsClient;

    @BeforeAll
    public static void init() {
        // 创建配置类
        SmsChannelProperties properties = new SmsChannelProperties();
        properties.setId(1L);
        properties.setSignature("芋道");
        properties.setCode(SmsChannelEnum.DEBUG_DING_TALK.getCode());
        properties.setApiKey("696b5d8ead48071237e4aa5861ff08dbadb2b4ded1c688a7b7c9afc615579859");
        properties.setApiSecret("SEC5c4e5ff888bc8a9923ae47f59e7ccd30af1f14d93c55b4e2c9cb094e35aeed67");
        // 创建客户端
        smsClient = new DebugDingTalkSmsClient(properties);
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

}
