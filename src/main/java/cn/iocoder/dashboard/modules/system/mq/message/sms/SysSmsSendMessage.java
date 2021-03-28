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
     * 发送日志编号
     */
    @NotNull(message = "发送日志编号不能为空")
    private Long sendLogId;
    /**
     * 手机号
     */
    @NotNull(message = "手机号不能为空")
    private String mobile;
    /**
     * 短信渠道编号
     */
    @NotNull(message = "短信渠道编号不能为空")
    private Long channelId;
    /**
     * 短信 API 的模板编号
     */
    @NotNull(message = "短信 API 的模板编号不能为空")
    private String apiTemplateId;
    /**
     * 短信模板参数
     */
    private Map<String, Object> templateParams;

    @Override
    public String getStreamKey() {
        return "system.sms.send";
    }

}
