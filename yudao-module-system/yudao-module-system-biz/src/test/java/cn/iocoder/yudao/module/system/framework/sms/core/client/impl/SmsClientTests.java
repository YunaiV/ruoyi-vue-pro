package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 各种 {@link SmsClientTests  集成测试
 *
 * @author 芋道源码
 */
public class SmsClientTests {

    @Test
    @Disabled
    public void testHuaweiSmsClient() throws Throwable {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("123")
                .setApiSecret("456");
        HuaweiSmsClient client = new HuaweiSmsClient(properties);
        // 准备参数
        Long sendLogId = System.currentTimeMillis();
        String mobile = "15601691323";
        String apiTemplateId = "xx test01";
        List<KeyValue<String, Object>> templateParams = ListUtil.of(new KeyValue<>("code", "1024"));
        // 调用
        SmsSendRespDTO smsSendRespDTO = client.sendSms(sendLogId, mobile, apiTemplateId, templateParams);
        // 打印结果
        System.out.println(smsSendRespDTO);
    }

}
