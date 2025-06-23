package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * IoT 数据流转的数据源 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_data_flow_source", autoResultMap = true)
@KeySequence("iot_data_flow_source_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDataRuleSourceDO {

    /**
     * 数据源编号
     */
    private Long id;
    /**
     * 数据源名称
     */
    private String name;

    /**
     * 配置数组
     */
    private List<Config> configs;

    /**
     * 配置
     */
    @Data
    public static class Config {

        /**
         * 消息方法
         *
         * 枚举 {@link IotDeviceMessageMethodEnum} 中的 upstream 上行部分
         */
        private String method;

        /**
         * 产品编号
         *
         * 关联 {@link IotProductDO#getId()}
         */
        private Long productId;
        /**
         * 设备编号
         *
         * 关联 {@link IotDeviceDO#getId()}
         * 特殊：如果为 {@link IotDeviceDO#DEVICE_ID_ALL} 时，则是全部设备
         */
        private Long deviceId;

        /**
         * 标识符
         *
         * 1. 物模型时，对应：{@link IotThingModelDO#getIdentifier()}
         */
        private String identifier;

    }

}
