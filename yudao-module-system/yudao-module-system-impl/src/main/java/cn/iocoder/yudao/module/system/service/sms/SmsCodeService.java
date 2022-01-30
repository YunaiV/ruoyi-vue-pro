package cn.iocoder.yudao.module.system.service.sms;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.system.enums.sms.SmsSceneEnum;

/**
 * 短信验证码 Service 接口
 *
 * @author 芋道源码
 */
public interface SmsCodeService {

    /**
     * 创建短信验证码，并进行发送
     *
     * @param mobile   手机号
     * @param scene    发送场景 {@link SmsSceneEnum}
     * @param createIp 发送 IP
     */
    void sendSmsCode(@Mobile String mobile, Integer scene, String createIp);

    /**
     * 验证短信验证码，并进行使用
     * 如果正确，则将验证码标记成已使用
     * 如果错误，则抛出 {@link ServiceException} 异常
     *
     * @param mobile 手机号
     * @param scene  发送场景 {@link SmsSceneEnum}
     * @param code   验证码
     * @param usedIp 使用 IP
     */
    void useSmsCode(@Mobile String mobile, Integer scene, String code, String usedIp);

    /**
     * 检查验证码是否有效
     *
     * @param mobile 手机
     * @param code 验证码
     * @param scene 使用场景
     */
    void checkSmsCode(String mobile, String code, Integer scene);

}
