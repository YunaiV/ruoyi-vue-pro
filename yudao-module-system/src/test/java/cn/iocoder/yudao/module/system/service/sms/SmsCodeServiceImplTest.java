package cn.iocoder.yudao.module.system.service.sms;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsCodeDO;
import cn.iocoder.yudao.module.system.dal.mysql.sms.SmsCodeMapper;
import cn.iocoder.yudao.module.system.dal.redis.sms.SmsCodeRedisDAO;
import cn.iocoder.yudao.module.system.enums.sms.SmsSceneEnum;
import cn.iocoder.yudao.module.system.framework.sms.config.SmsCodeProperties;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.SMS_CODE_SEND_IP_COUNT;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.SMS_CODE_SEND_IP_HOUR_COUNT;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.SMS_CODE_VALIDATE_COUNT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@Import({SmsCodeServiceImpl.class, SmsCodeRedisDAO.class})
public class SmsCodeServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SmsCodeServiceImpl smsCodeService;

    @Resource
    private SmsCodeMapper smsCodeMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @MockitoSpyBean
    private SmsCodeRedisDAO smsCodeRedisDAO;

    @MockitoBean
    private SmsCodeProperties smsCodeProperties;
    @MockitoBean
    private SmsSendService smsSendService;

    @BeforeEach
    public void setUp() {
        when(smsCodeProperties.getExpireTimes()).thenReturn(Duration.ofMinutes(5));
        when(smsCodeProperties.getSendFrequency()).thenReturn(Duration.ofMinutes(1));
        when(smsCodeProperties.getSendMaximumQuantityPerDay()).thenReturn(10);
        when(smsCodeProperties.getBeginCode()).thenReturn(9999);
        when(smsCodeProperties.getEndCode()).thenReturn(9999);
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerDay()).thenReturn(20);
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerHour()).thenReturn(10);
        when(smsCodeProperties.getValidateMaximumQuantity()).thenReturn(5);
    }

    @BeforeEach
    @AfterEach
    public void cleanSmsCodeKeys() {
        Set<String> keys = stringRedisTemplate.keys("sms_code_*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    public void sendSmsCode_success() {
        // 准备参数
        SmsCodeSendReqDTO reqDTO = randomPojo(SmsCodeSendReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
        });

        // 调用
        smsCodeService.sendSmsCode(reqDTO);
        // 断言 code 验证码
        SmsCodeDO smsCodeDO = smsCodeMapper.selectOne(null);
        assertPojoEquals(reqDTO, smsCodeDO);
        assertEquals("9999", smsCodeDO.getCode());
        assertEquals(1, smsCodeDO.getTodayIndex());
        assertFalse(smsCodeDO.getUsed());
        // 断言调用
        verify(smsSendService).sendSingleSms(eq(reqDTO.getMobile()), isNull(), isNull(),
                eq("user-sms-login"), eq(MapUtil.of("code", "9999")));
    }

    @Test
    public void sendSmsCode_register() {
        // 准备参数
        SmsCodeSendReqDTO reqDTO = randomPojo(SmsCodeSendReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_REGISTER.getScene());
        });

        // 调用
        smsCodeService.sendSmsCode(reqDTO);
        // 断言 code 验证码
        SmsCodeDO smsCodeDO = smsCodeMapper.selectOne(null);
        assertPojoEquals(reqDTO, smsCodeDO);
        assertEquals("9999", smsCodeDO.getCode());
        assertEquals(1, smsCodeDO.getTodayIndex());
        assertFalse(smsCodeDO.getUsed());
        // 断言调用
        verify(smsSendService).sendSingleSms(eq(reqDTO.getMobile()), isNull(), isNull(),
                eq("user-sms-register"), eq(MapUtil.of("code", "9999")));
    }

    @Test
    public void useSmsCode_registerSceneAndSingleUse() {
        // 准备参数
        SmsCodeUseReqDTO reqDTO = randomPojo(SmsCodeUseReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
            o.setCode("9999");
        });
        // mock 数据
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setMobile("15601691300")
                .setScene(SmsSceneEnum.MEMBER_REGISTER.getScene()).setCode("9999").setUsed(false)));

        // 调用，并断言场景不匹配
        assertServiceException(() -> smsCodeService.useSmsCode(reqDTO),
                SMS_CODE_NOT_FOUND);

        // 调用
        reqDTO.setScene(SmsSceneEnum.MEMBER_REGISTER.getScene());
        smsCodeService.useSmsCode(reqDTO);
        // 断言
        SmsCodeDO smsCodeDO = smsCodeMapper.selectOne(null);
        assertTrue(smsCodeDO.getUsed());
        assertNotNull(smsCodeDO.getUsedTime());
        assertEquals(reqDTO.getUsedIp(), smsCodeDO.getUsedIp());

        // 再次调用，并断言已被使用
        assertServiceException(() -> smsCodeService.useSmsCode(reqDTO),
                SMS_CODE_USED);
    }

    @Test
    public void sendSmsCode_tooFast() {
        // mock 数据：同一场景刚发送过
        SmsCodeDO smsCodeDO = randomPojo(SmsCodeDO.class, o -> o.setMobile("15601691300")
                .setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setTodayIndex(1).setUsed(false)
                .setCreateTime(LocalDateTime.now()));
        smsCodeMapper.insert(smsCodeDO);
        // 准备参数
        SmsCodeSendReqDTO reqDTO = randomPojo(SmsCodeSendReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
        });

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.sendSmsCode(reqDTO),
                SMS_CODE_SEND_TOO_FAST);
    }

    @Test
    public void sendSmsCode_exceedDay() {
        // mock 数据
        SmsCodeDO smsCodeDO = randomPojo(SmsCodeDO.class,
                o -> o.setMobile("15601691300").setTodayIndex(10).setUsed(false).setCreateTime(LocalDateTime.now()));
        smsCodeMapper.insert(smsCodeDO);
        // 准备参数
        SmsCodeSendReqDTO reqDTO = randomPojo(SmsCodeSendReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
        });
        when(smsCodeProperties.getSendFrequency()).thenReturn(Duration.ofMillis(0));

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.sendSmsCode(reqDTO),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
    }

    @Test
    public void sendSmsCode_sendFrequencyByScene() {
        // 调用：登录、注册场景连续发送，互不限制发送间隔
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691300", SmsSceneEnum.MEMBER_LOGIN, "10.0.0.1"));
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691300", SmsSceneEnum.MEMBER_REGISTER, "10.0.0.1"));
        // 调用，并断言异常：中间插入注册场景后，登录场景仍按其最后一条限制发送间隔
        assertServiceException(() -> smsCodeService.sendSmsCode(
                buildSendReqDTO("15601691300", SmsSceneEnum.MEMBER_LOGIN, "10.0.0.1")), SMS_CODE_SEND_TOO_FAST);
        // 断言：当天序号按手机号跨场景累加，被拒绝的请求不落库、不发送
        assertEquals(2L, smsCodeMapper.selectCount());
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile("15601691300", null, null);
        assertEquals(SmsSceneEnum.MEMBER_REGISTER.getScene(), lastSmsCode.getScene());
        assertEquals(2, lastSmsCode.getTodayIndex());
        verify(smsSendService, times(2)).sendSingleSms(any(), isNull(), isNull(), any(), any());
    }

    @Test
    public void sendSmsCode_exceedDayAcrossScene() {
        // mock 数据：注册场景刚发送过，且手机号当天已达上限
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setId(null).setMobile("15601691300")
                .setScene(SmsSceneEnum.MEMBER_REGISTER.getScene()).setTodayIndex(10).setUsed(false)
                .setCreateTime(LocalDateTime.now())));

        // 调用，并断言异常：换成登录场景不受发送间隔限制，但仍受手机号当天上限限制
        assertServiceException(() -> smsCodeService.sendSmsCode(
                        buildSendReqDTO("15601691300", SmsSceneEnum.MEMBER_LOGIN, "10.0.0.1")),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
        // 断言：不落库、不发送，也不占用 IP 额度
        assertEquals(1L, smsCodeMapper.selectCount());
        verify(smsSendService, never()).sendSingleSms(any(), any(), any(), any(), any());
        assertFalse(stringRedisTemplate.hasKey(buildSendIpDayKey("10.0.0.1")));
        assertFalse(stringRedisTemplate.hasKey(buildSendIpHourKey("10.0.0.1")));
    }

    @Test
    public void testUseSmsCode_success() {
        // 准备参数
        SmsCodeUseReqDTO reqDTO = randomPojo(SmsCodeUseReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> {
            o.setMobile(reqDTO.getMobile()).setScene(reqDTO.getScene())
                    .setCode(reqDTO.getCode()).setUsed(false);
        }));

        // 调用
        smsCodeService.useSmsCode(reqDTO);
        // 断言
        SmsCodeDO smsCodeDO = smsCodeMapper.selectOne(null);
        assertTrue(smsCodeDO.getUsed());
        assertNotNull(smsCodeDO.getUsedTime());
        assertEquals(reqDTO.getUsedIp(), smsCodeDO.getUsedIp());
    }

    @Test
    public void testUseSmsCode_concurrentUseOnlyOneSucceeds() throws Exception {
        // 准备参数：同一验证码的两次使用
        SmsCodeUseReqDTO reqDTO = randomPojo(SmsCodeUseReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
            o.setCode("9999");
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setId(null).setMobile(reqDTO.getMobile())
                .setScene(reqDTO.getScene()).setCode("9999").setUsed(false).setCreateTime(LocalDateTime.now())));
        // 第一次使用校验通过（已读到未使用）后停住，等第二次使用完成后再认领
        CountDownLatch firstValidated = new CountDownLatch(1);
        CountDownLatch secondUsed = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (firstValidated.getCount() > 0) {
                firstValidated.countDown();
                assertTrue(secondUsed.await(10, TimeUnit.SECONDS));
            }
            return invocation.callRealMethod();
        }).when(smsCodeRedisDAO).releaseValidateCount(anyLong());

        // 调用：第一次在另一线程使用，第二次在其校验通过、认领之前完成使用
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> first = executor.submit(() -> smsCodeService.useSmsCode(reqDTO));
            assertTrue(firstValidated.await(10, TimeUnit.SECONDS));
            smsCodeService.useSmsCode(reqDTO);
            secondUsed.countDown();

            // 断言：第一次虽已校验通过，按未使用条件认领失败，按已使用拒绝，不会重复消费
            ExecutionException ex = assertThrows(ExecutionException.class, () -> first.get(30, TimeUnit.SECONDS));
            ServiceException cause = assertInstanceOf(ServiceException.class, ex.getCause());
            assertEquals(SMS_CODE_USED.getCode(), cause.getCode());
        } finally {
            executor.shutdownNow();
        }
        assertTrue(smsCodeMapper.selectOne(null).getUsed());
    }

    @Test
    public void validateSmsCode_success() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setMobile(reqDTO.getMobile())
                .setScene(reqDTO.getScene()).setCode(reqDTO.getCode()).setUsed(false)));

        // 调用
        smsCodeService.validateSmsCode(reqDTO);
    }

    @Test
    public void validateSmsCode_notFound() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.validateSmsCode(reqDTO),
                SMS_CODE_NOT_FOUND);
    }

    @Test
    public void validateSmsCode_expired() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setMobile(reqDTO.getMobile())
                .setScene(reqDTO.getScene()).setCode(reqDTO.getCode()).setUsed(false)
                .setCreateTime(LocalDateTime.now().minusMinutes(6))));

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.validateSmsCode(reqDTO),
                SMS_CODE_EXPIRED);
    }

    @Test
    public void validateSmsCode_used() {
        // 准备参数
        SmsCodeValidateReqDTO reqDTO = randomPojo(SmsCodeValidateReqDTO.class, o -> {
            o.setMobile("15601691300");
            o.setScene(randomEle(SmsSceneEnum.values()).getScene());
        });
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setMobile(reqDTO.getMobile())
                .setScene(reqDTO.getScene()).setCode(reqDTO.getCode()).setUsed(true)
                .setCreateTime(LocalDateTime.now())));

        // 调用，并断言异常
        assertServiceException(() -> smsCodeService.validateSmsCode(reqDTO),
                SMS_CODE_USED);
    }

    @Test
    public void sendSmsCode_exceedIpPerDay() {
        // mock 方法：每个 IP 每日最多发送 2 条
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerDay()).thenReturn(2);

        // 调用：同一 IP 向不同手机号发送
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691301", "10.0.0.1"));
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691302", "10.0.0.1"));
        // 调用，并断言异常：第三个手机号被拒绝，且不落库、不发送
        assertServiceException(() -> smsCodeService.sendSmsCode(buildSendReqDTO("15601691303", "10.0.0.1")),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_IP_PER_DAY);
        assertEquals(2L, smsCodeMapper.selectCount());
        verify(smsSendService, times(2)).sendSingleSms(any(), isNull(), isNull(), any(), any());
        // 断言：计数按自然日设置有效期；当日超限时不占用小时额度
        String redisKey = buildSendIpDayKey("10.0.0.1");
        assertEquals("2", stringRedisTemplate.opsForValue().get(redisKey));
        assertEquals("2", stringRedisTemplate.opsForValue().get(buildSendIpHourKey("10.0.0.1")));
        Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= Duration.ofDays(1).getSeconds());

        // 调用：其它 IP 不受影响
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691303", "10.0.0.2"));
        assertEquals(3L, smsCodeMapper.selectCount());
    }

    @Test
    public void sendSmsCode_exceedIpPerHour() {
        // mock 方法：每个 IP 每日最多发送 3 条、每小时最多发送 2 条
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerDay()).thenReturn(3);
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerHour()).thenReturn(2);

        // 调用：同一 IP 向不同手机号发送
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691301", "10.0.0.1"));
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691302", "10.0.0.1"));
        // 调用，并断言异常：换手机号仍被小时数量拒绝，且不落库、不发送
        assertServiceException(() -> smsCodeService.sendSmsCode(buildSendReqDTO("15601691303", "10.0.0.1")),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_IP_PER_HOUR);
        assertEquals(2L, smsCodeMapper.selectCount());
        verify(smsSendService, times(2)).sendSingleSms(any(), isNull(), isNull(), any(), any());
        // 断言：小时超限时归还已占用的当日额度；小时计数按自然小时设置有效期
        assertEquals("2", stringRedisTemplate.opsForValue().get(buildSendIpDayKey("10.0.0.1")));
        String hourKey = buildSendIpHourKey("10.0.0.1");
        assertEquals("2", stringRedisTemplate.opsForValue().get(hourKey));
        Long ttl = stringRedisTemplate.getExpire(hourKey, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= Duration.ofHours(1).getSeconds());

        // 调用：其它 IP 的小时数量独立
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691303", "10.0.0.2"));
        assertEquals(3L, smsCodeMapper.selectCount());

        // 调用：进入下一个小时（清理小时计数模拟）后，被归还的当日额度仍可使用，用完后被当日数量拒绝
        deleteSendIpHourKeys();
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691304", "10.0.0.1"));
        assertEquals("3", stringRedisTemplate.opsForValue().get(buildSendIpDayKey("10.0.0.1")));
        assertServiceException(() -> smsCodeService.sendSmsCode(buildSendReqDTO("15601691305", "10.0.0.1")),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_IP_PER_DAY);
        assertEquals(4L, smsCodeMapper.selectCount());
    }

    @Test
    public void sendSmsCode_hourNotBypassDay() {
        // mock 方法：每个 IP 每日最多发送 2 条、每小时最多发送 10 条
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerDay()).thenReturn(2);
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerHour()).thenReturn(10);
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691301", "10.0.0.1"));
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691302", "10.0.0.1"));

        // 调用，并断言异常：进入下一个小时（清理小时计数模拟），小时额度重新计算，但仍受当日数量限制
        deleteSendIpHourKeys();
        assertServiceException(() -> smsCodeService.sendSmsCode(buildSendReqDTO("15601691303", "10.0.0.1")),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_IP_PER_DAY);
        assertEquals(2L, smsCodeMapper.selectCount());
        // 断言：当日超限时不占用小时额度
        assertFalse(stringRedisTemplate.hasKey(buildSendIpHourKey("10.0.0.1")));
    }

    @Test
    public void sendSmsCode_tooFastNotConsumeIpCount() {
        // mock 方法：每个 IP 每日、每小时最多发送 1 条
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerDay()).thenReturn(1);
        when(smsCodeProperties.getSendMaximumQuantityPerIpPerHour()).thenReturn(1);
        // mock 数据：手机号同一场景刚发送过
        smsCodeMapper.insert(randomPojo(SmsCodeDO.class, o -> o.setId(null).setMobile("15601691301")
                .setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setTodayIndex(1).setCreateTime(LocalDateTime.now())));

        // 调用，并断言异常：手机号维度拒绝
        assertServiceException(() -> smsCodeService.sendSmsCode(buildSendReqDTO("15601691301", "10.0.0.1")),
                SMS_CODE_SEND_TOO_FAST);
        assertFalse(stringRedisTemplate.hasKey(buildSendIpDayKey("10.0.0.1")));
        assertFalse(stringRedisTemplate.hasKey(buildSendIpHourKey("10.0.0.1")));
        // 调用：手机号维度被拒绝时不占用 IP 额度，同一 IP 仍可向其它手机号发送
        smsCodeService.sendSmsCode(buildSendReqDTO("15601691302", "10.0.0.1"));
        assertEquals(2L, smsCodeMapper.selectCount());
        assertEquals("1", stringRedisTemplate.opsForValue().get(buildSendIpDayKey("10.0.0.1")));
        assertEquals("1", stringRedisTemplate.opsForValue().get(buildSendIpHourKey("10.0.0.1")));
    }

    @Test
    public void validateSmsCode_exceedValidateMaximumQuantity() {
        // mock 数据
        SmsCodeDO smsCodeDO = insertSmsCode(1L, "9999");

        // 调用，并断言异常：连续错误 5 次
        for (int i = 0; i < 5; i++) {
            assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("1111")),
                    SMS_CODE_NOT_FOUND);
        }
        // 调用，并断言异常：达到上限后，正确的验证码也被拒绝
        assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("9999")),
                SMS_CODE_EXCEED_VALIDATE_MAXIMUM_QUANTITY);
        assertServiceException(() -> smsCodeService.useSmsCode(buildUseReqDTO("9999")),
                SMS_CODE_EXCEED_VALIDATE_MAXIMUM_QUANTITY);
        // 断言：验证码未被使用，计数有效期与验证码有效期一致
        assertFalse(smsCodeMapper.selectById(smsCodeDO.getId()).getUsed());
        String redisKey = String.format(SMS_CODE_VALIDATE_COUNT, smsCodeDO.getId());
        assertEquals("5", stringRedisTemplate.opsForValue().get(redisKey));
        Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= Duration.ofMinutes(5).getSeconds());
    }

    @Test
    public void validateSmsCode_correctNotConsumeCount() {
        // mock 数据
        SmsCodeDO smsCodeDO = insertSmsCode(1L, "9999");

        // 调用：错误 4 次
        for (int i = 0; i < 4; i++) {
            assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("1111")),
                    SMS_CODE_NOT_FOUND);
        }
        // 调用：多次正确校验均通过，且不消耗次数
        for (int i = 0; i < 3; i++) {
            smsCodeService.validateSmsCode(buildValidateReqDTO("9999"));
        }
        assertEquals("4", stringRedisTemplate.opsForValue().get(String.format(SMS_CODE_VALIDATE_COUNT, smsCodeDO.getId())));
        // 调用：第 5 次错误后，正确的验证码被拒绝
        assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("1111")),
                SMS_CODE_NOT_FOUND);
        assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("9999")),
                SMS_CODE_EXCEED_VALIDATE_MAXIMUM_QUANTITY);
    }

    @Test
    public void validateSmsCode_resendResetCount() {
        // mock 数据：旧验证码错误次数达到上限
        insertSmsCode(1L, "1111");
        for (int i = 0; i < 5; i++) {
            assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("0000")),
                    SMS_CODE_NOT_FOUND);
        }
        assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("1111")),
                SMS_CODE_EXCEED_VALIDATE_MAXIMUM_QUANTITY);
        // mock 数据：重新发送新的验证码
        SmsCodeDO newSmsCode = insertSmsCode(2L, "2222");

        // 调用：新验证码重新计数，可正常使用
        smsCodeService.validateSmsCode(buildValidateReqDTO("2222"));
        // 调用，并断言异常：旧验证码不再有效
        assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("1111")),
                SMS_CODE_NOT_FOUND);
        assertEquals("1", stringRedisTemplate.opsForValue().get(String.format(SMS_CODE_VALIDATE_COUNT, newSmsCode.getId())));
        smsCodeService.useSmsCode(buildUseReqDTO("2222"));
        assertTrue(smsCodeMapper.selectById(newSmsCode.getId()).getUsed());
    }

    @Test
    public void validateSmsCode_expiredOrUsedNotConsumeCount() {
        // mock 数据：已过期的验证码
        SmsCodeDO expiredSmsCode = insertSmsCode(1L, "9999", false, LocalDateTime.now().minusMinutes(6));
        // 调用，并断言异常：错误的验证码也返回过期，且不计数
        assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("1111")),
                SMS_CODE_EXPIRED);
        assertFalse(stringRedisTemplate.hasKey(String.format(SMS_CODE_VALIDATE_COUNT, expiredSmsCode.getId())));

        // mock 数据：已使用的验证码
        SmsCodeDO usedSmsCode = insertSmsCode(2L, "9999", true, LocalDateTime.now());
        // 调用，并断言异常：错误的验证码也返回已使用，且不计数
        assertServiceException(() -> smsCodeService.validateSmsCode(buildValidateReqDTO("1111")),
                SMS_CODE_USED);
        assertFalse(stringRedisTemplate.hasKey(String.format(SMS_CODE_VALIDATE_COUNT, usedSmsCode.getId())));
    }

    private SmsCodeSendReqDTO buildSendReqDTO(String mobile, String ip) {
        return buildSendReqDTO(mobile, SmsSceneEnum.MEMBER_LOGIN, ip);
    }

    private SmsCodeSendReqDTO buildSendReqDTO(String mobile, SmsSceneEnum scene, String ip) {
        return new SmsCodeSendReqDTO().setMobile(mobile).setScene(scene.getScene()).setCreateIp(ip);
    }

    private String buildSendIpDayKey(String ip) {
        return String.format(SMS_CODE_SEND_IP_COUNT, DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN), ip);
    }

    private String buildSendIpHourKey(String ip) {
        return String.format(SMS_CODE_SEND_IP_HOUR_COUNT, DateUtil.format(LocalDateTime.now(), "yyyyMMddHH"), ip);
    }

    private void deleteSendIpHourKeys() {
        Set<String> keys = stringRedisTemplate.keys("sms_code_send_ip_hour_count:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private SmsCodeValidateReqDTO buildValidateReqDTO(String code) {
        return new SmsCodeValidateReqDTO().setMobile("15601691300")
                .setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setCode(code);
    }

    private SmsCodeUseReqDTO buildUseReqDTO(String code) {
        return new SmsCodeUseReqDTO().setMobile("15601691300")
                .setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setCode(code).setUsedIp("10.0.0.1");
    }

    private SmsCodeDO insertSmsCode(Long id, String code) {
        return insertSmsCode(id, code, false, LocalDateTime.now());
    }

    private SmsCodeDO insertSmsCode(Long id, String code, Boolean used, LocalDateTime createTime) {
        SmsCodeDO smsCodeDO = randomPojo(SmsCodeDO.class, o -> o.setId(id).setMobile("15601691300")
                .setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setCode(code).setUsed(used)
                .setCreateTime(createTime));
        smsCodeMapper.insert(smsCodeDO);
        return smsCodeDO;
    }

}
