package cn.iocoder.yudao.module.im.dal.redis.group;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.GROUP_READ_POSITION;

/**
 * 群已读位置 Redis DAO
 * <p>
 * 采用 Redis Hash 存储：key=im:group:read:{groupId}, field=userId, value=maxReadMessageId
 *
 * @author 芋道源码
 */
@Repository
public class GroupReadPositionRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 更新用户在某群的已读位置
     *
     * @param groupId      群编号
     * @param userId       用户编号
     * @param maxMessageId 最大已读消息编号
     */
    public void updateReadPosition(Long groupId, Long userId, Long maxMessageId) {
        String key = String.format(GROUP_READ_POSITION, groupId);
        // 只在新值大于旧值时更新
        Object currentVal = stringRedisTemplate.opsForHash().get(key, userId.toString());
        if (currentVal == null || Long.parseLong(currentVal.toString()) < maxMessageId) {
            stringRedisTemplate.opsForHash().put(key, userId.toString(), maxMessageId.toString());
        }
    }

    /**
     * 获取用户在某群的已读位置
     */
    public Long getReadPosition(Long groupId, Long userId) {
        String key = String.format(GROUP_READ_POSITION, groupId);
        Object val = stringRedisTemplate.opsForHash().get(key, userId.toString());
        return val == null ? 0L : Long.parseLong(val.toString());
    }

    /**
     * 获取某群所有用户的已读位置
     */
    public Map<Object, Object> getAllReadPositions(Long groupId) {
        String key = String.format(GROUP_READ_POSITION, groupId);
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除某群所有用户的已读位置（群解散时调用）
     */
    public void deleteGroupPositions(Long groupId) {
        String key = String.format(GROUP_READ_POSITION, groupId);
        stringRedisTemplate.delete(key);
    }

    /**
     * 删除用户在某群的已读位置（退群时调用）
     */
    public void deleteUserPosition(Long groupId, Long userId) {
        String key = String.format(GROUP_READ_POSITION, groupId);
        stringRedisTemplate.opsForHash().delete(key, userId.toString());
    }

}
