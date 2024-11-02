package cn.iocoder.yudao.module.iot.dal.redis.deviceData;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants.DEVICE_PROPERTY_DATA;

/**
 * {@link IotDeviceDataDO} 的 Redis DAO
 */
@Repository
public class DeviceDataRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public IotDeviceDataDO get(String productKey, String deviceName, String identifier) {
        String redisKey = formatKey(productKey, deviceName, identifier);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), IotDeviceDataDO.class);
    }

    public void set(IotDeviceDataDO deviceData) {
        String redisKey = formatKey(deviceData.getProductKey(), deviceData.getDeviceName(), deviceData.getIdentifier());
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(deviceData));
    }

    public void delete(String productKey, String deviceName, String identifier) {
        String redisKey = formatKey(productKey, deviceName, identifier);
        stringRedisTemplate.delete(redisKey);
    }

    private static String formatKey(String productKey, String deviceName, String identifier) {
        return String.format(DEVICE_PROPERTY_DATA, productKey, deviceName, identifier);
    }
}
