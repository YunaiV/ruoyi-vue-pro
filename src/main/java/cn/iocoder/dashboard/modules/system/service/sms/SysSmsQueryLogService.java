package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;

import java.util.List;

/**
 * 短信请求日志服务接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SysSmsQueryLogService {
    /**
     * 发送短信前的日志处理
     *
     * @param smsBody      短信内容
     * @param targetPhones 发送对象手机号集合
     * @param client       短信客户端
     * @return 生成的日志id
     */
    // TODO FROM 芋艿 to ZZF: async 是针对发送的方式，对于日志不一定需要关心。这样，短信日志，实际就发送前插入，发送后更新结果.
    //   这里只用于记录状态，毕竟异步可能推送失败，此时日志可记录该状态。

    // TODO FROM 芋艿 to ZZF：短信日志，群发的情况，应该是每个手机一条哈。虽然是群发，但是可能部分成功，部分失败；对应到短信平台，实际也是多条。
    void beforeSendLog(SmsBody smsBody, List<String> targetPhones, AbstractSmsClient client);

    /**
     * 发送消息后的日志处理
     *
     * @param logId  日志id
     * @param result 消息结果
     */
    void afterSendLog(Long logId, SmsResult result);

}
