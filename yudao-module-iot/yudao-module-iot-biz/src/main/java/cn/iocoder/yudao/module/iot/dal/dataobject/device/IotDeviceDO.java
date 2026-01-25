package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.type.LongSetTypeHandler;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * IoT 设备 DO
 *
 * @author haohao
 */
@TableName(value = "iot_device", autoResultMap = true)
@KeySequence("iot_device_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceDO extends TenantBaseDO {

    /**
     * 设备编号 - 全部设备
     */
    public static final Long DEVICE_ID_ALL = 0L;

    /**
     * 设备 ID，主键，自增
     */
    @TableId
    private Long id;
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
     * 设备图片
     */
    private String picUrl;
    /**
     * 设备分组编号集合
     *
     * 关联 {@link IotDeviceGroupDO#getId()}
     */
    @TableField(typeHandler = LongSetTypeHandler.class)
    private Set<Long> groupIds;

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
     * 网关设备编号
     * <p>
     * 子设备需要关联的网关设备 ID
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long gatewayId;

    /**
     * 设备状态
     * <p>
     * 枚举 {@link IotDeviceStateEnum}
     */
    private Integer state;
    /**
     * 最后上线时间
     */
    private LocalDateTime onlineTime;
    /**
     * 最后离线时间
     */
    private LocalDateTime offlineTime;
    /**
     * 设备激活时间
     */
    private LocalDateTime activeTime;

    /**
     * 设备的 IP 地址
     */
    private String ip;
    /**
     * 固件编号
     *
     * 关联 {@link IotOtaFirmwareDO#getId()}
     */
    private Long firmwareId;

    /**
     * 设备密钥，用于设备认证
     */
    private String deviceSecret;

    /**
     * 设备位置的纬度
     */
    private BigDecimal latitude;
    /**
     * 设备位置的经度
     */
    private BigDecimal longitude;

    /**
     * 设备配置
     *
     * JSON 格式，可下发给 device 进行自定义配置
     */
    private String config;

}