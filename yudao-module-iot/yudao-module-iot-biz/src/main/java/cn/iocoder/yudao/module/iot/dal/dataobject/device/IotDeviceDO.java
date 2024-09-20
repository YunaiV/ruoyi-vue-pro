package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 设备 DO
 *
 * @author 芋道源码
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
     * 产品 ID，关联 iot_product 表的 id
     * 关联 {@link IotProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品 Key，关联 iot_product 表的 product_key
     * 关联 {@link IotProductDO#getProductKey()}
     */
    private String productKey;
    /**
     * 设备类型：0 - 直连设备，1 - 网关子设备，2 - 网关设备
     * 关联 {@link IotProductDO#getDeviceType()}
     */
    private Integer deviceType;
    /**
     * 设备备注名称，供用户自定义备注
     */
    private String nickname;
    /**
     * 网关设备 ID，子设备需要关联的网关设备 ID
     */
    private Long gatewayId;
    /**
     * 设备状态：0 - 未激活，1 - 在线，2 - 离线，3 - 已禁用
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum}
     */
    private Integer status;
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
    private String authType;
    /**
     * 设备位置的纬度，范围 -90.000000 ~ 90.000000
     */
    private BigDecimal latitude;
    /**
     * 设备位置的经度，范围 -180.000000 ~ 180.000000
     */
    private BigDecimal longitude;
    /**
     * 地区编码，符合国家地区编码标准，关联地区表
     */
    private Integer areaId;
    /**
     * 设备详细地址
     */
    private String address;
    /**
     * 设备序列号
     */
    private String serialNumber;

}