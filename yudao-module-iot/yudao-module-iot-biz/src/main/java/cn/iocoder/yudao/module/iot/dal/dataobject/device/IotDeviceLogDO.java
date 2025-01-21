package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备日志数据 DO
 *
 * 目前使用 TDengine 存储
 *
 * @author alwayssuper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceLogDO {

    // TODO @芋艿：消息 ID 的生成逻辑
    /**
     * 消息 ID
     */
    private String id;

    // TODO @super：关联要 @下
    /**
     * 产品标识
     * <p>
     * 关联 {@link IotProductDO#getProductKey()}
     */
    private String productKey;

    // TODO @super：关联要 @下
    /**
     * 设备标识
     * <p>
     * 关联 {@link IotDeviceDO#getDeviceKey()}}
     */
    private String deviceKey;

    // TODO @super：枚举类
    /**
     * 日志类型
     */
    private String type;

    // TODO @super：枚举类
    /**
     * 标识符：用于标识具体的属性、事件或服务
     */
    private String subType;

    /**
     * 数据内容
     *
     * 存储具体的消息数据内容，通常是 JSON 格式
     */
    private String content;

    /**
     * 上报时间戳
     */
    private Long reportTime;

    /**
     * 时序时间
     */
    private Long ts;

}
