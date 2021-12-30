package cn.iocoder.yudao.framework.mq.core.message;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis 消息抽象基类
 *
 * @author 芋道源码
 */
@Data
public abstract class AbstractRedisMessage {

    /**
     * 头
     */
    private Map<String, String> headers = new HashMap<>();

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

}
