package cn.iocoder.yudao.module.system.dal.redis.auth;

import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseRedisUnitTest;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.enums.auth.AuthQrcodeStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.AUTH_QRCODE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 扫码登录 Redis 原子操作测试
 *
 * @author 芋道源码
 */
@Import(AdminAuthQrcodeRedisDAO.class)
// 测试基类同时导入默认和项目 Redis 配置，使用项目配置覆盖默认 Bean。
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public class AdminAuthQrcodeRedisDAOTest extends BaseRedisUnitTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AdminAuthQrcodeRedisDAO dao;

    private String qrcodeId;
    private String key;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.clear();
        qrcodeId = UUID.randomUUID().toString();
        key = String.format(AUTH_QRCODE, qrcodeId);
    }

    @AfterEach
    public void tearDown() {
        stringRedisTemplate.delete(key);
        TenantContextHolder.clear();
    }

    @Test
    public void testCreateAndWaiting() {
        dao.create(qrcodeId, "hash");
        Map<Object, Object> fields = stringRedisTemplate.opsForHash().entries(key);
        assertEquals(Map.of("status", String.valueOf(AuthQrcodeStatusEnum.WAITING.getStatus()), "secretHash", "hash"), fields);
        assertTrue(stringRedisTemplate.getExpire(key) > 0 && stringRedisTemplate.getExpire(key) <= 180);
        assertEquals(AuthQrcodeStatusEnum.WAITING, dao.poll(qrcodeId, "hash").getKey());
        assertTrue(stringRedisTemplate.hasKey(key));
    }

    @Test
    public void testConfirmDoesNotExtendTtl() {
        dao.create(qrcodeId, "hash");
        stringRedisTemplate.expire(key, Duration.ofSeconds(60));
        dao.confirm(qrcodeId, 1L);
        dao.confirm(qrcodeId, 1L);
        assertEquals("1", stringRedisTemplate.opsForHash().get(key, "userId"));
        assertEquals(String.valueOf(AuthQrcodeStatusEnum.CONFIRMED.getStatus()), stringRedisTemplate.opsForHash().get(key, "status"));
        assertTrue(stringRedisTemplate.getExpire(key) > 0 && stringRedisTemplate.getExpire(key) <= 60);
        assertEquals(3, stringRedisTemplate.opsForHash().size(key));
    }

    @Test
    public void testAnotherUserCannotOverwriteConfirmation() {
        dao.create(qrcodeId, "hash");
        dao.confirm(qrcodeId, 1L);
        assertServiceException(() -> dao.confirm(qrcodeId, 2L), AUTH_QRCODE_ALREADY_CONFIRMED);
        assertEquals(1L, dao.poll(qrcodeId, "hash").getValue());
    }

    @Test
    public void testWrongSecretDoesNotConsume() {
        dao.create(qrcodeId, "hash");
        assertServiceException(() -> dao.poll(qrcodeId, "wrong"), AUTH_QRCODE_INVALID);
        dao.confirm(qrcodeId, 1L);
        assertServiceException(() -> dao.poll(qrcodeId, "wrong"), AUTH_QRCODE_INVALID);
        assertEquals(1L, dao.poll(qrcodeId, "hash").getValue());
        assertFalse(stringRedisTemplate.hasKey(key));
        assertServiceException(() -> dao.poll(qrcodeId, "hash"), AUTH_QRCODE_INVALID);
    }

    @Test
    public void testMissingKeyIsNotRecreated() {
        assertServiceException(() -> dao.confirm(qrcodeId, 1L), AUTH_QRCODE_INVALID);
        assertServiceException(() -> dao.poll(qrcodeId, "hash"), AUTH_QRCODE_INVALID);
        assertFalse(stringRedisTemplate.hasKey(key));
    }

    @Test
    public void testCreateCannotOverwriteExistingKey() {
        dao.create(qrcodeId, "hash");
        dao.confirm(qrcodeId, 1L);
        assertServiceException(() -> dao.create(qrcodeId, "another"), AUTH_QRCODE_INVALID);
        assertEquals(1L, dao.poll(qrcodeId, "hash").getValue());
    }

    @Test
    public void testTenantIsolation() {
        // 准备参数：租户 7 创建二维码。
        TenantContextHolder.setTenantId(7L);
        dao.create(qrcodeId, "hash");
        try {
            // 租户 8 不能确认或领取租户 7 的二维码。
            TenantContextHolder.setTenantId(8L);
            assertServiceException(() -> dao.confirm(qrcodeId, 1L), AUTH_QRCODE_INVALID);
            assertServiceException(() -> dao.poll(qrcodeId, "hash"), AUTH_QRCODE_INVALID);

            // 原租户仍可正常确认和领取，其他租户的请求未影响原记录。
            TenantContextHolder.setTenantId(7L);
            dao.confirm(qrcodeId, 1L);
            assertEquals(1L, dao.poll(qrcodeId, "hash").getValue());
        } finally {
            stringRedisTemplate.delete(key + ":7");
        }
    }

    @Test
    public void testConcurrentClaimOnlyOneWinner() throws Exception {
        dao.create(qrcodeId, "hash");
        dao.confirm(qrcodeId, 1L);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        Pair<AuthQrcodeStatusEnum, Long> result = dao.poll(qrcodeId, "hash");
                        return result.getKey() == AuthQrcodeStatusEnum.CONFIRMED && result.getValue().equals(1L);
                    } catch (ServiceException ex) {
                        assertEquals(AUTH_QRCODE_INVALID.getCode(), ex.getCode());
                        return false;
                    }
                }));
            }
            start.countDown();
            int winners = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(10, TimeUnit.SECONDS)) winners++;
            }
            assertEquals(1, winners);
            assertFalse(stringRedisTemplate.hasKey(key));
        } finally {
            executor.shutdownNow();
        }
    }

}
