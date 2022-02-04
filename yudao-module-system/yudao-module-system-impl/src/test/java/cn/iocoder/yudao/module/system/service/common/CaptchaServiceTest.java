package cn.iocoder.yudao.module.system.service.common;

import cn.iocoder.yudao.module.system.controller.admin.common.vo.CaptchaImageRespVO;
import cn.iocoder.yudao.module.system.dal.redis.common.CaptchaRedisDAO;
import cn.iocoder.yudao.module.system.framework.captcha.config.CaptchaProperties;
import cn.iocoder.yudao.module.system.test.BaseRedisUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

@Import({CaptchaServiceImpl.class, CaptchaProperties.class, CaptchaRedisDAO.class})
public class CaptchaServiceTest extends BaseRedisUnitTest {

    @Resource
    private CaptchaServiceImpl captchaService;

    @Resource
    private CaptchaRedisDAO captchaRedisDAO;
    @Resource
    private CaptchaProperties captchaProperties;

    @Test
    public void testGetCaptchaImage() {
        // 调用
        CaptchaImageRespVO respVO = captchaService.getCaptchaImage();
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
