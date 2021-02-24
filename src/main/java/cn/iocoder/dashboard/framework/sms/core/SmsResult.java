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
    // TODO FROM 芋艿 to zzf：未来要加一个 code，将不同平台的短信失败的情况，做一次统一的收敛。  DONE
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
    // TODO FROM 芋艿 to zzf：是不是统一各个平台的返回结果，这样对调用方来说统一。因为作为统一的短信客户端，最好让上层不太需要知道太具体。黑河诶  DONE
    private List<SmsResultDetail> result;

}
