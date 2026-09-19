package cn.iocoder.yudao.module.oa.service.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaSchedulePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaScheduleSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleParticipantDO;
import cn.iocoder.yudao.module.oa.dal.mysql.schedule.OaScheduleMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.schedule.OaScheduleParticipantMapper;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaScheduleTypeEnum;
import cn.iocoder.yudao.module.oa.framework.config.OaProperties;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.SCHEDULE_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.SCHEDULE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link OaScheduleServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaScheduleServiceImpl.class, OaProperties.class})
public class OaScheduleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DataSource dataSource;

    @Resource
    private OaScheduleServiceImpl scheduleService;

    @Resource
    private OaScheduleMapper scheduleMapper;
    @Resource
    private OaScheduleParticipantMapper scheduleParticipantMapper;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private NotifyMessageSendApi notifyMessageSendApi;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetSchedulePage_overlapIncludesPreviousMonth() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO crossing = buildScheduleDO(userId).setStartTime(LocalDateTime.of(2026, 8, 30, 9, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 2, 18, 0));
        scheduleMapper.insert(crossing);
        scheduleMapper.insert(buildScheduleDO(userId).setStartTime(LocalDateTime.of(2026, 8, 29, 9, 0))
                .setEndTime(LocalDateTime.of(2026, 8, 31, 23, 59)));
        // 准备参数
        OaSchedulePageReqVO reqVO = new OaSchedulePageReqVO();
        reqVO.setIncludeMine(true);
        reqVO.setIncludeReceived(false);
        reqVO.setOverlapTime(new LocalDateTime[]{LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTime.of(2026, 9, 30, 23, 59)});

        // 调用
        PageResult<OaScheduleDO> result = scheduleService.getSchedulePage(reqVO, userId);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(crossing.getId(), CollUtil.getFirst(result.getList()).getId());
    }

    @Test
    public void testSchedulePages_separateOwnerAndParticipant() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO mine = buildScheduleDO(userId).setTitle("我的日程");
        scheduleMapper.insert(mine);
        OaScheduleDO received = buildScheduleDO(randomLongId()).setTitle("收到日程");
        scheduleMapper.insert(received);
        OaScheduleDO other = buildScheduleDO(randomLongId());
        scheduleMapper.insert(other);
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(received.getId()).setUserId(userId));
        // 准备参数
        OaSchedulePageReqVO reqVO = new OaSchedulePageReqVO();

        // 调用，并断言
        assertEquals(mine.getId(), CollUtil.getFirst(scheduleService.getMySchedulePage(reqVO, userId).getList()).getId());
        assertEquals(1L, scheduleService.getMySchedulePage(reqVO, userId).getTotal());
        assertEquals(received.getId(), CollUtil.getFirst(scheduleService.getReceivedSchedulePage(reqVO, userId).getList()).getId());
        reqVO.setTitle("我的日程");
        assertEquals(0L, scheduleService.getReceivedSchedulePage(reqVO, userId).getTotal());
        assertEquals(0L, scheduleService.getReceivedSchedulePage(new OaSchedulePageReqVO(), randomLongId()).getTotal());
    }

    @Test
    public void testGetSchedulePage_bothScopesDeduplicatedAndPaged() {
        // mock 数据：本人的日程同时存在参与关系，其他人的非共享日程不应返回
        Long userId = randomLongId();
        OaScheduleDO mine = buildScheduleDO(userId).setStartTime(LocalDateTime.of(2026, 9, 9, 9, 0));
        scheduleMapper.insert(mine);
        OaScheduleDO received = buildScheduleDO(randomLongId()).setStartTime(LocalDateTime.of(2026, 9, 10, 9, 0));
        scheduleMapper.insert(received);
        scheduleMapper.insert(buildScheduleDO(randomLongId()));
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(mine.getId()).setUserId(userId));
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(received.getId()).setUserId(userId));

        // 准备参数
        OaSchedulePageReqVO reqVO = new OaSchedulePageReqVO();
        reqVO.setPageSize(1);
        reqVO.setIncludeMine(true);
        reqVO.setIncludeReceived(true);

        // 调用，并断言：按同一排序分页，重叠记录不重复计数
        assertEquals(2L, scheduleService.getSchedulePage(reqVO, userId).getTotal());
        assertEquals(received.getId(), CollUtil.getFirst(scheduleService.getSchedulePage(reqVO, userId).getList()).getId());
        reqVO.setPageNo(2);
        assertEquals(mine.getId(), CollUtil.getFirst(scheduleService.getSchedulePage(reqVO, userId).getList()).getId());

        // 断言：搜索条件同时限制本人和共享日程
        reqVO.setTitle("不存在的日程");
        assertEquals(0L, scheduleService.getSchedulePage(reqVO, userId).getTotal());
    }

    @Test
    public void testGetSchedulePage_singleScope() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO mine = buildScheduleDO(userId);
        scheduleMapper.insert(mine);
        OaScheduleDO received = buildScheduleDO(randomLongId());
        scheduleMapper.insert(received);
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(received.getId()).setUserId(userId));

        // 准备参数
        OaSchedulePageReqVO reqVO = new OaSchedulePageReqVO();
        reqVO.setIncludeMine(true);
        reqVO.setIncludeReceived(false);

        // 调用，并断言：仅本人
        assertEquals(1L, scheduleService.getSchedulePage(reqVO, userId).getTotal());
        assertEquals(mine.getId(), CollUtil.getFirst(scheduleService.getSchedulePage(reqVO, userId).getList()).getId());

        // 调用，并断言：仅共享
        reqVO.setIncludeMine(false);
        reqVO.setIncludeReceived(true);
        assertEquals(1L, scheduleService.getSchedulePage(reqVO, userId).getTotal());
        assertEquals(received.getId(), CollUtil.getFirst(scheduleService.getSchedulePage(reqVO, userId).getList()).getId());
    }

    @Test
    public void testGetSchedulePage_noScope() {
        // mock 数据
        Long userId = randomLongId();
        scheduleMapper.insert(buildScheduleDO(userId));

        // 准备参数
        OaSchedulePageReqVO reqVO = new OaSchedulePageReqVO();
        reqVO.setIncludeMine(false);
        reqVO.setIncludeReceived(false);

        // 调用，并断言
        assertEquals(0L, scheduleService.getSchedulePage(reqVO, userId).getTotal());
        assertTrue(CollUtil.isEmpty(scheduleService.getSchedulePage(reqVO, userId).getList()));
    }

    @Test
    public void testGetSchedulePage_withoutParticipation() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO mine = buildScheduleDO(userId);
        scheduleMapper.insert(mine);
        scheduleMapper.insert(buildScheduleDO(randomLongId()));

        // 调用
        OaSchedulePageReqVO reqVO = new OaSchedulePageReqVO();
        reqVO.setIncludeMine(true);
        reqVO.setIncludeReceived(true);
        // 断言
        assertEquals(1L, scheduleService.getSchedulePage(reqVO, userId).getTotal());
        assertEquals(mine.getId(), CollUtil.getFirst(scheduleService.getSchedulePage(reqVO, userId).getList()).getId());
        reqVO.setIncludeMine(false);
        assertEquals(0L, scheduleService.getSchedulePage(reqVO, userId).getTotal());
    }

    @Test
    public void testCreateSchedule_success() {
        // 准备参数
        Long userId = randomLongId();
        Long participantUserId = randomLongId();
        OaScheduleSaveReqVO reqVO = buildScheduleSaveReqVO()
                .setParticipantUserIds(Collections.singletonList(participantUserId));

        // 调用
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());
        Long scheduleId = scheduleService.createSchedule(reqVO, userId);

        // 断言
        OaScheduleDO schedule = scheduleMapper.selectById(scheduleId);
        assertNotNull(schedule);
        assertEquals(userId.toString(), schedule.getCreator());
        assertEquals(reqVO.getTitle(), schedule.getTitle());
        assertEquals(participantUserId, CollUtil.getFirst(scheduleParticipantMapper.selectListByScheduleIds(
                Collections.singleton(scheduleId))).getUserId());
    }

    @Test
    public void testGetSchedule_sharedParticipant() {
        // mock 数据
        Long ownerUserId = randomLongId();
        Long participantUserId = randomLongId();
        OaScheduleDO schedule = buildScheduleDO(ownerUserId);
        scheduleMapper.insert(schedule);
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(participantUserId));

        // 调用
        OaScheduleDO result = scheduleService.getSchedule(schedule.getId(), participantUserId);

        // 断言
        assertEquals(schedule.getId(), result.getId());
    }

    @Test
    public void testUpdateSchedule_notOwner() {
        // mock 数据
        OaScheduleDO schedule = buildScheduleDO(randomLongId());
        scheduleMapper.insert(schedule);
        // 准备参数
        OaScheduleSaveReqVO reqVO = buildScheduleSaveReqVO().setId(schedule.getId())
                .setParticipantUserIds(Arrays.asList(randomLongId(), randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> scheduleService.updateSchedule(reqVO, randomLongId()), SCHEDULE_ACCESS_DENIED);
    }

    @Test
    public void testUpdateSchedule_repeatedParticipant() {
        // mock 数据
        Long ownerUserId = randomLongId();
        Long participantUserId = randomLongId();
        OaScheduleDO schedule = buildScheduleDO(ownerUserId);
        scheduleMapper.insert(schedule);
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(participantUserId));
        // 准备参数
        OaScheduleSaveReqVO reqVO = buildScheduleSaveReqVO().setId(schedule.getId())
                .setParticipantUserIds(Collections.singletonList(participantUserId));

        // 调用
        scheduleService.updateSchedule(reqVO, ownerUserId);
        scheduleService.updateSchedule(reqVO, ownerUserId);

        // 断言
        assertEquals(1, scheduleParticipantMapper.selectListByScheduleIds(
                Collections.singleton(schedule.getId())).size());
    }

    @Test
    public void testUpdateSchedule_preserveReadStatusAndResetReminder() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO schedule = buildScheduleDO(userId).setReminded(true);
        scheduleMapper.insert(schedule);
        LocalDateTime readTime = LocalDateTime.of(2026, 1, 1, 9, 0);
        OaScheduleParticipantDO participant = new OaScheduleParticipantDO().setScheduleId(schedule.getId()).setUserId(2L)
                .setReadStatus(true).setReadTime(readTime);
        scheduleParticipantMapper.insert(participant);
        // 准备参数
        OaScheduleSaveReqVO reqVO = buildScheduleSaveReqVO().setId(schedule.getId())
                .setParticipantUserIds(Arrays.asList(2L, 3L, 3L));

        // 调用
        scheduleService.updateSchedule(reqVO, userId);

        // 断言：保留已有关系编号，新增去重，重置提醒但保留归属和阅读回执
        assertEquals(2, scheduleParticipantMapper.selectListByScheduleId(schedule.getId()).size());
        assertNotNull(scheduleParticipantMapper.selectById(participant.getId()));
        assertFalse(scheduleMapper.selectById(schedule.getId()).getReminded());
        assertEquals(userId.toString(), scheduleMapper.selectById(schedule.getId()).getCreator());
        assertEquals(readTime, scheduleParticipantMapper.selectById(participant.getId()).getReadTime());
        assertTrue(scheduleParticipantMapper.selectById(participant.getId()).getReadStatus());
        OaScheduleParticipantDO added = CollUtil.findOne(
                scheduleParticipantMapper.selectListByScheduleId(schedule.getId()), item -> item.getUserId().equals(3L));
        assertFalse(added.getReadStatus());
        assertNull(added.getReadTime());
    }

    @Test
    public void testUpdateSchedule_removeAndReAddParticipants() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO schedule = buildScheduleDO(userId);
        scheduleMapper.insert(schedule);
        OaScheduleSaveReqVO reqVO = buildScheduleSaveReqVO().setId(schedule.getId());

        // 调用：反复取消并添加同一参与人，不能违反唯一索引
        for (int i = 0; i < 3; i++) {
            scheduleService.updateSchedule(reqVO.setParticipantUserIds(Collections.singletonList(2L)), userId);
            scheduleService.updateSchedule(reqVO.setParticipantUserIds(Collections.emptyList()), userId);
        }

        // 断言：历史关系仍保留，但业务查询不再返回
        assertEquals(3L, new JdbcTemplate(dataSource).queryForObject("SELECT COUNT(*) FROM oa_schedule_participant WHERE deleted = TRUE", Long.class));
        assertTrue(CollUtil.isEmpty(scheduleParticipantMapper.selectListByScheduleId(schedule.getId())));
    }

    @Test
    public void testSendScheduleReminder_allUsersAndRepeat() {
        // mock 数据：创建人同时也是参与人
        LocalDateTime remindTime = LocalDateTime.now();
        Long ownerId = 1L;
        Long participantId = 2L;
        OaScheduleDO schedule = buildScheduleDO(ownerId).setRemind(true).setReminded(false)
                .setStartTime(remindTime.plusHours(1));
        scheduleMapper.insert(schedule);
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(schedule.getId()).setUserId(ownerId));
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(schedule.getId()).setUserId(participantId));
        // mock 方法
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenReturn(10L);

        // 调用，并断言：创建人和参与人各发送一次，重复执行不再发送
        assertTrue(scheduleService.sendScheduleReminder(schedule.getId(), remindTime));
        assertTrue(scheduleMapper.selectById(schedule.getId()).getReminded());
        assertFalse(scheduleService.sendScheduleReminder(schedule.getId(), remindTime));
        verify(notifyMessageSendApi, times(1)).sendSingleMessageToAdmin(argThat(req -> ownerId.equals(req.getUserId())));
        verify(notifyMessageSendApi, times(1)).sendSingleMessageToAdmin(argThat(req -> participantId.equals(req.getUserId())));
    }

    @Test
    public void testSendScheduleReminders_timeAndStatus() {
        // mock 数据：仅第一条位于严格的 24 小时窗口内，且开启提醒、尚未提醒
        LocalDateTime remindTime = LocalDateTime.of(2026, 9, 9, 10, 0);
        OaScheduleDO due = buildScheduleDO(1L).setRemind(true).setReminded(false).setStartTime(remindTime.plusHours(1));
        scheduleMapper.insert(due);
        scheduleMapper.insert(buildScheduleDO(1L).setRemind(true).setReminded(false).setStartTime(remindTime));
        scheduleMapper.insert(buildScheduleDO(1L).setRemind(true).setReminded(false).setStartTime(remindTime.plusHours(24)));
        scheduleMapper.insert(buildScheduleDO(1L).setRemind(true).setReminded(false).setStartTime(remindTime.minusMinutes(1)));
        scheduleMapper.insert(buildScheduleDO(1L).setRemind(false).setReminded(false).setStartTime(remindTime.plusHours(1)));
        scheduleMapper.insert(buildScheduleDO(1L).setRemind(true).setReminded(true).setStartTime(remindTime.plusHours(1)));

        // mock 方法
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenReturn(10L);
        try (MockedStatic<LocalDateTime> timeMock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            timeMock.when(LocalDateTime::now).thenReturn(remindTime);

            // 调用，并断言：通过批量入口验证查询范围，只有符合条件的日程实际发送
            assertEquals(1, scheduleService.sendScheduleReminders());
            assertTrue(scheduleMapper.selectById(due.getId()).getReminded());
            verify(notifyMessageSendApi, times(1)).sendSingleMessageToAdmin(any());
        }
    }

    @Test
    public void testSendScheduleReminder_failureDoesNotMarkAndCanRetry() {
        // mock 数据
        LocalDateTime remindTime = LocalDateTime.now();
        OaScheduleDO schedule = buildScheduleDO(1L).setRemind(true).setReminded(false)
                .setStartTime(remindTime.plusHours(1));
        scheduleMapper.insert(schedule);
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(schedule.getId()).setUserId(2L));
        // mock 方法：参与人成功、创建人失败
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any()))
                .thenReturn(10L).thenThrow(new IllegalStateException("发送失败")).thenReturn(11L);

        // 调用，并断言：失败不能标记完成，重试整条日程
        assertThrows(IllegalStateException.class,
                () -> scheduleService.sendScheduleReminder(schedule.getId(), remindTime));
        assertFalse(scheduleMapper.selectById(schedule.getId()).getReminded());
        assertTrue(scheduleService.sendScheduleReminder(schedule.getId(), remindTime));
        assertTrue(scheduleMapper.selectById(schedule.getId()).getReminded());
        verify(notifyMessageSendApi, times(4)).sendSingleMessageToAdmin(any());
    }

    @Test
    public void testSendScheduleReminder_disabledDoesNotSend() {
        // mock 数据
        LocalDateTime remindTime = LocalDateTime.now();
        OaScheduleDO schedule = buildScheduleDO(1L).setRemind(false).setReminded(false)
                .setStartTime(remindTime.plusHours(1));
        scheduleMapper.insert(schedule);

        // 调用，并断言
        assertFalse(scheduleService.sendScheduleReminder(schedule.getId(), remindTime));
        verifyNoInteractions(notifyMessageSendApi);
    }

    @Test
    public void testSendScheduleReminder_doesNotCheckMessageId() {
        // mock 数据
        LocalDateTime remindTime = LocalDateTime.now();
        OaScheduleDO schedule = buildScheduleDO(1L).setRemind(true).setReminded(false)
                .setStartTime(remindTime.plusHours(1));
        scheduleMapper.insert(schedule);
        // mock 方法
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenReturn(null);

        // 调用，并断言
        assertTrue(scheduleService.sendScheduleReminder(schedule.getId(), remindTime));
        assertTrue(scheduleMapper.selectById(schedule.getId()).getReminded());
    }

    @Test
    public void testSendScheduleReminder_creatorOnly() {
        // mock 数据
        LocalDateTime remindTime = LocalDateTime.now();
        OaScheduleDO schedule = buildScheduleDO(1L).setRemind(true).setReminded(false)
                .setStartTime(remindTime.plusHours(1));
        scheduleMapper.insert(schedule);
        // mock 方法
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenReturn(10L);

        // 调用，并断言：没有参与人也提醒创建人
        assertTrue(scheduleService.sendScheduleReminder(schedule.getId(), remindTime));
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(argThat(req -> Long.valueOf(1L).equals(req.getUserId())));
    }

    @Test
    public void testSendScheduleReminders_failureDoesNotBlockOthers() {
        // mock 数据：较早日程失败，后续日程仍需发送
        LocalDateTime remindTime = LocalDateTime.now();
        OaScheduleDO failed = buildScheduleDO(1L).setRemind(true).setReminded(false)
                .setStartTime(remindTime.plusHours(1));
        OaScheduleDO success = buildScheduleDO(2L).setRemind(true).setReminded(false)
                .setStartTime(remindTime.plusHours(2));
        scheduleMapper.insert(failed);
        scheduleMapper.insert(success);
        // mock 方法：验证批量内部调用也在事务内
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            NotifySendSingleToUserReqDTO req = invocation.getArgument(0);
            if (Long.valueOf(1L).equals(req.getUserId())) {
                throw new IllegalStateException("发送失败");
            }
            return 10L;
        });

        // 调用
        int count = scheduleService.sendScheduleReminders();

        // 断言：统计和异常隔离均由 Service 负责
        assertEquals(1, count);
        assertFalse(scheduleMapper.selectById(failed.getId()).getReminded());
        assertTrue(scheduleMapper.selectById(success.getId()).getReminded());
        verify(notifyMessageSendApi, times(2)).sendSingleMessageToAdmin(any());
    }

    @Test
    public void testSendScheduleReminders_empty() {

        // 调用，并断言
        assertEquals(0, scheduleService.sendScheduleReminders());
        verifyNoInteractions(notifyMessageSendApi);
    }

    @Test
    public void testUpdateScheduleReadStatus_success() {
        // mock 数据
        OaScheduleDO schedule = buildScheduleDO(randomLongId());
        scheduleMapper.insert(schedule);
        OaScheduleParticipantDO participant = new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(2L).setReadStatus(false);
        scheduleParticipantMapper.insert(participant);
        OaScheduleParticipantDO other = new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(3L).setReadStatus(false);
        scheduleParticipantMapper.insert(other);

        // 调用
        scheduleService.updateScheduleReadStatus(schedule.getId(), 2L);

        // 断言：只更新本人参与关系
        OaScheduleParticipantDO result = scheduleParticipantMapper.selectById(participant.getId());
        assertTrue(result.getReadStatus());
        assertNotNull(result.getReadTime());
        assertFalse(scheduleParticipantMapper.selectById(other.getId()).getReadStatus());
        assertNull(scheduleParticipantMapper.selectById(other.getId()).getReadTime());
    }

    @Test
    public void testUpdateScheduleReadStatus_preserveFirstReadTime() {
        // mock 数据
        OaScheduleDO schedule = buildScheduleDO(randomLongId());
        scheduleMapper.insert(schedule);
        LocalDateTime readTime = LocalDateTime.of(2026, 1, 1, 9, 0);
        OaScheduleParticipantDO participant = new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(2L).setReadStatus(true).setReadTime(readTime);
        scheduleParticipantMapper.insert(participant);

        // 调用
        scheduleService.updateScheduleReadStatus(schedule.getId(), 2L);
        scheduleService.updateScheduleReadStatus(schedule.getId(), 2L);

        // 断言：不覆盖首次时间
        assertEquals(readTime, scheduleParticipantMapper.selectById(participant.getId()).getReadTime());
    }

    @Test
    public void testGetScheduleAndPages_doNotMarkRead() {
        // mock 数据
        OaScheduleDO schedule = buildScheduleDO(randomLongId());
        scheduleMapper.insert(schedule);
        OaScheduleParticipantDO participant = new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(2L).setReadStatus(false);
        scheduleParticipantMapper.insert(participant);

        // 调用
        scheduleService.getSchedule(schedule.getId(), 2L);
        scheduleService.getScheduleParticipantList(schedule.getId());
        scheduleService.getReceivedSchedulePage(new OaSchedulePageReqVO(), 2L);
        scheduleService.getMySchedulePage(new OaSchedulePageReqVO(), 2L);

        // 断言
        OaScheduleParticipantDO result = scheduleParticipantMapper.selectById(participant.getId());
        assertFalse(result.getReadStatus());
        assertNull(result.getReadTime());
    }

    @Test
    public void testUpdateScheduleReadStatus_notParticipant() {
        // mock 数据
        OaScheduleDO schedule = buildScheduleDO(randomLongId());
        scheduleMapper.insert(schedule);

        // 调用，并断言异常
        assertServiceException(() -> scheduleService.updateScheduleReadStatus(schedule.getId(), 2L),
                SCHEDULE_ACCESS_DENIED);
        assertServiceException(() -> scheduleService.getSchedule(schedule.getId(), 2L),
                SCHEDULE_ACCESS_DENIED);
    }

    @Test
    public void testUpdateScheduleReadStatus_ownerWithoutParticipation() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO schedule = buildScheduleDO(userId);
        scheduleMapper.insert(schedule);

        // 调用
        scheduleService.updateScheduleReadStatus(schedule.getId(), userId);

        // 断言：不为创建人自动新增参与关系
        assertTrue(CollUtil.isEmpty(scheduleParticipantMapper.selectListByScheduleId(schedule.getId())));
    }

    @Test
    public void testUpdateScheduleReadStatus_removedParticipant() {
        // mock 数据
        OaScheduleDO schedule = buildScheduleDO(randomLongId());
        scheduleMapper.insert(schedule);
        OaScheduleParticipantDO participant = new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(2L).setReadStatus(false);
        scheduleParticipantMapper.insert(participant);
        scheduleParticipantMapper.deleteById(participant.getId());

        // 调用，并断言异常
        assertServiceException(() -> scheduleService.updateScheduleReadStatus(schedule.getId(), 2L),
                SCHEDULE_ACCESS_DENIED);
    }

    @Test
    public void testUpdateScheduleReadStatus_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> scheduleService.updateScheduleReadStatus(id, 2L), SCHEDULE_NOT_EXISTS);
    }

    @Test
    public void testUpdateScheduleReadStatus_deletedSchedule() {
        // mock 数据
        OaScheduleDO schedule = buildScheduleDO(randomLongId());
        scheduleMapper.insert(schedule);
        OaScheduleParticipantDO participant = new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(2L).setReadStatus(false);
        scheduleParticipantMapper.insert(participant);
        scheduleMapper.deleteById(schedule.getId());

        // 调用，并断言异常
        assertServiceException(() -> scheduleService.updateScheduleReadStatus(schedule.getId(), 2L), SCHEDULE_NOT_EXISTS);
        assertFalse(scheduleParticipantMapper.selectById(participant.getId()).getReadStatus());
    }

    @Test
    public void testUpdateSchedule_reAddedParticipantUnread() {
        // mock 数据
        Long userId = randomLongId();
        OaScheduleDO schedule = buildScheduleDO(userId);
        scheduleMapper.insert(schedule);
        OaScheduleParticipantDO participant = new OaScheduleParticipantDO().setScheduleId(schedule.getId())
                .setUserId(2L).setReadStatus(true).setReadTime(LocalDateTime.of(2026, 1, 1, 9, 0));
        scheduleParticipantMapper.insert(participant);
        // 准备参数
        OaScheduleSaveReqVO reqVO = buildScheduleSaveReqVO().setId(schedule.getId());

        // 调用：先移除，再重新邀请同一个用户
        scheduleService.updateSchedule(reqVO.setParticipantUserIds(Collections.emptyList()), userId);
        scheduleService.updateSchedule(reqVO.setParticipantUserIds(Collections.singletonList(2L)), userId);

        // 断言：历史阅读不沿用到新参与关系
        OaScheduleParticipantDO result = CollUtil.getFirst(scheduleParticipantMapper.selectListByScheduleId(schedule.getId()));
        assertFalse(result.getReadStatus());
        assertNull(result.getReadTime());
    }

    @Test
    public void testGetScheduleParticipantUserIdListMap_success() {
        // mock 数据
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(10L).setUserId(1L).setReadStatus(false));
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(10L).setUserId(2L).setReadStatus(false));
        scheduleParticipantMapper.insert(new OaScheduleParticipantDO().setScheduleId(20L).setUserId(3L).setReadStatus(false));

        // 调用
        Map<Long, List<Long>> participants = scheduleService.getScheduleParticipantUserIdListMap(Collections.singletonList(10L));

        // 断言
        assertEquals(1, participants.size());
        assertEquals(2, participants.get(10L).size());
        assertTrue(participants.get(10L).containsAll(Arrays.asList(1L, 2L)));
    }

    @Test
    public void testGetScheduleParticipantUserIdListMap_empty() {

        // 调用，并断言
        assertTrue(scheduleService.getScheduleParticipantUserIdListMap(Collections.emptyList()).isEmpty());
    }

    // ========== 随机对象 ==========

    /**
     * 构造开始时间晚于当前时间且开启提醒的日程参数。
     *
     * @return 保存参数
     */
    private static OaScheduleSaveReqVO buildScheduleSaveReqVO() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        return new OaScheduleSaveReqVO().setType(OaScheduleTypeEnum.REMINDER.getType())
                .setPriority(OaPriorityEnum.NORMAL.getPriority()).setTitle("产品周会").setDescription("同步项目进度")
                .setStartTime(startTime).setEndTime(startTime.plusHours(1)).setRemind(true);
    }

    /**
     * 构造开始时间晚于当前时间且关闭提醒的日程。
     *
     * @param ownerUserId 创建人编号
     * @return 未入库的测试对象
     */
    private static OaScheduleDO buildScheduleDO(Long ownerUserId) {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        OaScheduleDO schedule = new OaScheduleDO().setType(OaScheduleTypeEnum.REMINDER.getType())
                .setPriority(OaPriorityEnum.NORMAL.getPriority()).setTitle("共享日程")
                .setStartTime(startTime).setEndTime(startTime.plusHours(1)).setRemind(false);
        schedule.setCreator(ownerUserId.toString());
        return schedule;
    }

}
