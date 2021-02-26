package cn.iocoder.dashboard.framework.sms.core;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息内容实体类
 */
@Data
public class SmsResult implements Serializable {

    /**
     * 是否成功(发送短信的请求是否成功)
     */
    private Boolean success;

    /**
     * 状态码
     */
    private String code;

    /**
     * 提示
     */
    private String message;

    /**
     * 返回值
     */
    private List<SmsResultDetail> result;


    public static SmsResult failResult(String message) {
        SmsResult resultBody = new SmsResult();
        resultBody.setSuccess(false);
        resultBody.setMessage(message);
        return resultBody;
    }
}
