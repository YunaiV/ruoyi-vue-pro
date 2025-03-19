package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 设备 DO
 *
 * @author haohao
 */
@TableName("iot_device")
@KeySequence("iot_device_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceDO extends BaseDO {

    /**
     * 设备 ID，主键，自增
     */
    @TableId
    private Long id;
    /**
     * 设备唯一标识符，全局唯一，用于识别设备
     */
    private String deviceKey;
    /**
     * 设备名称，在产品内唯一，用于标识设备
     */
    private String deviceName;
    /**
     * 设备备注名称
     */
    private String nickname;
    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 产品编号
     * <p>
     * 关联 {@link IotProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品标识
     * <p>
     * 冗余 {@link IotProductDO#getProductKey()}
     */
    private String productKey;
    /**
     * 设备类型
     * <p>
     * 冗余 {@link IotProductDO#getDeviceType()}
     */
    private Integer deviceType;

    /**
     * 设备状态
     * <p>
     * 枚举 {@link IotDeviceStatusEnum}
     */
    private Integer status;
    /**
     * 网关设备编号
     * <p>
     * 子设备需要关联的网关设备 ID
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long gatewayId;

    /**
     * 设备状态最后更新时间
     */
    private LocalDateTime statusLastUpdateTime;
    /**
     * 最后上线时间
     */
    private LocalDateTime lastOnlineTime;
    /**
     * 最后离线时间
     */
    private LocalDateTime lastOfflineTime;
    /**
     * 设备激活时间
     */
    private LocalDateTime activeTime;

    /**
     * 设备的 IP 地址
     */
    private String ip;
    /**
     * 设备的固件版本
     */
    private String firmwareVersion;

    /**
     * 设备密钥，用于设备认证，需安全存储
     */
    private String deviceSecret;
    /**
     * MQTT 客户端 ID
     */
    private String mqttClientId;
    /**
     * MQTT 用户名
     */
    private String mqttUsername;
    /**
     * MQTT 密码
     */
    private String mqttPassword;
    /**
     * 认证类型（如一机一密、动态注册）
     */
    // TODO @haohao：是不是要枚举哈
    private String authType;

    /**
     * 设备位置的纬度
     */
    private BigDecimal latitude;
    /**
     * 设备位置的经度
     */
    private BigDecimal longitude;
    /**
     * 地区编码
     * <p>
     * 关联 Area 的 id
     */
    private Integer areaId;
    /**
     * 设备详细地址
     */
    private String address;

}