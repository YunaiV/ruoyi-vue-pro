package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 短信Service接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SysSmsService {

    /**
     * 发送消息
     *
     * @param smsBody      消息内容
     * @param targetPhones 发送对象手机号列表
     * @return 是否发送成功
     */
    SmsResult send(SmsBody smsBody, List<String> targetPhones);

    /**
     * 发送消息
     *
     * @param smsBody     消息内容
     * @param targetPhone 发送对象手机号
     * @return 是否发送成功
     */
    default SmsResult send(SmsBody smsBody, String targetPhone) {
        if (StringUtils.isBlank(targetPhone)) {
            return failResult("targetPhone must not null.");
        }

        return send(smsBody, Collections.singletonList(targetPhone));
    }

    /**
     * 发送消息
     *
     * @param smsBody      消息内容
     * @param targetPhones 发送对象手机号数组
     * @return 是否发送成功
     */
    default SmsResult send(SmsBody smsBody, String... targetPhones) {
        if (targetPhones == null) {
            return failResult("targetPhones must not null.");
        }

        return send(smsBody, Arrays.asList(targetPhones));
    }


    /**
     * 异步发送消息
     *
     * @param msgBody      消息内容
     * @param targetPhones 发送对象列表
     */
    void sendAsync(SmsBody msgBody, List<String> targetPhones);

    /**
     * 异步发送消息
     *
     * @param msgBody     消息内容
     * @param targetPhone 发送对象
     */
    default void sendAsync(SmsBody msgBody, String targetPhone) {
        if (StringUtils.isBlank(targetPhone)) {
            return;
        }
        sendAsync(msgBody, Collections.singletonList(targetPhone));
    }

    /**
     * 异步发送消息
     *
     * @param msgBody      消息内容
     * @param targetPhones 发送对象列表
     */
    default void sendAsync(SmsBody msgBody, String... targetPhones) {
        if (targetPhones == null) {
            return;
        }
        sendAsync(msgBody, Arrays.asList(targetPhones));
    }


    default SmsResult failResult(String message) {
        SmsResult resultBody = new SmsResult();
        resultBody.setSuccess(false);
        resultBody.setMessage(message);
        return resultBody;
    }

}
