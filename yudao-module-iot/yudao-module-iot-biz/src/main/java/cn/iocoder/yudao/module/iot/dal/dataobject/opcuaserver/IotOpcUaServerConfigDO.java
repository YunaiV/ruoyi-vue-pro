package cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT OPC UA Server 配置 DO
 * <p>
 * 网关作为 OPC UA Server，把设备属性以只读节点暴露给上位机（SCADA 等）。每份配置属于一个租户，
 * 上位机使用配置的凭据别名对应的账号登录后，只能看到该配置下的节点。
 * <p>
 * 不保存口令：只保存凭据别名，账号、口令写在网关本地配置里
 *
 * @author 芋道源码
 */
@TableName("iot_opc_ua_server_config")
@KeySequence("iot_opc_ua_server_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotOpcUaServerConfigDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 配置名称：也作为 OPC UA 地址空间中的文件夹名称
     */
    private String name;
    /**
     * 凭据别名：对应网关本地配置的 credentials；未删除的记录中全局唯一（跨租户，应用层校验）
     */
    private String credentialAlias;
    /**
     * 最大会话数：使用该凭据同时建立的会话上限
     */
    private Integer maxSessions;
    /**
     * 每个会话的最大订阅数
     */
    private Integer maxSubscriptionsPerSession;
    /**
     * 每个会话的最大 MonitoredItem 数
     */
    private Integer maxMonitoredItemsPerSession;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
