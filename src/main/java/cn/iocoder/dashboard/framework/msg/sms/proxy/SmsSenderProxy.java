package cn.iocoder.dashboard.framework.msg.sms.proxy;

import cn.iocoder.dashboard.framework.msg.sms.SmsSender;
import cn.iocoder.dashboard.framework.msg.sms.intercepter.SmsIntercepter;

import java.util.Collections;
import java.util.List;

/**
 * 消息父接口
 *
 * @author zzf
 * @date 2021/1/22 15:46
 */
public interface SmsSenderProxy<R> extends SmsSender<R> {

    /**
     * 添加短信拦截器
     *
     * @param smsIntercepter 短信拦截器
     */
    default void addSmsIntercepter(SmsIntercepter smsIntercepter) {
        addSmsIntercepter(Collections.singletonList(smsIntercepter));
    }

    /**
     * 添加短信拦截器
     *
     * @param smsIntercepterList 短信拦截器数组
     */
    void addSmsIntercepter(List<SmsIntercepter> smsIntercepterList);


}
