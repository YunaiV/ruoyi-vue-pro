package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import static cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants.DEVICE_SHADOW;

/**
 * {@link IotDeviceShadowDO} 的 Redis DAO
 * <p>
 * 影子缓存：DB 为准，Redis 仅作读缓存（cache-aside），写后失效。
 *
 * @author 芋道源码
 */
@Repository
public class DeviceShadowRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public IotDeviceShadowDO get(Long deviceId) {
        String redisKey = formatKey(deviceId);
        String json = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObject(json, IotDeviceShadowDO.class);
    }

    public void set(IotDeviceShadowDO shadow) {
        String redisKey = formatKey(shadow.getDeviceId());
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(shadow));
    }

    public void delete(Long deviceId) {
        stringRedisTemplate.delete(formatKey(deviceId));
    }

    private static String formatKey(Long deviceId) {
        return String.format(DEVICE_SHADOW, deviceId);
    }

}
