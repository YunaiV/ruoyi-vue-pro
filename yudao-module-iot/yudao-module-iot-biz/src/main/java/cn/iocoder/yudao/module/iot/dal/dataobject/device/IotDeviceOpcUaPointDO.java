package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 设备 OPC UA 点位配置 DO
 * <p>
 * 不冗余属性标识符和名称：查询时关联物模型，物模型改名后无需同步
 *
 * @author 芋道源码
 */
@TableName("iot_device_opc_ua_point")
@KeySequence("iot_device_opc_ua_point_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceOpcUaPointDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;
    /**
     * 物模型属性编号
     *
     * 关联 {@link IotThingModelDO#getId()}；同一设备未删除的记录中唯一（应用层校验）
     */
    private Long thingModelId;

    // ========== OPC UA 协议配置 ==========

    /**
     * NodeId，例如 nsu=urn:example;s=Temperature、ns=2;i=1001
     */
    private String nodeId;
    /**
     * 采集方式
     *
     * 枚举 {@link IotOpcUaCollectModeEnum}
     */
    private Integer collectMode;
    /**
     * 采集间隔，单位：毫秒（轮询周期，或订阅的发布、采样间隔）
     */
    private Integer collectInterval;
    /**
     * 是否可写：默认 false；为 true 时才允许 thing.property.set 写入该节点的 Value 属性（物模型属性还需要为读写）
     */
    private Boolean writable;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
