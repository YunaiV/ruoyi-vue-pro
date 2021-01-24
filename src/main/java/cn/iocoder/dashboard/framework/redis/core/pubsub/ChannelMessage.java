package cn.iocoder.dashboard.framework.redis.core.pubsub;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Redis Channel Message 接口
 */
public interface ChannelMessage {

    /**
     * 获得 Redis Channel
     *
     * @return Channel
     */
    @JsonIgnore // 必须序列化
    String getChannel();

}
