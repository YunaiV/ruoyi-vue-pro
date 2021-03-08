package cn.iocoder.dashboard.framework.sms.core;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 消息内容实体类
 */
@Data
@Accessors(chain = true)
public class SmsResult implements Serializable {

    /**
     * 是否成功(发送短信的请求是否成功)
     */
    private Boolean success;

    /**
     * 第三方唯一标识
     */
    private String apiId;

    /**
     * 状态码
     */
    private String code;

    /**
     * 提示
     */
    private String message;

    /**
     * 用于查询发送结果的参数
     */
    private String sendResultParam;

    public static SmsResult failResult(String message) {
        SmsResult resultBody = new SmsResult();
        resultBody.setSuccess(false);
        resultBody.setMessage(message);
        return resultBody;
    }
}
