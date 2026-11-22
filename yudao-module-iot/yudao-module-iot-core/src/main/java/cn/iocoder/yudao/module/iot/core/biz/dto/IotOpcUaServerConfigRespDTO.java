package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;

import java.util.List;

/**
 * IoT OPC UA Server 配置 Response DTO
 * <p>
 * 只下发启用的配置及其启用的节点；不含任何秘密，账号、口令由网关按凭据别名读取本地配置
 *
 * @author 芋道源码
 */
@Data
public class IotOpcUaServerConfigRespDTO {

    /**
     * 配置编号
     */
    private Long id;
    /**
     * 租户编号
     *
     * 网关的用途：
     * 1. 凭据授权：必须命中凭据别名允许的 tenant-ids，防止配置引用其它租户的凭据别名
     * 2. 匹配上报：只有租户一致的属性上报才会更新节点值，并作为最近值缓存键（tenantId|deviceId|identifier）的一部分
     *
     * 会话可见的节点按配置编号隔离，不依赖该字段
     */
    private Long tenantId;
    /**
     * 配置名称
     */
    private String name;
    /**
     * 凭据别名
     */
    private String credentialAlias;
    /**
     * 最大会话数
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
     * 节点列表
     */
    private List<IotOpcUaServerNodeRespDTO> nodes;

}
