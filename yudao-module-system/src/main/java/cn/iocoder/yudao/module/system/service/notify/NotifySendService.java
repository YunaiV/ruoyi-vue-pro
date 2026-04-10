package cn.iocoder.yudao.module.system.service.notify;

import java.util.List;
import java.util.Map;

/**
 * 站内信发送 Service 接口
 *
 * @author xrcoder
 */
public interface NotifySendService {

    /**
     * 发送单条站内信给管理后台的用户
     *
     * 在 mobile 为空时，使用 userId 加载对应管理员的手机号
     *
     * @param userId 用户编号
     * @param templateCode 短信模板编号
     * @param templateParams 短信模板参数
     * @return 发送日志编号
     */
    Long sendSingleNotifyToAdmin(Long userId,
                                 String templateCode, Map<String, Object> templateParams);
    /**
     * 发送单条站内信给用户 APP 的用户
     *
     * 在 mobile 为空时，使用 userId 加载对应会员的手机号
     *
     * @param userId 用户编号
     * @param templateCode 站内信模板编号
     * @param templateParams 站内信模板参数
     * @return 发送日志编号
     */
    Long sendSingleNotifyToMember(Long userId,
                                  String templateCode, Map<String, Object> templateParams);

    /**
     * 发送单条站内信给用户
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @param templateCode 站内信模板编号
     * @param templateParams 站内信模板参数
     * @return 发送日志编号
     */
    Long sendSingleNotify( Long userId, Integer userType,
                           String templateCode, Map<String, Object> templateParams);

    default void sendBatchNotify(List<String> mobiles, List<Long> userIds, Integer userType,
                                 String templateCode, Map<String, Object> templateParams) {
        throw new UnsupportedOperationException("暂时不支持该操作，感兴趣可以实现该功能哟！");
    }

}
