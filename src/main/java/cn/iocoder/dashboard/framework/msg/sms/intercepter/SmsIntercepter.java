package cn.iocoder.dashboard.framework.msg.sms.intercepter;

import cn.iocoder.dashboard.framework.msg.sms.SmsBody;
import cn.iocoder.dashboard.framework.msg.sms.SmsResult;

import java.util.Collection;

/**
 * 消息父接口
 *
 * @author zzf
 * @date 2021/1/22 15:46
 */
public interface SmsIntercepter {

    /**
     * 监听发送前
     *
     * @param msgBody 消息体
     * @param targets 发送对象数组
     */
    void beforeSender(SmsBody msgBody, Collection<String> targets);

    /**
     * 监听发送后
     *
     * @param msgBody    消息体
     * @param targets    发送对象数组
     * @param resultBody 返回对象
     */
    <T> void afterSender(SmsBody msgBody, Collection<String> targets, SmsResult<T> resultBody);

    /**
     * 排序值，拦截器根据order值顺序执行
     * <p>
     * 值越小，越早执行
     *
     * @return 排序值
     */
    int getOrder();
}
