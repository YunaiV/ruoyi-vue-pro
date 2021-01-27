package cn.iocoder.dashboard.framework.msg.sms.proxy;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.dashboard.framework.msg.sms.SmsBody;
import cn.iocoder.dashboard.framework.msg.sms.SmsResult;
import cn.iocoder.dashboard.framework.msg.sms.SmsSender;
import cn.iocoder.dashboard.framework.msg.sms.intercepter.AbstractSmsIntercepterChain;

import java.util.Collection;

/**
 * 消息父接口
 *
 * @author zzf
 * @date 2021/1/22 15:46
 */
public class DefaultSmsSenderProxy<R> implements SmsSender<R> {

    private final SmsSender<R> smsSender;
    private final AbstractSmsIntercepterChain chain;

    @Override
    public SmsResult<R> send(SmsBody msgBody, Collection<String> targets) {
        if (ObjectUtil.isNotNull(chain) && ObjectUtil.isNotEmpty(chain.getIntercepterList())) {
            chain.getIntercepterList().forEach(s -> s.beforeSender(msgBody, targets));
        }

        SmsResult<R> resultBody = smsSender.send(msgBody, targets);

        if (ObjectUtil.isNotNull(chain) && ObjectUtil.isNotEmpty(chain.getIntercepterList())) {
            chain.getIntercepterList().forEach(s -> s.afterSender(msgBody, targets, resultBody));
        }
        return resultBody;
    }

    public DefaultSmsSenderProxy(SmsSender<R> smsSender,
                                 AbstractSmsIntercepterChain chain) {
        this.smsSender = smsSender;
        this.chain = chain;
    }
}
