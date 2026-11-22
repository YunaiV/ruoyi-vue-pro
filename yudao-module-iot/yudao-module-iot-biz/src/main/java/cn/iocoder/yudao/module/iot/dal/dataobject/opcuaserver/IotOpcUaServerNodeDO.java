package cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT OPC UA Server 节点 DO
 * <p>
 * 一个节点对应一个设备的一个物模型属性，只读：上位机可以 Read、订阅 MonitoredItem，写入一律拒绝
 *
 * @author 芋道源码
 */
@TableName("iot_opc_ua_server_node")
@KeySequence("iot_opc_ua_server_node_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotOpcUaServerNodeDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 配置编号
     *
     * 关联 {@link IotOpcUaServerConfigDO#getId()}
     */
    private Long configId;
    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;
    /**
     * 物模型编号：设备所属产品的属性
     *
     * 关联 {@link IotThingModelDO#getId()}；不冗余标识符，网关配置接口按关联结果下发
     */
    private Long thingModelId;
    /**
     * 浏览名称：同一配置下唯一，网关据此生成 NodeId（ns=服务端命名空间;s=配置编号/浏览名称）
     */
    private String browseName;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
