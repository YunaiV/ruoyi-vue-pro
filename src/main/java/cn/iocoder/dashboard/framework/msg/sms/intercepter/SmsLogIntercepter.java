package cn.iocoder.dashboard.framework.msg.sms.intercepter;

import cn.iocoder.dashboard.framework.msg.sms.SmsBody;
import cn.iocoder.dashboard.framework.msg.sms.SmsResult;
import cn.iocoder.dashboard.modules.msg.dal.mysql.dao.sms.SmsLogMapper;
import cn.iocoder.dashboard.util.json.JsonUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 短信日志拦截器
 *
 * @author zzf
 * @date 2021/1/22 15:46
 */
@Slf4j
public class SmsLogIntercepter implements SmsIntercepter {


    @Override
    public void beforeSender(SmsBody msgBody, Collection<String> targets) {
        log.debug("ready send sms, body: {}, target: {}", JsonUtils.toJsonString(msgBody), targets);

    }

    @Override
    public <T> void afterSender(SmsBody msgBody, Collection<String> targets, SmsResult<T> resultBody) {
        if (resultBody.getSuccess()) {
            //
        } else {
            log.warn("send sms fail, body: {}, target: {}, resultBody: {}",
                    JsonUtils.toJsonString(msgBody),
                    targets,
                    JsonUtils.toJsonString(resultBody)
            );
        }

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
