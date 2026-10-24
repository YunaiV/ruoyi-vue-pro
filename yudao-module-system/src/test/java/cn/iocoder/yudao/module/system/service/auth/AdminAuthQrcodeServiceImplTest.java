package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthQrcodeCreateRespVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthQrcodePollReqVO;
import cn.iocoder.yudao.module.system.dal.redis.auth.AdminAuthQrcodeRedisDAO;
import cn.iocoder.yudao.module.system.enums.auth.AuthQrcodeStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link AdminAuthQrcodeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(AdminAuthQrcodeServiceImpl.class)
public class AdminAuthQrcodeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminAuthQrcodeServiceImpl authQrcodeService;
    @MockitoBean
    private AdminAuthQrcodeRedisDAO authQrcodeRedisDAO;

    @Test
    public void testCreateAuthQrcode() {
        // 调用
        AuthQrcodeCreateRespVO result = authQrcodeService.createAuthQrcode();

        // 校验：二维码不包含领取凭证，Redis 只保存凭证摘要。
        assertTrue(result.getQrcodeId().matches("[a-f0-9]{32}"));
        assertTrue(result.getSecret().matches("[a-f0-9]{32}"));
        assertNotEquals(result.getQrcodeId(), result.getSecret());
        assertFalse(result.getQrContent().contains(result.getSecret()));
        assertEquals("YuDaoLogin:" + result.getQrcodeId(), result.getQrContent());
        assertEquals(180, result.getExpiresIn());
        verify(authQrcodeRedisDAO).create(result.getQrcodeId(), DigestUtil.sha256Hex(result.getSecret()));
    }

    @Test
    public void testConfirmAuthQrcode() {
        // 准备参数
        String qrcodeId = randomString();
        Long userId = randomLongId();

        // 调用
        authQrcodeService.confirmAuthQrcode(qrcodeId, userId);

        // 校验
        verify(authQrcodeRedisDAO).confirm(qrcodeId, userId);
    }

    @Test
    public void testPollAuthQrcode() {
        // 准备参数与 mock
        AuthQrcodePollReqVO reqVO = new AuthQrcodePollReqVO().setSecret(randomString());
        reqVO.setQrcodeId(randomString());
        Pair<AuthQrcodeStatusEnum, Long> expected = Pair.of(AuthQrcodeStatusEnum.CONFIRMED, randomLongId());
        when(authQrcodeRedisDAO.poll(reqVO.getQrcodeId(), DigestUtil.sha256Hex(reqVO.getSecret())))
                .thenReturn(expected);

        // 调用
        Pair<AuthQrcodeStatusEnum, Long> result = authQrcodeService.pollAuthQrcode(reqVO);

        // 校验：领取时同样使用摘要，不把原始凭证传入 Redis。
        assertEquals(expected, result);
        verify(authQrcodeRedisDAO).poll(reqVO.getQrcodeId(), DigestUtil.sha256Hex(reqVO.getSecret()));
    }

}
