package cn.iocoder.yudao.module.iot.dal.dataobject.plugin;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginDeployTypeEnum;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 插件配置 DO
 *
 * @author 芋道源码
 */
@TableName("iot_plugin_config")
@KeySequence("iot_plugin_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotPluginConfigDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;
    /**
     * 插件包标识符
     */
    private String pluginKey;
    /**
     * 插件名称
     */
    private String name;
    /**
     * 插件描述
     */
    private String description;
    /**
     * 部署方式
     * <p>
     * 枚举 {@link IotPluginDeployTypeEnum}
     */
    private Integer deployType;
    // TODO @芋艿：如果是外置的插件，fileName 和 version 的选择~
    /**
     * 插件包文件名
     */
    private String fileName;
    /**
     * 插件版本
     */
    private String version;
    // TODO @芋艿：type 字典的定义
    /**
     * 插件类型
     * <p>
     * 枚举 {@link IotPluginTypeEnum}
     */
    private Integer type;
    /**
     * 设备插件协议类型
     */
    // TODO @芋艿：枚举字段
    private String protocol;
    // TODO @haohao：这个字段，是不是直接用 CommonStatus，开启、禁用；然后插件实例那，online 是否在线
    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // TODO @芋艿：configSchema、config 示例字段
    /**
     * 插件配置项描述信息
     */
    private String configSchema;
    /**
     * 插件配置信息
     */
    private String config;

    // TODO @芋艿：script 后续的使用
    /**
     * 插件脚本
     */
    private String script;

}