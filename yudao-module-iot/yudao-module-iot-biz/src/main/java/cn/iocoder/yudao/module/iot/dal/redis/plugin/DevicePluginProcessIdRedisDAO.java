package cn.iocoder.yudao.module.iot.dal.redis.plugin;

import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 设备插件的插件进程编号的缓存的 Redis DAO
 */
@Repository
public class DevicePluginProcessIdRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void put(String deviceKey, String processId) {
        stringRedisTemplate.opsForHash().put(RedisKeyConstants.DEVICE_PLUGIN_INSTANCE_PROCESS_IDS, deviceKey, processId);
    }

    public String get(String deviceKey) {
        return (String) stringRedisTemplate.opsForHash().get(RedisKeyConstants.DEVICE_PLUGIN_INSTANCE_PROCESS_IDS, deviceKey);
    }

}