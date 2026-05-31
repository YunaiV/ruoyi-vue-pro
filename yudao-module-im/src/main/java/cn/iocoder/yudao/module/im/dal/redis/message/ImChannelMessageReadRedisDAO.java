package cn.iocoder.yudao.module.im.dal.redis.message;

import cn.hutool.core.convert.Convert;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.CHANNEL_MESSAGE_READ;

/**
 * IM 频道消息已读位置 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class ImChannelMessageReadRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 更新用户在某频道的最大已读消息编号
     *
     * @param channelId    频道编号
     * @param userId       用户编号
     * @param maxMessageId 最大已读消息编号
     */
    public void updateReadMaxMessageId(Long channelId, Long userId, Long maxMessageId) {
        String key = formatKey(channelId);
        stringRedisTemplate.opsForHash().put(key, userId.toString(), maxMessageId.toString());
    }

    /**
     * 获取用户在某频道的最大已读消息编号
     *
     * @param channelId 频道编号
     * @param userId    用户编号
     * @return 最大已读消息编号；不存在则返回 null
     */
    public Long getReadMaxMessageId(Long channelId, Long userId) {
        String key = formatKey(channelId);
        Object val = stringRedisTemplate.opsForHash().get(key, userId.toString());
        return Convert.toLong(val);
    }

    private static String formatKey(Long channelId) {
        return String.format(CHANNEL_MESSAGE_READ, channelId);
    }

}
