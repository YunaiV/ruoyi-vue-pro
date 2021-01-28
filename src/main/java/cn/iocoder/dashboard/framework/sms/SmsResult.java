package cn.iocoder.dashboard.framework.sms;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息内容实体类
 */
@Data
public class SmsResult<T> implements Serializable {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 提示
     */
    private String message;

    /**
     * 返回值
     */
    private T result;
}
