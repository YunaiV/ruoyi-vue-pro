package cn.iocoder.yudao.coreservice.modules.system.service.sms;

import cn.iocoder.yudao.coreservice.modules.system.mq.message.sms.SysSmsSendMessage;

import java.util.List;
import java.util.Map;

/**
 * 短信 Service Core 接口
 *
 * 接入方，通过调用 send 开头的方法，创建发送短信的任务到 MQ 中
 * 后续，yudao-admin-server 监听 MQ，执行真正的短信发送逻辑
 *
 * @author 芋道源码
 */
public interface SysSmsCoreService {

    /**
     * 发送单条短信给后台用户
     *
     * 在 mobile 为空时，使用 userId 加载对应管理员的手机号
     *
     * @param mobile 手机号
     * @param userId 用户编号
     * @param templateCode 短信模板编号
     * @param templateParams 短信模板参数
     * @return 发送日志编号
     */
    Long sendSingleSmsToAdmin(String mobile, Long userId,
                              String templateCode, Map<String, Object> templateParams);

    /**
     * 发送单条短信给前台用户
     *
     * 在 mobile 为空时，使用 userId 加载对应会员的手机号
     *
     * @param mobile 手机号
     * @param userId 用户编号
     * @param templateCode 短信模板编号
     * @param templateParams 短信模板参数
     * @return 发送日志编号
     */
    Long sendSingleSmsToMember(String mobile, Long userId,
                               String templateCode, Map<String, Object> templateParams);

    Long sendSingleSms(String mobile, Long userId, Integer userType,
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
