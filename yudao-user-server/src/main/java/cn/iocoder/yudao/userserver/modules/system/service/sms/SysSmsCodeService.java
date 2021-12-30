package cn.iocoder.yudao.userserver.modules.system.service.sms;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.userserver.modules.system.controller.auth.vo.SysAuthSendSmsReqVO;
import cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsSceneEnum;

/**
 * 短信验证码 Service 接口
 *
 * @author 芋道源码
 */
public interface SysSmsCodeService {

    /**
     * 创建短信验证码，并进行发送
     *
     * @param mobile 手机号
     * @param scene 发送场景 {@link SysSmsSceneEnum}
     * @param createIp 发送 IP
     */
    void sendSmsCode(@Mobile String mobile, Integer scene, String createIp);

    /**
     * 发送短信验证码，并检测手机号是否已被注册
     * @param reqVO 请求实体
     */
    void sendSmsNewCode(SysAuthSendSmsReqVO reqVO);

    /**
     * 验证短信验证码，并进行使用
     * 如果正确，则将验证码标记成已使用
     * 如果错误，则抛出 {@link ServiceException} 异常
     *
     * @param mobile 手机号
     * @param scene 发送场景
     * @param code 验证码
     * @param usedIp 使用 IP
     */
    void useSmsCode(@Mobile String mobile, Integer scene, String code, String usedIp);

    /**
     * 根据用户id发送验证码
     * @param userId 用户id
     */
    void sendSmsCodeLogin(Long userId);
}
