package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
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

    // ========== 阿里云 ==========

    @Test
    @Disabled
    public void testAliyunSmsClient_getSmsTemplate() throws Throwable {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("LTAI5tAicJAxaSFiZuGGeXHR")
                .setApiSecret("Fdr9vadxnDvS6GJU0W1tijQ0VmLhYz");
        AliyunSmsClient client = new AliyunSmsClient(properties);
        // 准备参数
        String apiTemplateId = "SMS_207945135";
        // 调用
        SmsTemplateRespDTO template = client.getSmsTemplate(apiTemplateId);
        // 打印结果
        System.out.println(template);
    }

    @Test
    @Disabled
    public void testAliyunSmsClient_sendSms() throws Throwable {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("LTAI5tAicJAxaSFiZuGGeXHR")
                .setApiSecret("Fdr9vadxnDvS6GJU0W1tijQ0VmLhYz")
                .setSignature("runpu");
        AliyunSmsClient client = new AliyunSmsClient(properties);
        // 准备参数
        Long sendLogId = System.currentTimeMillis();
        String mobile = "15601691323";
        String apiTemplateId = "SMS_207945135";
        // 调用
        SmsSendRespDTO sendRespDTO = client.sendSms(sendLogId, mobile, apiTemplateId, ListUtil.of(new KeyValue<>("code", "1024")));
        // 打印结果
        System.out.println(sendRespDTO);
    }

    @Test
    @Disabled
    public void testAliyunSmsClient_parseSmsReceiveStatus() {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("LTAI5tAicJAxaSFiZuGGeXHR")
                .setApiSecret("Fdr9vadxnDvS6GJU0W1tijQ0VmLhYz");
        AliyunSmsClient client = new AliyunSmsClient(properties);
        // 准备参数
        String text = "[\n" +
                "  {\n" +
                "    \"phone_number\" : \"13900000001\",\n" +
                "    \"send_time\" : \"2017-01-01 11:12:13\",\n" +
                "    \"report_time\" : \"2017-02-02 22:23:24\",\n" +
                "    \"success\" : true,\n" +
                "    \"err_code\" : \"DELIVERED\",\n" +
                "    \"err_msg\" : \"用户接收成功\",\n" +
                "    \"sms_size\" : \"1\",\n" +
                "    \"biz_id\" : \"12345\",\n" +
                "    \"out_id\" : \"67890\"\n" +
                "  }\n" +
                "]";
        // mock 方法

        // 调用
        List<SmsReceiveRespDTO> statuses = client.parseSmsReceiveStatus(text);
        // 打印结果
        System.out.println(statuses);
    }

    // ========== 腾讯云 ==========

    @Test
    @Disabled
    public void testTencentSmsClient_sendSms() throws Throwable {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("LTAI5tAicJAxaSFiZuGGeXHR 1428926523")
                .setApiSecret("Fdr9vadxnDvS6GJU0W1tijQ0VmLhYz")
                .setSignature("芋道源码");
        TencentSmsClient client = new TencentSmsClient(properties);
        // 准备参数
        Long sendLogId = System.currentTimeMillis();
        String mobile = "15601691323";
        String apiTemplateId = "2136358";
        // 调用
        SmsSendRespDTO sendRespDTO = client.sendSms(sendLogId, mobile, apiTemplateId, ListUtil.of(new KeyValue<>("code", "1024")));
        // 打印结果
        System.out.println(sendRespDTO);
    }

    @Test
    @Disabled
    public void testTencentSmsClient_getSmsTemplate() throws Throwable {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("LTAI5tAicJAxaSFiZuGGeXHR 1428926523")
                .setApiSecret("Fdr9vadxnDvS6GJU0W1tijQ0VmLhYz")
                .setSignature("芋道源码");
        TencentSmsClient client = new TencentSmsClient(properties);
        // 准备参数
        String apiTemplateId = "2136358";
        // 调用
        SmsTemplateRespDTO template = client.getSmsTemplate(apiTemplateId);
        // 打印结果
        System.out.println(template);
    }

    // ========== 华为云 ==========

    @Test
    @Disabled
    public void testHuaweiSmsClient_sendSms() throws Throwable {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("123")
                .setApiSecret("456")
                .setSignature("runpu");
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

