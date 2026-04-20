package cn.iocoder.yudao.module.im.dal.redis.message;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.GROUP_READ_POSITION;

// TODO @AI：需要改成，GroupMessageReadRedisDAO
/**
 * 群已读位置 Redis DAO
 * <p>
 * 采用 Redis Hash 存储：key=im:group:read:{groupId}, field=userId, value=maxReadMessageId TODO @AI：这里不用注释；
 *
 * @author 芋道源码
 */
@Repository
public class GroupReadPositionRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // TODO @AI：updateReadMaxMessageId；
    /**
     * 更新用户在某群的已读位置
     *
     * @param groupId      群编号
     * @param userId       用户编号
     * @param maxMessageId 最大已读消息编号
     */
    public void updateReadPosition(Long groupId, Long userId, Long maxMessageId) {
        // TODO @AI：这里搞个 formatKey 方法，可以复用；
        String key = String.format(GROUP_READ_POSITION, groupId);
        // 只在新值大于旧值时更新
        Object currentVal = stringRedisTemplate.opsForHash().get(key, userId.toString());
        if (currentVal == null || Long.parseLong(currentVal.toString()) < maxMessageId) {
            // TODO @AI：可以 put 的时候，就是 Long，Long 么？
            stringRedisTemplate.opsForHash().put(key, userId.toString(), maxMessageId.toString());
        }
    }

    // TODO @AI：不存在，则返回 null 更合理；
    // TODO @AI：getReadMaxMessageId；
    /**
     * 获取用户在某群的已读位置
     */
    public Long getReadPosition(Long groupId, Long userId) {
        String key = String.format(GROUP_READ_POSITION, groupId);
        Object val = stringRedisTemplate.opsForHash().get(key, userId.toString());
        return val == null ? 0L : Long.parseLong(val.toString());
    }

    // TODO @AI：参考这个风格，优化下；
    /**
     * 获取某群所有用户的已读位置
     */
    public Map<Object, Object> getAllReadPositions(Long groupId) {
        // TODO @AI：可以返回的时候，就是 Long，Long 么？
        String key = String.format(GROUP_READ_POSITION, groupId);
        return stringRedisTemplate.opsForHash().entries(key);
    }

}
