package cn.iocoder.yudao.module.oa.service.meetingroom;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.*;
import cn.iocoder.yudao.module.oa.dal.mysql.meetingroom.OaMeetingRoomBookingMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.meetingroom.listener.OaMeetingRoomBookingStatusListener;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaMeetingRoomBookingServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaMeetingRoomBookingServiceImpl.class, OaMeetingRoomBookingStatusListener.class})
public class OaMeetingRoomBookingServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private OaMeetingRoomBookingServiceImpl meetingRoomBookingService;

    @Resource
    private OaMeetingRoomBookingMapper meetingRoomBookingMapper;

    @MockBean
    private OaMeetingRoomService meetingRoomService;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private OaNoRedisDAO noRedisDAO;
    @MockBean
    private BpmProcessInstanceApi processInstanceApi;
    @MockBean
    private NotifyMessageSendApi notifyMessageSendApi;

    @Test
    public void testStartMeetingRoomBooking_roomUnavailable() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2)
                .setStartTime(LocalDateTime.now().minusMinutes(5)).setEndTime(LocalDateTime.now().plusHours(1));
        meetingRoomBookingMapper.insert(booking);
        // mock 方法：审批后会议室进入维修
        when(meetingRoomService.validateMeetingRoomExists(10L)).thenReturn(randomMeetingRoomDO().setStatus(1));

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomBookingService.startMeetingRoomBooking(booking.getId(), 1L),
                MEETING_ROOM_NOT_AVAILABLE);
        assertEquals(0, meetingRoomBookingMapper.selectById(booking.getId()).getUseStatus());
    }

    @Test
    public void testStartMeetingRoomBooking_bookingDisabled() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2)
                .setStartTime(LocalDateTime.now().minusMinutes(5)).setEndTime(LocalDateTime.now().plusHours(1));
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(10L)).thenReturn(randomMeetingRoomDO().setAllowBooking(false));

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomBookingService.startMeetingRoomBooking(booking.getId(), 1L),
                MEETING_ROOM_NOT_AVAILABLE);
    }

    @Test
    public void testCreateMeetingRoomBooking_noDuplicate() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setNo("HY20260916000001");
        meetingRoomBookingMapper.insert(booking);
        // 准备参数
        OaMeetingRoomBookingSaveReqVO reqVO = BeanUtils.toBean(booking, OaMeetingRoomBookingSaveReqVO.class).setId(null);
        // mock 方法
        when(adminUserApi.validateUser(1L)).thenReturn(
                new AdminUserRespDTO().setId(1L).setDeptId(10L));
        when(noRedisDAO.generate(OaNoRedisDAO.MEETING_ROOM_BOOKING_NO_PREFIX)).thenReturn(booking.getNo());

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomBookingService.createMeetingRoomBooking(reqVO, 1L),
                MEETING_ROOM_BOOKING_NO_DUPLICATE);
        assertEquals(1L, meetingRoomBookingMapper.selectCount());
    }

    @Test
    public void testGetMeetingRoomBooking_notExists() {

        // 调用，并断言
        assertNull(meetingRoomBookingService.getMeetingRoomBooking(-1L));
    }

    @Test
    public void testSubmitMeetingRoomBooking_withoutApproval() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId())).thenReturn(randomMeetingRoomDO()
                .setNeedApproval(false));

        // 调用
        String processInstanceId = meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 1L);

        // 断言
        OaMeetingRoomBookingDO actual = meetingRoomBookingMapper.selectById(booking.getId());
        assertNull(processInstanceId);
        assertEquals(2, actual.getStatus());
        assertEquals(0, actual.getUseStatus());
        assertFalse(actual.getNeedApproval());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitMeetingRoomBooking_withApproval() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId())).thenReturn(randomMeetingRoomDO()
                .setNeedApproval(true));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("process-1");

        // 调用
        String processInstanceId = meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 1L);

        // 断言
        assertEquals("process-1", processInstanceId);
        OaMeetingRoomBookingDO actual = meetingRoomBookingMapper.selectById(booking.getId());
        assertEquals(1, actual.getStatus());
        assertEquals("process-1", actual.getProcessInstanceId());
        assertTrue(actual.getNeedApproval());
    }

    @Test
    public void testSubmitMeetingRoomBooking_conflict() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        OaMeetingRoomBookingDO existing = randomMeetingRoomBookingDO().setStartTime(booking.getStartTime())
                .setEndTime(booking.getEndTime()).setStatus(1);
        meetingRoomBookingMapper.insert(existing);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId())).thenReturn(randomMeetingRoomDO());

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 1L),
                MEETING_ROOM_BOOKING_TIME_CONFLICT);
        assertEquals(-1, meetingRoomBookingMapper.selectById(booking.getId()).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitMeetingRoomBooking_adjacent() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        OaMeetingRoomBookingDO existing = randomMeetingRoomBookingDO().setStartTime(booking.getEndTime())
                .setEndTime(booking.getEndTime().plusHours(1)).setStatus(2);
        meetingRoomBookingMapper.insert(existing);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId())).thenReturn(randomMeetingRoomDO());

        // 调用
        meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 1L);

        // 断言
        assertEquals(2, meetingRoomBookingMapper.selectById(booking.getId()).getStatus());
    }

    @Test
    public void testSubmitMeetingRoomBooking_notOwner() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId())).thenReturn(randomMeetingRoomDO());

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 2L),
                MEETING_ROOM_BOOKING_NOT_OWNER);
    }

    @Test
    public void testSubmitMeetingRoomBooking_specifiedMembers() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId()))
                .thenReturn(randomMeetingRoomDO().setBookingScope(1).setBookingUserIds(Collections.singletonList(2L)));

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 1L),
                MEETING_ROOM_NOT_AVAILABLE);
    }

    @Test
    public void testSubmitMeetingRoomBooking_processFailureRollback() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId())).thenReturn(randomMeetingRoomDO()
                .setNeedApproval(true));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenThrow(new IllegalStateException("流程未部署"));

        // 调用，并断言
        assertThrows(IllegalStateException.class, () -> meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 1L));
        assertEquals(-1, meetingRoomBookingMapper.selectById(booking.getId()).getStatus());
    }

    @Test
    public void testCancelMeetingRoomBooking_approved() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2).setNeedApproval(false);
        meetingRoomBookingMapper.insert(booking);

        // 调用
        meetingRoomBookingService.cancelMeetingRoomBooking(booking.getId(), 1L);

        // 断言
        OaMeetingRoomBookingDO actual = meetingRoomBookingMapper.selectById(booking.getId());
        assertEquals(2, actual.getStatus());
        assertEquals(3, actual.getUseStatus());
        assertFalse(meetingRoomBookingService.hasActiveMeetingRoomBooking(booking.getRoomId()));
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateMeetingRoomBookingStatus_reject() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(1).setNeedApproval(true)
                .setProcessInstanceId("process-1");
        meetingRoomBookingMapper.insert(booking);

        // 调用
        meetingRoomBookingService.updateMeetingRoomBookingStatus(booking.getId(), 3);

        // 断言
        assertEquals(3, meetingRoomBookingMapper.selectById(booking.getId()).getUseStatus());
        assertFalse(meetingRoomBookingService.hasActiveMeetingRoomBooking(booking.getRoomId()));
    }

    @Test
    public void testUpdateMeetingRoomBookingStatus_completedNotReset() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2).setUseStatus(2)
                .setNeedApproval(true).setProcessInstanceId("process-1");
        meetingRoomBookingMapper.insert(booking);

        // 调用
        meetingRoomBookingService.updateMeetingRoomBookingStatus(booking.getId(), 2);
        meetingRoomBookingService.updateMeetingRoomBookingStatus(booking.getId(), 3);

        // 断言
        OaMeetingRoomBookingDO actual = meetingRoomBookingMapper.selectById(booking.getId());
        assertEquals(2, actual.getStatus());
        assertEquals(2, actual.getUseStatus());
        assertEquals("process-1", actual.getProcessInstanceId());
    }

    @Test
    public void testSubmitMeetingRoomBooking_synchronousApproval() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId()))
                .thenReturn(randomMeetingRoomDO().setNeedApproval(true));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenAnswer(invocation -> {
            eventPublisher.publishEvent(new BpmProcessInstanceStatusEvent(this).setId("auto-process")
                    .setProcessDefinitionKey(BpmModelConstants.MEETING_ROOM_BOOKING)
                    .setBusinessKey(booking.getId().toString()).setStatus(2));
            return "auto-process";
        });

        // 调用
        meetingRoomBookingService.submitMeetingRoomBooking(booking.getId(), 1L);

        // 断言
        OaMeetingRoomBookingDO actual = meetingRoomBookingMapper.selectById(booking.getId());
        assertEquals(2, actual.getStatus());
        assertEquals(0, actual.getUseStatus());
        assertEquals("auto-process", actual.getProcessInstanceId());
    }

    @Test
    public void testStartAndFinishMeetingRoomBooking() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2)
                .setStartTime(LocalDateTime.now().minusMinutes(5)).setEndTime(LocalDateTime.now().plusHours(1));
        meetingRoomBookingMapper.insert(booking);

        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(booking.getRoomId())).thenReturn(randomMeetingRoomDO());

        // 调用
        meetingRoomBookingService.startMeetingRoomBooking(booking.getId(), 1L);
        // 断言
        assertEquals(1, meetingRoomBookingMapper.selectById(booking.getId()).getUseStatus());

        // 调用
        meetingRoomBookingService.finishMeetingRoomBooking(booking.getId(), 1L);
        // 断言
        assertEquals(2, meetingRoomBookingMapper.selectById(booking.getId()).getUseStatus());
        assertFalse(meetingRoomBookingService.hasActiveMeetingRoomBooking(booking.getRoomId()));
    }

    @Test
    public void testStartMeetingRoomBooking_beforeStart() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2);
        meetingRoomBookingMapper.insert(booking);

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomBookingService.startMeetingRoomBooking(booking.getId(), 1L),
                MEETING_ROOM_BOOKING_STATUS_INVALID);
    }

    @Test
    public void testGetMeetingRoomBookingPage_emptyRelatedFilter() {
        // mock 数据
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO());
        // 准备参数
        OaMeetingRoomBookingPageReqVO reqVO = new OaMeetingRoomBookingPageReqVO().setRoomName("不存在的房间");
        // mock 方法
        when(meetingRoomService.getMeetingRoomListByName(reqVO.getRoomName())).thenReturn(Collections.emptyList());

        // 调用，并断言
        assertEquals(0, meetingRoomBookingService.getMeetingRoomBookingPage(1L, reqVO).getTotal());
    }

    @Test
    public void testGetMeetingRoomBookingPage_ownerScope() {
        // mock 数据
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO());
        OaMeetingRoomBookingDO otherBooking = randomMeetingRoomBookingDO();
        otherBooking.setCreator("2");
        meetingRoomBookingMapper.insert(otherBooking);
        // 准备参数
        OaMeetingRoomBookingPageReqVO reqVO = new OaMeetingRoomBookingPageReqVO();

        // 调用，并断言
        assertEquals(1, meetingRoomBookingService.getMeetingRoomBookingPage(1L, reqVO).getTotal());
    }

    @Test
    public void testGetMeetingRoomBookingPage_endTimeAndCreator() {
        // mock 数据
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 18, 12, 0);
        OaMeetingRoomBookingDO matched = randomMeetingRoomBookingDO().setEndTime(endTime);
        meetingRoomBookingMapper.insert(matched);
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setEndTime(endTime.plusDays(1)));
        OaMeetingRoomBookingDO other = randomMeetingRoomBookingDO().setEndTime(endTime);
        other.setCreator("2");
        meetingRoomBookingMapper.insert(other);
        // 准备参数
        OaMeetingRoomBookingPageReqVO reqVO = new OaMeetingRoomBookingPageReqVO().setCreator("1")
                .setEndTime(new LocalDateTime[]{endTime, endTime.plusHours(1)});

        // 调用，并断言
        assertEquals(1L, meetingRoomBookingService.getMeetingRoomBookingPage(1L, reqVO).getTotal());
        // 创建人筛选不扩大本人预定范围
        reqVO.setCreator("2");
        assertEquals(0L, meetingRoomBookingService.getMeetingRoomBookingPage(1L, reqVO).getTotal());
    }

    @Test
    public void testGetMeetingRoomBookingSchedule_excludesInvalidBookings() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO();
        meetingRoomBookingMapper.insert(booking);
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStartTime(booking.getStartTime())
                .setEndTime(booking.getEndTime()).setStatus(3));
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStartTime(booking.getStartTime())
                .setEndTime(booking.getEndTime()).setStatus(2));
        // 准备参数
        OaMeetingRoomBookingScheduleReqVO reqVO = new OaMeetingRoomBookingScheduleReqVO().setRoomId(10L)
                .setStartTime(booking.getStartTime()).setEndTime(booking.getEndTime());

        // 调用，并断言
        assertEquals(1, meetingRoomBookingService.getMeetingRoomBookingListBySchedule(reqVO).size());
    }

    @Test
    public void testSendMeetingRoomBookingReminder_recipientsAndRepeat() {
        // mock 数据：申请人、主持人、参会人存在重复
        LocalDateTime remindTime = LocalDateTime.of(2026, 9, 16, 10, 0);
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2).setReminderType(2)
                .setStartTime(remindTime.plusMinutes(5)).setModeratorUserId(2L)
                .setAttendeeUserIds(Arrays.asList(1L, 2L, 3L, 3L));
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(10L))
                .thenReturn(randomMeetingRoomDO().setName("第一会议室").setLocation("三楼"));

        // 调用，并断言
        assertTrue(meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime));
        assertFalse(meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime));
        assertTrue(meetingRoomBookingMapper.selectById(booking.getId()).getReminded());
        ArgumentCaptor<NotifySendSingleToUserReqDTO> captor = ArgumentCaptor.forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi, times(3)).sendSingleMessageToAdmin(captor.capture());
        assertEquals(Arrays.asList(1L, 2L, 3L), convertList(captor.getAllValues(), NotifySendSingleToUserReqDTO::getUserId));
        NotifySendSingleToUserReqDTO message = captor.getValue();
        assertEquals("oa_meeting_room_booking_reminder", message.getTemplateCode());
        assertEquals(booking.getTitle(), message.getTemplateParams().get("title"));
        assertEquals("第一会议室", message.getTemplateParams().get("roomName"));
        assertEquals("三楼", message.getTemplateParams().get("location"));
        assertEquals("2026-09-16 10:05", message.getTemplateParams().get("startTime"));
    }

    @ParameterizedTest
    @CsvSource({"2,5", "3,10", "4,15", "5,30"})
    public void testSendMeetingRoomBookingReminder_timeBoundary(int type, int minutes) {
        // mock 数据
        LocalDateTime remindTime = LocalDateTime.of(2026, 9, 16, 10, 0);
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2).setReminderType(type)
                .setStartTime(remindTime.plusMinutes(minutes));
        meetingRoomBookingMapper.insert(booking);
        // mock 方法
        when(meetingRoomService.validateMeetingRoomExists(10L)).thenReturn(randomMeetingRoomDO());

        // 调用，并断言：边界前一秒不提醒，刚好进入提醒窗口时提醒
        assertFalse(meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime.minusSeconds(1)));
        assertTrue(meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime));
        verify(notifyMessageSendApi, times(1)).sendSingleMessageToAdmin(any());
    }

    @ParameterizedTest
    @CsvSource({"-1,0,2,false,5", "1,0,2,false,5", "3,0,2,false,5", "4,0,2,false,5",
            "2,1,2,false,5", "2,2,2,false,5", "2,3,2,false,5", "2,0,1,false,5",
            "2,0,2,true,5", "2,0,2,false,0", "2,0,2,false,-1"})
    public void testSendMeetingRoomBookingReminder_skipInvalid(int status, int useStatus, int reminderType,
                                                              boolean reminded, int minutes) {
        // mock 数据：草稿、审批未通过、已使用/取消、不提醒、已提醒、已开始均不发送
        LocalDateTime remindTime = LocalDateTime.of(2026, 9, 16, 10, 0);
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(status).setUseStatus(useStatus)
                .setReminderType(reminderType).setReminded(reminded).setStartTime(remindTime.plusMinutes(minutes));
        meetingRoomBookingMapper.insert(booking);

        // 调用，并断言
        assertFalse(meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime));
        verifyNoInteractions(notifyMessageSendApi, meetingRoomService);
    }

    @Test
    public void testSendMeetingRoomBookingReminders_filterAndFailureRetry() {
        // mock 数据：包含未到时间、已开始、已取消、已提醒及不开提醒的记录
        LocalDateTime remindTime = LocalDateTime.of(2026, 9, 16, 10, 0);
        OaMeetingRoomBookingDO failed = randomMeetingRoomBookingDO().setStatus(2).setReminderType(2)
                .setStartTime(remindTime.plusMinutes(1)).setTitle("发送失败的会议");
        meetingRoomBookingMapper.insert(failed);
        OaMeetingRoomBookingDO due = randomMeetingRoomBookingDO().setStatus(2).setReminderType(5)
                .setStartTime(remindTime.plusMinutes(30));
        meetingRoomBookingMapper.insert(due);
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStatus(2).setReminderType(2)
                .setStartTime(remindTime.plusMinutes(6)));
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStatus(2).setReminderType(5)
                .setStartTime(remindTime.plusMinutes(31)));
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStatus(2).setReminderType(2).setStartTime(remindTime));
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStatus(2).setUseStatus(3).setReminderType(2)
                .setStartTime(remindTime.plusMinutes(1)));
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStatus(1).setReminderType(2)
                .setStartTime(remindTime.plusMinutes(1)));
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStatus(2).setReminded(true).setReminderType(2)
                .setStartTime(remindTime.plusMinutes(1)));
        meetingRoomBookingMapper.insert(randomMeetingRoomBookingDO().setStatus(2).setReminderType(1)
                .setStartTime(remindTime.plusMinutes(1)));
        // mock 方法：先扫描的会议失败，仍继续处理后一条
        when(meetingRoomService.validateMeetingRoomExists(10L)).thenReturn(randomMeetingRoomDO());
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any()))
                .thenThrow(new IllegalStateException("站内信发送失败")).thenReturn(10L);

        // 调用
        try (MockedStatic<LocalDateTime> timeMock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            timeMock.when(LocalDateTime::now).thenReturn(remindTime);
        // 断言
            assertEquals(1, meetingRoomBookingService.sendMeetingRoomBookingReminders());
            assertFalse(meetingRoomBookingMapper.selectById(failed.getId()).getReminded());
            assertTrue(meetingRoomBookingMapper.selectById(due.getId()).getReminded());
            assertEquals(1, meetingRoomBookingService.sendMeetingRoomBookingReminders());
            assertEquals(0, meetingRoomBookingService.sendMeetingRoomBookingReminders());
        }
        verify(notifyMessageSendApi, times(3)).sendSingleMessageToAdmin(any());
    }

    @Test
    public void testSendMeetingRoomBookingReminder_partialFailureRollback() {
        // mock 数据
        LocalDateTime remindTime = LocalDateTime.of(2026, 9, 16, 10, 0);
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2).setReminderType(2)
                .setStartTime(remindTime.plusMinutes(5)).setAttendeeUserIds(Collections.singletonList(2L));
        meetingRoomBookingMapper.insert(booking);
        // mock 方法：第二个接收人发送失败
        when(meetingRoomService.validateMeetingRoomExists(10L)).thenReturn(randomMeetingRoomDO());
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any()))
                .thenReturn(10L).thenThrow(new IllegalStateException("发送失败")).thenReturn(11L);

        // 调用，并断言：回滚提醒标记，重试完整会议
        assertThrows(IllegalStateException.class,
                () -> meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime));
        assertFalse(meetingRoomBookingMapper.selectById(booking.getId()).getReminded());
        assertTrue(meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime));
        verify(notifyMessageSendApi, times(4)).sendSingleMessageToAdmin(any());
    }

    @Test
    public void testSendMeetingRoomBookingReminders_deletedAndEmpty() {
        // mock 数据
        LocalDateTime remindTime = LocalDateTime.now();
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(2).setReminderType(5)
                .setStartTime(remindTime.plusMinutes(10));
        meetingRoomBookingMapper.insert(booking);
        meetingRoomBookingMapper.deleteById(booking.getId());

        // 调用，并断言：已删除会议在单条和扫描入口都不提醒
        assertFalse(meetingRoomBookingService.sendMeetingRoomBookingReminder(booking.getId(), remindTime));
        assertEquals(0, meetingRoomBookingService.sendMeetingRoomBookingReminders());
        verifyNoInteractions(notifyMessageSendApi, meetingRoomService);
    }

    @Test
    public void testCancelExpiredMeetingRoomBookings() {
        // mock 数据：覆盖到期边界、未来会议、使用中、已完成、已取消、审批中及已删除会议
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 18, 12, 0);
        OaMeetingRoomBookingDO expired = randomMeetingRoomBookingDO().setStatus(2).setEndTime(endTime.minusSeconds(1));
        meetingRoomBookingMapper.insert(expired);
        OaMeetingRoomBookingDO boundary = randomMeetingRoomBookingDO().setStatus(2).setEndTime(endTime);
        meetingRoomBookingMapper.insert(boundary);
        OaMeetingRoomBookingDO future = randomMeetingRoomBookingDO().setStatus(2).setEndTime(endTime.plusSeconds(1));
        meetingRoomBookingMapper.insert(future);
        OaMeetingRoomBookingDO inUse = randomMeetingRoomBookingDO().setStatus(2).setUseStatus(1).setEndTime(endTime);
        meetingRoomBookingMapper.insert(inUse);
        OaMeetingRoomBookingDO completed = randomMeetingRoomBookingDO().setStatus(2).setUseStatus(2).setEndTime(endTime);
        meetingRoomBookingMapper.insert(completed);
        OaMeetingRoomBookingDO cancelled = randomMeetingRoomBookingDO().setStatus(2).setUseStatus(3).setEndTime(endTime);
        meetingRoomBookingMapper.insert(cancelled);
        OaMeetingRoomBookingDO running = randomMeetingRoomBookingDO().setStatus(1).setEndTime(endTime);
        meetingRoomBookingMapper.insert(running);
        OaMeetingRoomBookingDO deleted = randomMeetingRoomBookingDO().setStatus(2).setEndTime(endTime);
        meetingRoomBookingMapper.insert(deleted);
        meetingRoomBookingMapper.deleteById(deleted.getId());

        // 调用
        try (MockedStatic<LocalDateTime> time = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            time.when(LocalDateTime::now).thenReturn(endTime);
            assertEquals(2, meetingRoomBookingService.cancelExpiredMeetingRoomBookings());
            assertEquals(0, meetingRoomBookingService.cancelExpiredMeetingRoomBookings());
        }
        // 断言：只取消已通过且尚未使用的过期预定，保留审批结果
        assertEquals(3, meetingRoomBookingMapper.selectById(expired.getId()).getUseStatus());
        assertEquals(2, meetingRoomBookingMapper.selectById(expired.getId()).getStatus());
        assertEquals(3, meetingRoomBookingMapper.selectById(boundary.getId()).getUseStatus());
        assertEquals(0, meetingRoomBookingMapper.selectById(future.getId()).getUseStatus());
        assertEquals(1, meetingRoomBookingMapper.selectById(inUse.getId()).getUseStatus());
        assertEquals(2, meetingRoomBookingMapper.selectById(completed.getId()).getUseStatus());
        assertEquals(3, meetingRoomBookingMapper.selectById(cancelled.getId()).getUseStatus());
        assertEquals(0, meetingRoomBookingMapper.selectById(running.getId()).getUseStatus());
        assertNull(meetingRoomBookingMapper.selectById(deleted.getId()));
    }

    @Test
    public void testUpdateMeetingRoomBookingStatus_approvedAfterEndTime() {
        // mock 数据
        OaMeetingRoomBookingDO booking = randomMeetingRoomBookingDO().setStatus(1)
                .setStartTime(LocalDateTime.now().minusHours(2)).setEndTime(LocalDateTime.now().minusHours(1));
        meetingRoomBookingMapper.insert(booking);

        // 调用
        meetingRoomBookingService.updateMeetingRoomBookingStatus(booking.getId(), 2);
        // 断言：审批结果保留通过，但不再进入待使用
        OaMeetingRoomBookingDO result = meetingRoomBookingMapper.selectById(booking.getId());
        assertEquals(2, result.getStatus());
        assertEquals(3, result.getUseStatus());
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaMeetingRoomBookingDO randomMeetingRoomBookingDO() {
        return randomPojo(OaMeetingRoomBookingDO.class, booking -> {
            booking.setCreator("1");
            booking.setId(null).setRoomId(10L)
                .setTitle("会议室预定测试").setStatus(-1).setUseStatus(0)
                .setStartTime(LocalDateTime.now().plusDays(1)).setEndTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .setModeratorUserId(1L).setAttendeeUserIds(Collections.emptyList()).setReminderType(1).setReminded(false)
                .setNeedApproval(null).setProcessInstanceId(null).setFileUrls(Collections.emptyList());
        });
    }

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaMeetingRoomDO randomMeetingRoomDO() {
        return randomPojo(OaMeetingRoomDO.class, room -> room.setId(10L).setAllowBooking(true)
                .setStatus(0).setNeedApproval(false).setBookingScope(0).setBookingUserIds(Collections.emptyList()));
    }

}
