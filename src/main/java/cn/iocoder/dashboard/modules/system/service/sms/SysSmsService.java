package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;

import java.util.List;
import java.util.Map;

/**
 * 短信Service接口
 * 只支持异步，因此没有返回值
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SysSmsService {

    void sendSingleSms(String mobile, Long userId, Integer userType,
                       String templateCode, Map<String, Object> templateParams);

    void sendBatchSms(List<String> mobiles, List<Long> userIds, Integer userType,
                      String templateCode, Map<String, Object> templateParams);

    void doSendSms(SysSmsSendMessage message);

    /**
     * 接收短信的接收结果
     *
     * @param channelCode 渠道编码
     * @param text 结果内容
     * @throws Throwable 处理失败时，抛出异常
     */
    void receiveSmsStatus(String channelCode, String text) throws Throwable;

}
