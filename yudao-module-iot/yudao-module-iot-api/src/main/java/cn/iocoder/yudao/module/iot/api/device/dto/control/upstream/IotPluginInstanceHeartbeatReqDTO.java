package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * IoT 插件实例心跳 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotPluginInstanceHeartbeatReqDTO {

    /**
     * 请求编号
     */
    @NotEmpty(message = "请求编号不能为空")
    private String processId;

    /**
     * 插件包标识符
     */
    @NotEmpty(message = "插件包标识符不能为空")
    private String pluginKey;

    /**
     * 插件实例所在 IP
     */
    @NotEmpty(message = "插件实例所在 IP 不能为空")
    private String hostIp;
    /**
     * 插件实例的进程编号
     */
    @NotNull(message = "插件实例的进程编号不能为空")
    private Integer downstreamPort;

    /**
     * 是否在线
     */
    @NotNull(message = "是否在线不能为空")
    private Boolean online;

}
