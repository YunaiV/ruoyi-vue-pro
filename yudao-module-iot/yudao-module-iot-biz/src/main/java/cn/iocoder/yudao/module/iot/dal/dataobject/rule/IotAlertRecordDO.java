package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

/**
 * IoT 告警记录 DO
 *
 * @author 芋道源码
 */
@TableName("iot_alert_record")
@KeySequence("iot_alert_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlertRecordDO extends BaseDO {

    /**
     * 记录编号
     */
    @TableField
    private Long id;
    /**
     * 告警名称
     *
     * 冗余 {@link IotAlertConfig#getName()}
     */
    private Long configId;
    /**
     * 告警名称
     *
     * 冗余 {@link IotAlertConfig#getName()}
     */
    private String name;

    /**
     * 产品标识
     *
     * 关联 {@link IotProductDO#getProductKey()} ()}
     */
    private String productKey;
    /**
     * 设备名称
     *
     * 冗余 {@link IotDeviceDO#getDeviceName()}
     */
    private String deviceName;

    // TODO @芋艿：有没更好的方式
    /**
     * 触发的设备消息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private IotDeviceMessage deviceMessage;

    // TODO @芋艿：换成枚举，枚举对应 ApiErrorLogProcessStatusEnum
    /**
     * 处理状态
     *
     * true - 已处理
     * false - 未处理
     */
    private Boolean processStatus;
    /**
     * 处理结果（备注）
     */
    private String processRemark;

}
