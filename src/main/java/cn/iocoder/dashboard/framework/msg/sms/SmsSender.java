package cn.iocoder.dashboard.framework.msg.sms;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * 短信父接口
 *
 * @author zzf
 * @date 2021/1/25 14:14
 */
public interface SmsSender<R> {

    /**
     * 发送通知
     *
     * @param msgBody 通知内容
     * @param targets 发送对象列表
     * @return 是否发送成功
     */
    SmsResult<R> send(SmsBody msgBody, Collection<String> targets);

    /**
     * 发送通知
     *
     * @param msgBody 通知内容
     * @param target  发送对象列表
     * @return 是否发送成功
     */
    default SmsResult<R> send(SmsBody msgBody, String target) {
        if (StringUtils.isBlank(target)) {
            return failResult();
        }

        return send(msgBody, Collections.singletonList(target));
    }

    /**
     * 发送通知
     *
     * @param msgBody 通知内容
     * @param targets 发送对象列表
     * @return 是否发送成功
     */
    default SmsResult<R> send(SmsBody msgBody, String... targets) {
        if (targets == null) {
            return failResult();
        }

        return send(msgBody, Arrays.asList(targets));
    }

    default SmsResult<R> failResult() {
        SmsResult<R> resultBody = new SmsResult<>();
        resultBody.setSuccess(false);
        return resultBody;
    }

    default SmsResult<R> failResult(String message) {
        SmsResult<R> resultBody = failResult();
        resultBody.setMessage(message);
        return resultBody;
    }
}