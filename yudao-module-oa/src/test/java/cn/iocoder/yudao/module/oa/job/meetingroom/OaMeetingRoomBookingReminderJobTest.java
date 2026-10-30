package cn.iocoder.yudao.module.oa.job.meetingroom;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.oa.service.meetingroom.OaMeetingRoomBookingService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link OaMeetingRoomBookingReminderJob} 的单元测试类
 *
 * @author 芋道源码
 */
public class OaMeetingRoomBookingReminderJobTest extends BaseMockitoUnitTest {

    @InjectMocks
    private OaMeetingRoomBookingReminderJob job;

    @Mock
    private OaMeetingRoomBookingService meetingRoomBookingService;

    @Test
    public void testExecute() {
        // mock 方法
        when(meetingRoomBookingService.cancelExpiredMeetingRoomBookings()).thenReturn(1);
        when(meetingRoomBookingService.sendMeetingRoomBookingReminders()).thenReturn(4);

        // 调用
        String result = job.execute(null);
        // 断言：兼容入口仍按原顺序，先取消过期预定，再发送会议提醒
        assertEquals("取消过期未开始会议 1 条，发送会议提醒 4 条", result);
        InOrder inOrder = inOrder(meetingRoomBookingService);
        inOrder.verify(meetingRoomBookingService).cancelExpiredMeetingRoomBookings();
        inOrder.verify(meetingRoomBookingService).sendMeetingRoomBookingReminders();
        verifyNoMoreInteractions(meetingRoomBookingService);
    }

}
