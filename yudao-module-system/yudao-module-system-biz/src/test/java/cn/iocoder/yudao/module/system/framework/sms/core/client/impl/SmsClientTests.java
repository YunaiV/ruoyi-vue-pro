package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.module.system.framework.sms.core.client.SmsClient;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 各种 {@link SmsClient} 的集成测试
 *
 * @author 芋道源码
 */
public class SmsClientTests {

    // ========== 阿里云 ==========
    @Test
    @Disabled
    public void testAliyunSmsClient_getSmsTemplate() throws Throwable {
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey(System.getenv("SMS_ALIYUN_ACCESS_KEY"))
                .setApiSecret(System.getenv("SMS_ALIYUN_SECRET_KEY"));
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
                .setApiKey(System.getenv("SMS_ALIYUN_ACCESS_KEY"))
                .setApiSecret(System.getenv("SMS_ALIYUN_SECRET_KEY"))
                .setSignature("Ballcat");
        AliyunSmsClient client = new AliyunSmsClient(properties);
        // 准备参数
        Long sendLogId = System.currentTimeMillis();
        String mobile = "15601691323";
        String apiTemplateId = "SMS_207945135";
        // 调用
        SmsSendRespDTO sendRespDTO = client.sendSms(sendLogId, mobile, apiTemplateId, List.of(new KeyValue<>("code", "1024")));
        // 打印结果
        System.out.println(sendRespDTO);
    }

    // ========== 腾讯云 ==========

    @Test
    @Disabled
    public void testTencentSmsClient_sendSms() throws Throwable {
        String sdkAppId = "1400500458";
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey(System.getenv("SMS_TENCENT_ACCESS_KEY") + " " + sdkAppId)
                .setApiSecret(System.getenv("SMS_TENCENT_SECRET_KEY"))
                .setSignature("芋道源码");
        TencentSmsClient client = new TencentSmsClient(properties);
        // 准备参数
        Long sendLogId = System.currentTimeMillis();
        String mobile = "15601691323";
        String apiTemplateId = "358212";
        // 调用
        SmsSendRespDTO sendRespDTO = client.sendSms(sendLogId, mobile, apiTemplateId, List.of(new KeyValue<>("code", "1024")));
        // 打印结果
        System.out.println(sendRespDTO);
    }

    @Test
    @Disabled
    public void testTencentSmsClient_getSmsTemplate() throws Throwable {
        String sdkAppId = "1400500458";
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey(System.getenv("SMS_TENCENT_ACCESS_KEY") + " " + sdkAppId)
                .setApiSecret(System.getenv("SMS_TENCENT_SECRET_KEY"))
                .setSignature("芋道源码");
        TencentSmsClient client = new TencentSmsClient(properties);
        // 准备参数
        String apiTemplateId = "358212";
        // 调用
        SmsTemplateRespDTO template = client.getSmsTemplate(apiTemplateId);
        // 打印结果
        System.out.println(template);
    }

    // ========== 华为云 ==========

    @Test
    @Disabled
    public void testHuaweiSmsClient_sendSms() throws Throwable {
        String sender = "x8824060312575";
        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey(System.getenv("SMS_HUAWEI_ACCESS_KEY") + " " + sender)
                .setApiSecret(System.getenv("SMS_HUAWEI_SECRET_KEY"))
                .setSignature("runpu");
        HuaweiSmsClient client = new HuaweiSmsClient(properties);
        // 准备参数
        Long sendLogId = System.currentTimeMillis();
        String mobile = "17321315478";
        String apiTemplateId = "3644cdab863546a3b718d488659a99ef";
        List<KeyValue<String, Object>> templateParams = List.of(new KeyValue<>("code", "1024"));
        // 调用
        SmsSendRespDTO smsSendRespDTO = client.sendSms(sendLogId, mobile, apiTemplateId, templateParams);
        // 打印结果
        System.out.println(smsSendRespDTO);
    }

    // ========== 七牛云 ==========

    @Test
    @Disabled
    public void testQiniuSmsClient_sendSms() throws Throwable {

        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("SMS_QINIU_ACCESS_KEY")
                .setApiSecret("SMS_QINIU_SECRET_KEY");
        QiniuSmsClient client = new QiniuSmsClient(properties);
        // 准备参数
        Long sendLogId = System.currentTimeMillis();
        String mobile = "17321315478";
        String apiTemplateId = "3644cdab863546a3b718d488659a99ef";
        List<KeyValue<String, Object>> templateParams = List.of(new KeyValue<>("code", "1122"));
        // 调用
        SmsSendRespDTO smsSendRespDTO = client.sendSms(sendLogId, mobile, apiTemplateId, templateParams);
        // 打印结果
        System.out.println(smsSendRespDTO);
    }

    @Test
    @Disabled
    public void testQiniuSmsClient_getSmsTemplate() throws Throwable {

        SmsChannelProperties properties = new SmsChannelProperties()
                .setApiKey("SMS_QINIU_ACCESS_KEY")
                .setApiSecret("SMS_QINIU_SECRET_KEY");
        QiniuSmsClient client = new QiniuSmsClient(properties);
        // 准备参数
        String apiTemplateId = "3644cdab863546a3b718d488659a99ef";
        // 调用
        SmsTemplateRespDTO template = client.getSmsTemplate(apiTemplateId);
        // 打印结果
        System.out.println(template);
    }

}