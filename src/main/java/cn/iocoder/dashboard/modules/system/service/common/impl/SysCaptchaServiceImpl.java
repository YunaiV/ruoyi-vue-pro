package cn.iocoder.dashboard.modules.system.service.common.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.dashboard.framework.captcha.config.CaptchaProperties;
import cn.iocoder.dashboard.modules.system.controller.common.vo.SysCaptchaImageRespVO;
import cn.iocoder.dashboard.modules.system.convert.common.SysCaptchaConvert;
import cn.iocoder.dashboard.modules.system.dal.redis.common.SysCaptchaRedisDAO;
import cn.iocoder.dashboard.modules.system.service.common.SysCaptchaService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 验证码 Service 实现类
 */
@Service
public class SysCaptchaServiceImpl implements SysCaptchaService {

    @Resource
    private CaptchaProperties captchaProperties;

    @Resource
    private SysCaptchaRedisDAO captchaRedisDAO;

    @Override
    public SysCaptchaImageRespVO getCaptchaImage() {
        // 生成验证码
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(captchaProperties.getWidth(), captchaProperties.getHeight());
        // 缓存到 Redis 中
        String uuid = IdUtil.fastSimpleUUID();
        captchaRedisDAO.set(uuid, captcha.getCode(), captchaProperties.getTimeout());
        // 返回
        return SysCaptchaConvert.INSTANCE.convert(uuid, captcha);
    }

    @Override
    public String getCaptchaCode(String uuid) {
        return captchaRedisDAO.get(uuid);
    }

    @Override
    public void deleteCaptchaCode(String uuid) {
        captchaRedisDAO.delete(uuid);
    }

}
