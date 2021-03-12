package cn.iocoder.dashboard.modules.system.redis.stream.sms;

import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import lombok.Data;

/**
 * 部门数据刷新 Message
 */
@Data
public class SmsSendMessage {

    private SmsBody smsBody;

    private String targetPhone;

}
