package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * IoT 数据流转规则 DO
 *
 * 监听 {@link SourceConfig} 数据源，转发到 {@link IotDataSinkDO} 数据目的
 *
 * @author 芋道源码
 */
@TableName(value = "iot_data_rule", autoResultMap = true)
@KeySequence("iot_data_rule_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDataRuleDO extends BaseDO {

    /**
     * 数据流转规格编号
     */
    private Long id;
    /**
     * 数据流转规格名称
     */
    private String name;
    /**
     * 数据流转规格描述
     */
    private String description;
    /**
     * 数据流转规格状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 数据源配置数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SourceConfig> sourceConfigs;
    /**
     * 数据目的编号数组
     *
     * 关联 {@link IotDataSinkDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> sinkIds;

    // TODO @芋艿：未来考虑使用 groovy；支持数据处理；

    /**
     * 数据源配置
     */
    @Data
    public static class SourceConfig {

        /**
         * 消息方法
         *
         * 枚举 {@link IotDeviceMessageMethodEnum} 中的 upstream 上行部分
         */
        @NotEmpty(message = "消息方法不能为空")
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
        @NotEmpty(message = "设备编号不能为空")
        private Long deviceId;

        /**
         * 标识符
         *
         * 1. 物模型时，对应：{@link IotThingModelDO#getIdentifier()}
         */
        private String identifier;

    }

}
