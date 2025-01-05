package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备日志数据 DO
 *
 * @author alwayssuper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceLogDO {
    /**
     * 消息ID
     */
    private String id;

    /**
     * 产品ID
     */
    private String productKey;

    /**
     * 设备ID
     */
    private String deviceKey;

    /**
     * 消息/日志类型
     */
    private String type;

    /**
     * 标识符：用于标识具体的属性、事件或服务
     */
    private String subType;

    /**
     * 数据内容：存储具体的消息数据内容，通常是JSON格式
     */
    private String content;

    /**
     * 上报时间戳
     */
    private DateTime reportTime;

    /**
     * 时序时间
     */
    private DateTime ts;


}
