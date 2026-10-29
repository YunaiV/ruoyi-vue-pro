package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;

import static cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants.DEVICE_SHADOW;

/**
 * {@link IotDeviceShadowDO} 的 Redis DAO
 * <p>
 * 影子缓存：DB 为准，Redis 仅作读缓存（cache-aside），写后失效，并设置较短的过期时间兜底。
 *
 * @author 芋道源码
 */
@Repository
public class DeviceShadowRedisDAO {

    /**
     * 缓存过期时间：并发回填旧数据时，最多在该时间内读到旧影子
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public IotDeviceShadowDO get(Long deviceId) {
        String redisKey = formatKey(deviceId);
        String json = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObject(json, IotDeviceShadowDO.class);
    }

    public void set(IotDeviceShadowDO shadow) {
        String redisKey = formatKey(shadow.getDeviceId());
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(shadow), TIMEOUT);
    }

    public void delete(Long deviceId) {
        stringRedisTemplate.delete(formatKey(deviceId));
    }

    private static String formatKey(Long deviceId) {
        return String.format(DEVICE_SHADOW, deviceId);
    }

}
