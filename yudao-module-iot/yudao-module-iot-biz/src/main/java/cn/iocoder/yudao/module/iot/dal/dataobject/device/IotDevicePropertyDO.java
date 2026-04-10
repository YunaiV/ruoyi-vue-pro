package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备属性项 Redis DO
 *
 * @see cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants#DEVICE_PROPERTY
 * @see DevicePropertyRedisDAO
 *
 * @author haohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDevicePropertyDO {

    /**
     * 属性值（最新）
     */
    private Object value;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}