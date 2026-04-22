package cn.iocoder.yudao.module.im.dal.redis.message;

import cn.hutool.core.convert.Convert;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.GROUP_MESSAGE_READ;

/**
 * 群消息已读位置 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class GroupMessageReadRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 更新用户在某群的最大已读消息编号
     *
     * @param groupId      群编号
     * @param userId       用户编号
     * @param maxMessageId 最大已读消息编号
     */
    public void updateReadMaxMessageId(Long groupId, Long userId, Long maxMessageId) {
        String key = formatKey(groupId);
        stringRedisTemplate.opsForHash().put(key, userId.toString(), maxMessageId.toString());
    }

    /**
     * 获取用户在某群的最大已读消息编号
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 最大已读消息编号，不存在则返回 null
     */
    public Long getReadMaxMessageId(Long groupId, Long userId) {
        String key = formatKey(groupId);
        Object val = stringRedisTemplate.opsForHash().get(key, userId.toString());
        return Convert.toLong(val);
    }

    /**
     * 获取某群所有用户的已读位置
     *
     * @param groupId 群编号
     * @return userId → maxReadMessageId 映射
     */
    public Map<Long, Long> getReadMaxMessageIdMap(Long groupId) {
        String key = formatKey(groupId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        // 转换为 Long → Long 的 Map
        Map<Long, Long> result = new HashMap<>(entries.size());
        entries.forEach((k, v) -> result.put(Long.parseLong(k.toString()), Long.parseLong(v.toString())));
        return result;
    }

    /**
     * 删除用户在某群的已读位置
     *
     * @param groupId 群编号
     * @param userId  用户编号
     */
    public void deleteReadMaxMessageId(Long groupId, Long userId) {
        String key = formatKey(groupId);
        stringRedisTemplate.opsForHash().delete(key, userId.toString());
    }

    /**
     * 批量删除用户在某群的已读位置
     *
     * @param groupId 群编号
     * @param userIds 用户编号集合
     */
    public void deleteReadMaxMessageIds(Long groupId, Collection<Long> userIds) {
        String key = formatKey(groupId);
        Object[] hashKeys = userIds.stream().map(String::valueOf).toArray();
        stringRedisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * 删除某群所有用户的已读位置（整个 Hash Key）
     *
     * @param groupId 群编号
     */
    public void deleteReadMaxMessageIdMap(Long groupId) {
        String key = formatKey(groupId);
        stringRedisTemplate.delete(key);
    }

    private static String formatKey(Long groupId) {
        return String.format(GROUP_MESSAGE_READ, groupId);
    }

}
