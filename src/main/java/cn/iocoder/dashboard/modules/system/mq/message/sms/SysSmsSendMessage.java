package cn.iocoder.dashboard.modules.system.mq.message.sms;

import cn.iocoder.dashboard.framework.redis.core.stream.StreamMessage;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 短信发送消息
 *
 * @author 芋道源码
 */
@Data
public class SysSmsSendMessage implements StreamMessage {

    /**
     * 手机号
     */
    @NotNull(message = "手机号不能为空")
    private String mobile;
    /**
     * 短信模板编号
     */
    @NotNull(message = "短信模板编号不能为空")
    private String templateCode;
    /**
     * 短信模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 用户编号，允许空
     */
    private Integer userId;
    /**
     * 用户类型，允许空
     */
    private Integer userType;

    @Override
    public String getStreamKey() {
        return "system.sms.send";
    }

}
