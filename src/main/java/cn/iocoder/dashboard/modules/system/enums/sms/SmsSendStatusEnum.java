package cn.iocoder.dashboard.modules.system.enums.sms;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信发送状态
 *
 * @author zzf
 * @date 2021/2/1 13:39
 */
@Getter
@AllArgsConstructor
public enum SmsSendStatusEnum {

    //请求发送结果时失败
    QUERY_SEND_FAIL(-3),

    //短信发送失败
    SEND_FAIL(-2),

    //短信请求失败
    QUERY_FAIL(-1),

    //异步转发中
    ASYNC(0),

    //请求成功
    QUERY_SUCCESS(1),

    //短信成功
    SEND_SUCCESS(2),

    //等待回执
    WAITING(3);

    private final int status;

}
