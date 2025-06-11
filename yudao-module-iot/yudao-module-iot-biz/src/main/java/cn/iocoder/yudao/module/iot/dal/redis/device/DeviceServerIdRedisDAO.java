package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.util.StrUtil;
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

    /**
     * 更新设备关联的网关 serverId
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @param serverId   网关 serverId
     */
    public void update(String productKey, String deviceName, String serverId) {
        String hashKey = buildHashKey(productKey, deviceName);
        stringRedisTemplate.opsForHash().put(RedisKeyConstants.DEVICE_SERVER_ID, hashKey, serverId);
    }

    /**
     * 获得设备关联的网关 serverId
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 网关 serverId
     */
    public String get(String productKey, String deviceName) {
        String hashKey = buildHashKey(productKey, deviceName);
        Object value = stringRedisTemplate.opsForHash().get(RedisKeyConstants.DEVICE_SERVER_ID, hashKey);
        return value != null ? (String) value : null;
    }

    /**
     * 构建 HASH KEY
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return HASH KEY
     */
    private String buildHashKey(String productKey, String deviceName) {
        return productKey + StrUtil.COMMA + deviceName;
    }

}