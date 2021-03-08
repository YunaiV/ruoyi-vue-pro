package cn.iocoder.dashboard.framework.sms.core;

import cn.iocoder.dashboard.util.json.JsonUtils;
import lombok.Data;

import java.util.Map;

/**
 * 消息内容实体类
 */
@Data
public class SmsBody {

    /**
     * 消息日志id
     */
    private Long smsLogId;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板编码
     */
    private String templateContent;

    /**
     * 参数列表
     */
    private Map<String, String> params;

    public String getParamsStr() {
        return JsonUtils.toJsonString(params);
    }

}
