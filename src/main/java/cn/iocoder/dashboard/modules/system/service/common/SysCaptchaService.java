package cn.iocoder.dashboard.modules.system.service.common;

import cn.iocoder.dashboard.modules.system.controller.common.vo.SysCaptchaImageRespVO;

/**
 * 验证码 Service 接口
 */
public interface SysCaptchaService {

    SysCaptchaImageRespVO getCaptchaImage();

}
