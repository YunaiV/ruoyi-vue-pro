package cn.iocoder.yudao.module.oa.job.meetingroom;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.oa.service.meetingroom.OaMeetingRoomBookingService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link OaMeetingRoomBookingCancelExpiredJob} 的单元测试类
 *
 * @author 芋道源码
 */
public class OaMeetingRoomBookingCancelExpiredJobTest extends BaseMockitoUnitTest {

    @InjectMocks
    private OaMeetingRoomBookingCancelExpiredJob job;

    @Mock
    private OaMeetingRoomBookingService meetingRoomBookingService;

    @Test
    public void testExecute() {
        // mock 方法
        when(meetingRoomBookingService.cancelExpiredMeetingRoomBookings()).thenReturn(2);

        // 调用
        String result = job.execute(null);
        // 断言：只取消过期预定，不发送会议提醒
        assertEquals("取消过期未开始会议 2 条", result);
        verify(meetingRoomBookingService).cancelExpiredMeetingRoomBookings();
        verifyNoMoreInteractions(meetingRoomBookingService);
    }

}
