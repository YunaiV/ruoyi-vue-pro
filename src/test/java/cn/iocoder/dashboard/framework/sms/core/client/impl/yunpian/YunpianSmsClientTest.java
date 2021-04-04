package cn.iocoder.dashboard.framework.sms.core.client.impl.yunpian;

import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.dashboard.util.date.DateUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对 {@link YunpianSmsClient} 的单元测试
 */
public class YunpianSmsClientTest {

    @InjectMocks
    private final YunpianSmsClient smsClient = new YunpianSmsClient(null);

    @Test
    void doInit() {
    }

    @Test
    void doSendSms() {
    }

    @Test
    void testDoParseSmsReceiveStatus() throws Throwable {
        // 准备参数
        String text = "[{\"sid\":9527,\"uid\":1024,\"user_receive_time\":\"2014-03-17 22:55:21\",\"error_msg\":\"\",\"mobile\":\"15205201314\",\"report_status\":\"SUCCESS\"}]";
        // mock 方法

        // 调用

        // 断言
        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.doParseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).getSuccess());
        assertEquals("", statuses.get(0).getErrorCode());
        assertNull(statuses.get(0).getErrorMsg());
        assertEquals("15205201314", statuses.get(0).getMobile());
        assertEquals(DateUtils.buildTime(2014, 3, 17, 22, 55, 21), statuses.get(0).getReceiveTime());
        assertEquals("9527", statuses.get(0).getSerialNo());
        assertEquals(1024L, statuses.get(0).getLogId());
    }

}
