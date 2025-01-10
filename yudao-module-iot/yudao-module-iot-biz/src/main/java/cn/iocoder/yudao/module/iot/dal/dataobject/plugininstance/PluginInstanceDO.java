package cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;


/**
 * IoT 插件实例 DO
 *
 * @author 芋道源码
 */
@TableName("iot_plugin_instance")
@KeySequence("iot_plugin_instance_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginInstanceDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 插件主程序 ID
     */
    private String mainId;
    /**
     * 插件 ID
     * <p>
     * 关联 {@link PluginInfoDO#getId()}
     */
    private Long pluginId;

    /**
     * 插件主程序所在 IP
     */
    private String ip;
    /**
     * 插件主程序端口
     */
    private Integer port;

    // TODO @haohao：字段改成 heartbeatTime，LocalDateTime
    /**
     * 心跳时间，心路时间超过 30 秒需要剔除
     */
    private Long heartbeatAt;

}