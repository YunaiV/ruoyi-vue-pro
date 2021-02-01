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

    //异步转发中
    ASYNC(1),

    //发送中
    SENDING(2),

    //失败
    FAIL(3),

    //成功
    SUCCESS(4);

    private final int status;

}
