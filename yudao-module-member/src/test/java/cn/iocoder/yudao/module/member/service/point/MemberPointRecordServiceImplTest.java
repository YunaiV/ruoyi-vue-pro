package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.point.MemberPointRecordMapper;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.module.member.mq.producer.user.MemberUserProducer;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.member.service.user.MemberUserServiceImpl;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.USER_POINT_NOT_ENOUGH;
import static cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum.SIGN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MemberPointRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({MemberPointRecordServiceImpl.class, MemberUserServiceImpl.class})
public class MemberPointRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MemberPointRecordServiceImpl memberPointRecordService;

    @Resource
    private MemberUserMapper memberUserMapper;
    @Resource
    private MemberPointRecordMapper memberPointRecordMapper;
    @Resource
    private PlatformTransactionManager transactionManager;

    @MockitoSpyBean
    private MemberUserService memberUserService;
    @MockitoBean
    private SmsCodeApi smsCodeApi;
    @MockitoBean
    private SocialClientApi socialClientApi;
    @MockitoBean
    private OAuth2TokenCommonApi oauth2TokenApi;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private MemberUserProducer memberUserProducer;

    // ========== createPointRecord ==========

    @Test
    public void testCreatePointRecord_concurrentDecrNotEnough() throws Exception {
        // mock 数据
        MemberUserDO user = createUser(100);

        // 调用：两笔并发各扣 80 积分
        List<Throwable> errors = concurrentCreatePointRecord(user.getId(), -80);
        // 断言：两笔都通过了预检查，但只有一笔扣减成功
        verify(memberUserService, times(2)).updateUserPoint(eq(user.getId()), eq(-80));
        assertEquals(1, errors.size());
        assertServiceException(() -> {
            throw errors.get(0);
        }, USER_POINT_NOT_ENOUGH);
        assertEquals(20, memberUserMapper.selectById(user.getId()).getPoint());
        // 断言：失败的一笔整体回滚，只有成功的一笔流水
        List<MemberPointRecordDO> records = getPointRecordList(user.getId());
        assertEquals(1, records.size());
        assertEquals(-80, records.get(0).getPoint());
        assertEquals(20, records.get(0).getTotalPoint());
    }

    @Test
    public void testCreatePointRecord_concurrentDecr() throws Exception {
        // mock 数据
        MemberUserDO user = createUser(100);

        // 调用：两笔并发各扣 40 积分
        List<Throwable> errors = concurrentCreatePointRecord(user.getId(), -40);
        // 断言：流水按实际扣减先后记录变动后的积分
        assertEquals(0, errors.size());
        assertEquals(20, memberUserMapper.selectById(user.getId()).getPoint());
        List<MemberPointRecordDO> records = getPointRecordList(user.getId());
        assertEquals(List.of(-40, -40), convertList(records, MemberPointRecordDO::getPoint));
        assertEquals(List.of(60, 20), convertList(records, MemberPointRecordDO::getTotalPoint));
    }

    @Test
    public void testCreatePointRecord_concurrentIncr() throws Exception {
        // mock 数据
        MemberUserDO user = createUser(100);

        // 调用：两笔并发各加 40 积分
        List<Throwable> errors = concurrentCreatePointRecord(user.getId(), 40);
        // 断言：流水按实际增加先后记录变动后的积分
        assertEquals(0, errors.size());
        assertEquals(180, memberUserMapper.selectById(user.getId()).getPoint());
        List<MemberPointRecordDO> records = getPointRecordList(user.getId());
        assertEquals(List.of(40, 40), convertList(records, MemberPointRecordDO::getPoint));
        assertEquals(List.of(140, 180), convertList(records, MemberPointRecordDO::getTotalPoint));
    }

    @Test
    public void testCreatePointRecord_notEnough() {
        // mock 数据
        MemberUserDO user = createUser(20);

        // 调用：预检查积分不足，打印日志后直接返回
        memberPointRecordService.createPointRecord(user.getId(), -80, SIGN, randomString());
        // 断言
        verify(memberUserService, never()).updateUserPoint(anyLong(), anyInt());
        assertEquals(20, memberUserMapper.selectById(user.getId()).getPoint());
        assertEquals(0, getPointRecordList(user.getId()).size());
    }

    @Test
    public void testCreatePointRecord_sameSqlSession() {
        // mock 数据
        MemberUserDO user = createUser(100);

        // 调用：同一事务（SqlSession）内，先查询，再变动积分，再查询
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            MemberUserDO cachedUser = memberUserMapper.selectById(user.getId());
            assertSame(cachedUser, memberUserMapper.selectById(user.getId())); // 命中一级缓存
            assertEquals(100, cachedUser.getPoint());

            memberPointRecordService.createPointRecord(user.getId(), -30, SIGN, randomString());

            // 断言：UPDATE 清空了一级缓存，查询到自身写入的积分
            MemberUserDO latestUser = memberUserMapper.selectById(user.getId());
            assertNotSame(cachedUser, latestUser);
            assertEquals(70, latestUser.getPoint());
        });
        // 断言：流水使用 UPDATE 后重新读取的积分
        verify(memberUserService, times(2)).getUser(eq(user.getId()));
        List<MemberPointRecordDO> records = getPointRecordList(user.getId());
        assertEquals(1, records.size());
        assertEquals(70, records.get(0).getTotalPoint());
        assertEquals(70, memberUserMapper.selectById(user.getId()).getPoint());
    }

    @Test
    public void testCreatePointRecord_insertFailRollback() {
        // mock 数据
        MemberUserDO user = createUser(100);
        // mock 方法：记录每次读取到的积分
        List<Integer> readPoints = new CopyOnWriteArrayList<>();
        doAnswer(invocation -> {
            MemberUserDO result = (MemberUserDO) invocation.callRealMethod();
            readPoints.add(result.getPoint());
            return result;
        }).when(memberUserService).getUser(eq(user.getId()));

        // 调用：bizId 为空，违反 biz_id 非空约束，插入流水失败
        assertThrows(DataIntegrityViolationException.class,
                () -> memberPointRecordService.createPointRecord(user.getId(), -30, SIGN, null));
        // 断言：事务内积分已扣减为 70，随插入流水失败一起回滚
        assertEquals(List.of(100, 70), readPoints);
        assertEquals(100, memberUserMapper.selectById(user.getId()).getPoint());
        assertEquals(0, getPointRecordList(user.getId()).size());
    }

    // ========== MemberUserMapper.updatePointDecr ==========

    @Test
    public void testUpdatePointDecr_exact() {
        // mock 数据
        MemberUserDO user = createUser(100);

        // 调用，并断言
        assertEquals(1, memberUserMapper.updatePointDecr(user.getId(), -100));
        assertEquals(0, memberUserMapper.selectById(user.getId()).getPoint());
    }

    @Test
    public void testUpdatePointDecr_notEnough() {
        // mock 数据
        MemberUserDO user = createUser(100);

        // 调用，并断言
        assertEquals(0, memberUserMapper.updatePointDecr(user.getId(), -101));
        assertEquals(100, memberUserMapper.selectById(user.getId()).getPoint());
    }

    @Test
    public void testUpdatePointDecr_minValue() {
        // mock 数据
        MemberUserDO user = createUser(100);

        // 调用，并断言：Integer.MIN_VALUE 取反不溢出，按积分不足处理
        assertEquals(0, memberUserMapper.updatePointDecr(user.getId(), Integer.MIN_VALUE));
        assertEquals(100, memberUserMapper.selectById(user.getId()).getPoint());
    }

    // ========== 私有方法 ==========

    /**
     * 两个线程各自开启事务，都读取到变动前的积分后，再并发变动积分
     *
     * @return 失败线程的异常
     */
    private List<Throwable> concurrentCreatePointRecord(Long userId, Integer point) throws Exception {
        // 首次读取积分后，等待另一个线程也读取完成；UPDATE 后的重新读取不再等待
        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            latch.countDown();
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            return result;
        }).when(memberUserService).getUser(eq(userId));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                futures.add(executor.submit(() -> memberPointRecordService.createPointRecord(
                        userId, point, SIGN, randomString())));
            }
            List<Throwable> errors = new ArrayList<>();
            for (Future<?> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                } catch (ExecutionException ex) {
                    errors.add(ex.getCause());
                }
            }
            return errors;
        } finally {
            executor.shutdownNow();
        }
    }

    private MemberUserDO createUser(Integer point) {
        MemberUserDO user = MemberUserDO.builder().mobile("15601691300").status(CommonStatusEnum.ENABLE.getStatus())
                .registerIp("127.0.0.1").point(point).build();
        memberUserMapper.insert(user);
        return user;
    }

    private List<MemberPointRecordDO> getPointRecordList(Long userId) {
        return memberPointRecordMapper.selectList(new LambdaQueryWrapperX<MemberPointRecordDO>()
                .eq(MemberPointRecordDO::getUserId, userId)
                .orderByAsc(MemberPointRecordDO::getId));
    }

}
