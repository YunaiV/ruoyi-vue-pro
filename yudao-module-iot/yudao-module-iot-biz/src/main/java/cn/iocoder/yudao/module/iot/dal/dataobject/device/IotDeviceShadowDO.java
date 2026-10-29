package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * IoT 设备影子 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_device_shadow", autoResultMap = true)
@KeySequence("iot_device_shadow_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceShadowDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 设备编号
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;
    /**
     * 产品编号
     * <p>
     * 关联 {@link IotProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 上报状态
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private Map<String, Object> reported;
    /**
     * 期望状态
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private Map<String, Object> desired;
    /**
     * 元数据：{ desired: { key: { timestamp } }, reported: { ... } }
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private Map<String, Object> metadata;
    /**
     * 版本号（乐观锁）
     */
    private Long version;
    /**
     * 文档最后更新时间（epoch 秒）
     */
    private Long shadowTime;
    /**
     * 状态
     * <p>
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
