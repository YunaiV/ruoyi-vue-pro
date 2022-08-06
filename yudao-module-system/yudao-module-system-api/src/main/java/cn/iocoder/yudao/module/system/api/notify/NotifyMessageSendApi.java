package cn.iocoder.yudao.module.system.api.notify;

import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;

import javax.validation.Valid;

/**
 * 站内信发送 API 接口
 *
 * @author xrcoder
 */
public interface NotifyMessageSendApi {

    /**
     * 发送单条站内信给 Admin 用户
     * <p>
     * 在 mobile 为空时，使用 userId 加载对应 Admin 的手机号
     *
     * @param reqDTO 发送请求
     * @return 发送消息ID
     */
    Long sendSingleMessageToAdmin(@Valid NotifySendSingleToUserReqDTO reqDTO);

    /**
     * 发送单条站内信给 Member 用户
     * <p>
     * 在 mobile 为空时，使用 userId 加载对应 Member 的手机号
     *
     * @param reqDTO 发送请求
     * @return 发送消息ID
     */
    Long sendSingleMessageToMember(@Valid NotifySendSingleToUserReqDTO reqDTO);

}
