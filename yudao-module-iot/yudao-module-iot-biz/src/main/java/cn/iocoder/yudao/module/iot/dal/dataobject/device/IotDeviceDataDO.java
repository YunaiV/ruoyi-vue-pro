package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    // TODO @haohao：每个字段的关联关系，可以 @ 下哈。
    /**
     * 设备编号
     */
    private Long deviceId;

    /**
     * 物模型编号
     */
    private Long thinkModelFunctionId;

    /**
     * 产品标识
     */
    private String productKey;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 属性标识符
     */
    private String identifier;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 数据类型
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