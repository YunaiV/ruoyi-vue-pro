package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.framework.sms.core.SmsBody;

import javax.servlet.ServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 短信Service接口
 * 只支持异步，因此没有返回值
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
     */
    void send(SmsBody smsBody, List<String> targetPhones);

    /**
     * 发送消息
     *
     * @param smsBody     消息内容
     * @param targetPhone 发送对象手机号
     */
    default void send(SmsBody smsBody, String targetPhone) {
        send(smsBody, Collections.singletonList(targetPhone));
    }

    /**
     * 发送消息
     *
     * @param smsBody      消息内容
     * @param targetPhones 发送对象手机号数组
     */
    default void send(SmsBody smsBody, String... targetPhones) {
        send(smsBody, Arrays.asList(targetPhones));
    }

    /**
     * 处理短信发送回调函数
     *
     * @param request 请求
     * @return 响应数据
     */
    Object smsSendCallbackHandle(ServletRequest request);
}
