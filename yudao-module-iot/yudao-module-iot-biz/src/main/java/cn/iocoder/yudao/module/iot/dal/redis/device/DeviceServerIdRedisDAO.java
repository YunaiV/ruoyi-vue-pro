package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * 设备关联的网关 serverId 的 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class DeviceServerIdRedisDAO {

    /**
     * 网关协议实例存活标记的过期时间：网关每 20 秒上报一次，可以容忍几轮上报失败、短时的 biz 重启
     */
    private static final Duration GATEWAY_SERVER_ALIVE_TIMEOUT = Duration.ofSeconds(120);

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

    /**
     * 获得网关协议实例的启动时间
     *
     * 只供离线检查任务使用：设备上线早于启动时间，说明网关重启过、设备未重连；不参与普通上下线的状态事务
     *
     * @param serverId 网关 serverId
     * @return 启动时间（毫秒）；标记不存在（网关已停止或宕机）时返回 null
     */
    public Long getGatewayServerStartTime(String serverId) {
        String value = stringRedisTemplate.opsForValue().get(IotDeviceMessageUtils.buildGatewayServerAliveKey(serverId));
        return value != null ? Long.valueOf(value) : null;
    }

    /**
     * 更新网关协议实例的启动时间，同时续期存活标记
     *
     * 直接覆盖，不比较新旧值：时钟回拨时，比较写入反而会拒绝新的启动时间
     *
     * @param serverId  网关 serverId
     * @param startTime 网关上报的启动时间（毫秒），不是 biz 的接收时间
     */
    public void updateGatewayServerStartTime(String serverId, Long startTime) {
        stringRedisTemplate.opsForValue().set(IotDeviceMessageUtils.buildGatewayServerAliveKey(serverId),
                String.valueOf(startTime), GATEWAY_SERVER_ALIVE_TIMEOUT);
    }

}