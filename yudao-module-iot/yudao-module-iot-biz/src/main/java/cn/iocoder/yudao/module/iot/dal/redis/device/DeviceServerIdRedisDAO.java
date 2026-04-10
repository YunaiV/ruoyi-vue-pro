package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 设备关联的网关 serverId 的 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class DeviceServerIdRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void update(Long deviceId, String serverId) {
        stringRedisTemplate.opsForHash().put(RedisKeyConstants.DEVICE_SERVER_ID,
                String.valueOf(deviceId), serverId);
    }

    public String get(Long deviceId) {
        Object value = stringRedisTemplate.opsForHash().get(RedisKeyConstants.DEVICE_SERVER_ID,
                String.valueOf(deviceId));
        return value != null ? (String) value : null;
    }

}