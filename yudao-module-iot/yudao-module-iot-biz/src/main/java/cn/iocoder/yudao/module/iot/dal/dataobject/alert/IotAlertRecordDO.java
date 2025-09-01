package cn.iocoder.yudao.module.iot.dal.dataobject.alert;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 告警记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_alert_record", autoResultMap = true)
@KeySequence("iot_alert_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlertRecordDO extends BaseDO {

    /**
     * 记录编号
     */
    @TableId
    private Long id;
    /**
     * 告警名称
     *
     * 冗余 {@link IotAlertConfigDO#getId()}
     */
    private Long configId;
    /**
     * 告警名称
     *
     * 冗余 {@link IotAlertConfigDO#getName()}
     */
    private String configName;
    /**
     * 告警级别
     *
     * 冗余 {@link IotAlertConfigDO#getLevel()}
     * 字典 {@link cn.iocoder.yudao.module.iot.enums.DictTypeConstants#ALERT_LEVEL}
     */
    private Integer configLevel;
    /**
     * 场景规则编号
     *
     * 关联 {@link IotSceneRuleDO#getId()}
     */
    private Long sceneRuleId;

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
     */
    private Long deviceId;
    /**
     * 触发的设备消息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private IotDeviceMessage deviceMessage;

    /**
     * 是否处理
     */
    private Boolean processStatus;
    /**
     * 处理结果（备注）
     */
    private String processRemark;

}
