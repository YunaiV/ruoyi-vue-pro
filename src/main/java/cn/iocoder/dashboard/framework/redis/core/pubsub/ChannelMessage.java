package cn.iocoder.dashboard.framework.redis.core.pubsub;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Redis Channel Message 接口
 */
public interface ChannelMessage {

    /**
     * 获得 Redis Channel
     *
     * @return Channel
     */
    @JSONField(serialize = false) // 必须序列化
    String getChannel();

}
