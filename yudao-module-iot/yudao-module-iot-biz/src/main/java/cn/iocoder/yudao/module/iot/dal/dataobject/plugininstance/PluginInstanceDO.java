package cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

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
     * 插件主程序id
     */
    private String mainId;
    /**
     * 插件id
     */
    private Long pluginId;
    /**
     * 插件主程序所在ip
     */
    private String ip;
    /**
     * 插件主程序端口
     */
    private Integer port;
    /**
     * 心跳时间，心路时间超过30秒需要剔除
     */
    private Long heartbeatAt;

}