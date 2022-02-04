package cn.iocoder.yudao.module.system.mq.message.sms;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 短信发送消息
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSendMessage extends AbstractStreamMessage {

    /**
     * 短信日志编号
     */
    @NotNull(message = "短信日志编号不能为空")
    private Long logId;
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
    private List<KeyValue<String, Object>> templateParams;

    @Override
    public String getStreamKey() {
        return "system.sms.send";
    }

}
