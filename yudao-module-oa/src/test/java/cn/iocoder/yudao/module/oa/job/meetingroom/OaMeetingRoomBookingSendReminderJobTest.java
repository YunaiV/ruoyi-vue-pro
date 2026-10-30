package cn.iocoder.yudao.module.oa.job.meetingroom;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.oa.service.meetingroom.OaMeetingRoomBookingService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link OaMeetingRoomBookingSendReminderJob} 的单元测试类
 *
 * @author 芋道源码
 */
public class OaMeetingRoomBookingSendReminderJobTest extends BaseMockitoUnitTest {

    @InjectMocks
    private OaMeetingRoomBookingSendReminderJob job;

    @Mock
    private OaMeetingRoomBookingService meetingRoomBookingService;

    @Test
    public void testExecute() {
        // mock 方法
        when(meetingRoomBookingService.sendMeetingRoomBookingReminders()).thenReturn(3);

        // 调用
        String result = job.execute(null);
        // 断言：只发送会议提醒，不取消过期预定
        assertEquals("发送会议提醒 3 条", result);
        verify(meetingRoomBookingService).sendMeetingRoomBookingReminders();
        verifyNoMoreInteractions(meetingRoomBookingService);
    }

}
