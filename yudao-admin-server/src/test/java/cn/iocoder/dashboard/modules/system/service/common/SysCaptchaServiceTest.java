package cn.iocoder.dashboard.modules.system.service.common;

import cn.iocoder.dashboard.BaseRedisUnitTest;
import cn.iocoder.dashboard.framework.captcha.config.CaptchaProperties;
import cn.iocoder.dashboard.modules.system.controller.common.vo.SysCaptchaImageRespVO;
import cn.iocoder.dashboard.modules.system.dal.redis.common.SysCaptchaRedisDAO;
import cn.iocoder.dashboard.modules.system.service.common.impl.SysCaptchaServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.util.test.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

@Import({SysCaptchaServiceImpl.class, CaptchaProperties.class, SysCaptchaRedisDAO.class})
public class SysCaptchaServiceTest extends BaseRedisUnitTest {

    @Resource
    private SysCaptchaServiceImpl captchaService;

    @Resource
    private SysCaptchaRedisDAO captchaRedisDAO;
    @Resource
    private CaptchaProperties captchaProperties;

    @Test
    public void testGetCaptchaImage() {
        // 调用
        SysCaptchaImageRespVO respVO = captchaService.getCaptchaImage();
        // 断言
        assertNotNull(respVO.getUuid());
        assertNotNull(respVO.getImg());
        String captchaCode = captchaRedisDAO.get(respVO.getUuid());
        assertNotNull(captchaCode);
    }

    @Test
    public void testGetCaptchaCode() {
        // 准备参数
        String uuid = randomString();
        String code = randomString();
        // mock 数据
        captchaRedisDAO.set(uuid, code, captchaProperties.getTimeout());

        // 调用
        String resultCode = captchaService.getCaptchaCode(uuid);
        // 断言
        assertEquals(code, resultCode);
    }

    @Test
    public void testDeleteCaptchaCode() {
        // 准备参数
        String uuid = randomString();
        String code = randomString();
        // mock 数据
        captchaRedisDAO.set(uuid, code, captchaProperties.getTimeout());

        // 调用
        captchaService.deleteCaptchaCode(uuid);
        // 断言
        assertNull(captchaRedisDAO.get(uuid));
    }

}
