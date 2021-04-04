package cn.iocoder.dashboard.framework.sms.core.client.impl.aliyun;

import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.dashboard.util.date.DateUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AliyunSmsClient} 的单元测试
 */
public class AliyunSmsClientTest {

    @InjectMocks
    private final AliyunSmsClient smsClient = new AliyunSmsClient(null);

    @Test
    void doInit() {
    }

    @Test
    void doSendSms() {
    }

    @Test
    public void testDoTParseSmsReceiveStatus() throws Throwable {
        // 准备参数
        String text = "[\n" +
                "  {\n" +
                "    \"phone_number\" : \"13900000001\",\n" +
                "    \"send_time\" : \"2017-01-01 11:12:13\",\n" +
                "    \"report_time\" : \"2017-02-02 22:23:24\",\n" +
                "    \"success\" : true,\n" +
                "    \"err_code\" : \"DELIVERED\",\n" +
                "    \"err_msg\" : \"用户接收成功\",\n" +
                "    \"sms_size\" : \"1\",\n" +
                "    \"biz_id\" : \"12345\",\n" +
                "    \"out_id\" : \"67890\"\n" +
                "  }\n" +
                "]";
        // mock 方法

        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.doParseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).getSuccess());
        assertEquals("DELIVERED", statuses.get(0).getErrorCode());
        assertEquals("用户接收成功", statuses.get(0).getErrorMsg());
        assertEquals("13900000001", statuses.get(0).getMobile());
        assertEquals(DateUtils.buildTime(2017, 2, 2, 22, 23, 24), statuses.get(0).getReceiveTime());
        assertEquals("12345", statuses.get(0).getSerialNo());
        assertEquals(67890L, statuses.get(0).getLogId());
    }

}
