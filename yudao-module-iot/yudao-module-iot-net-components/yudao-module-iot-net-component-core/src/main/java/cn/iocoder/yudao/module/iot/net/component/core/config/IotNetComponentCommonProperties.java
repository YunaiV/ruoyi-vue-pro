package cn.iocoder.yudao.module.iot.net.component.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * IoT 网络组件通用配置属性
 *
 * @author haohao
 */
@ConfigurationProperties(prefix = "yudao.iot.component")
@Validated
@Data
public class IotNetComponentCommonProperties {

    /**
     * 组件的唯一标识
     * <p>
     * 注意：该值将在运行时由各组件设置，不再从配置读取
     */
    private String pluginKey;

    /**
     * 组件实例心跳超时时间，单位：毫秒
     * <p>
     * 默认值：30 秒
     */
    private Long instanceHeartbeatTimeout = 30000L;

    /**
     * 网络组件消息转发配置
     */
    private ForwardMessage forwardMessage = new ForwardMessage();

    /**
     * 消息转发配置
     */
    @Data
    public static class ForwardMessage {

        /**
         * 是否转发所有设备消息到 EMQX
         * <p>
         * 默认为 true 开启
         */
        private boolean forwardAllDeviceMessageToEmqx = true;

        /**
         * 是否转发所有设备消息到 HTTP
         * <p>
         * 默认为 false 关闭
         */
        private boolean forwardAllDeviceMessageToHttp = false;
    }
}