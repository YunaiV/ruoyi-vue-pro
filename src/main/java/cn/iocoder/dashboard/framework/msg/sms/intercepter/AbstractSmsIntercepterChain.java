package cn.iocoder.dashboard.framework.msg.sms.intercepter;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息父接口
 *
 * @author zzf
 * @date 2021/1/22 15:46
 */
public abstract class AbstractSmsIntercepterChain {

    @Getter
    protected final List<SmsIntercepter> intercepterList = new ArrayList<>(8);


    /**
     * 添加短信拦截器
     *
     * @param smsIntercepter 短信拦截器
     */
    public void addSmsIntercepter(SmsIntercepter smsIntercepter) {
        addSmsIntercepter(Collections.singletonList(smsIntercepter));
    }

    /**
     * 添加短信拦截器
     *
     * @param smsIntercepterList 短信拦截器数组
     */
    abstract void addSmsIntercepter(List<SmsIntercepter> smsIntercepterList);


}
