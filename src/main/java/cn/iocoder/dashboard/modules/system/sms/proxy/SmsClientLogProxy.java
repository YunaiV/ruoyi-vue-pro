package cn.iocoder.dashboard.modules.system.sms.proxy;

import cn.iocoder.dashboard.framework.sms.SmsBody;
import cn.iocoder.dashboard.framework.sms.SmsClient;
import cn.iocoder.dashboard.framework.sms.SmsResult;
import cn.iocoder.dashboard.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 消息父接口
 *
 * @author zzf
 * @date 2021/1/22 15:46
 */
@Slf4j
public class SmsClientLogProxy<R> implements SmsClient<R> {

    private final SmsClient<R> smsClient;

    @Override
    public SmsResult<R> send(SmsBody msgBody, Collection<String> targets) {
        log.debug("ready send sms, body: {}, target: {}", JsonUtils.toJsonString(msgBody), targets);

        SmsResult<R> resultBody = smsClient.send(msgBody, targets);

        if (resultBody.getSuccess()) {
            //
        } else {
            log.warn("send sms fail, body: {}, target: {}, resultBody: {}",
                    JsonUtils.toJsonString(msgBody),
                    targets,
                    JsonUtils.toJsonString(resultBody)
            );
        }
        return resultBody;
    }

    @Override
    public SmsResult<R> sendAsync(SmsBody msgBody, Collection<String> targets) {
        return send(msgBody, targets);
    }

    public SmsClientLogProxy(SmsClient<R> smsClient) {
        this.smsClient = smsClient;
    }
}
