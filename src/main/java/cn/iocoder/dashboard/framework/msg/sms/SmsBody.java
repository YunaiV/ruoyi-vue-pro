package cn.iocoder.dashboard.framework.msg.sms;

import cn.iocoder.dashboard.util.json.JsonUtils;
import lombok.Data;

import java.util.Map;

/**
 * 消息内容实体类
 */
@Data
public class SmsBody {

    /**
     * 模板编码
     */
    private String code;

    /**
     * 参数列表
     */
    private Map<String, String> params;

    public String getParamsStr() {
        return JsonUtils.toJsonString(params);
    }

}
