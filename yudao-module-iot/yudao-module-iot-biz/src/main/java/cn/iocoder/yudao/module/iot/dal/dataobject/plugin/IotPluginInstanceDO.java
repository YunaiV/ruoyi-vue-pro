package cn.iocoder.yudao.module.iot.dal.dataobject.plugin;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 插件实例 DO
 *
 * @author 芋道源码
 */
@TableName("iot_plugin_instance")
@KeySequence("iot_plugin_instance_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotPluginInstanceDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 插件编号
     * <p>
     * 关联 {@link IotPluginConfigDO#getId()}
     */
    private Long pluginId;
    /**
     * 插件进程编号
     *
     * 一般格式是：hostIp@processId@${uuid}
     */
    private String processId;

    /**
     * 插件实例所在 IP
     */
    private String hostIp;
    /**
     * 设备下行端口
     */
    private Integer downstreamPort;

    /**
     * 是否在线
     */
    private Boolean online;
    /**
     * 在线时间
     */
    private LocalDateTime onlineTime;
    /**
     * 离线时间
     */
    private LocalDateTime offlineTime;
    /**
     * 心跳时间
     *
     * 目的：心路时间超过一定时间后，会被进行下线处理
     */
    private LocalDateTime heartbeatTime;

}