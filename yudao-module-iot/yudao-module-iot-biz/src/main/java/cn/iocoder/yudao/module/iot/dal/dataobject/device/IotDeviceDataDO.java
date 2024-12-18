package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodel.IotProductThinkModelDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备数据 DO
 *
 * @author haohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceDataDO {

    /**
     * 设备编号
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;

    /**
     * 物模型编号
     * <p>
     * 关联 {@link IotProductThinkModelDO#getId()}
     */
    private Long thinkModelId;

    /**
     * 产品标识
     * <p>
     * 关联 {@link IotProductDO#getProductKey()}
     */
    private String productKey;

    /**
     * 设备名称
     * <p>
     * 冗余 {@link IotDeviceDO#getDeviceName()}
     */
    private String deviceName;

    /**
     * 属性标识符
     * <p>
     * 关联 {@link IotProductThinkModelDO#getIdentifier()}
     */
    private String identifier;

    /**
     * 属性名称
     * <p>
     * 关联 {@link IotProductThinkModelDO#getName()}
     */
    private String name;

    /**
     * 数据类型
     * <p>
     * 关联 {@link IotProductThinkModelDO#getProperty()#getDataType()}
     */
    private String dataType;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 最新值
     */
    private String value;

}