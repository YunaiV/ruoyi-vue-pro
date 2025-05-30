package cn.iocoder.yudao.module.iot.net.component.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * IoT 网络组件服务配置属性
 *
 * @author haohao
 */
@ConfigurationProperties(prefix = "yudao.iot.component.server")
@Validated
@Data
public class IotNetComponentServerProperties {

    /**
     * 上行 URL，用于向主应用程序上报数据
     * <p>
     * 默认：http://127.0.0.1:48080
     */
    private String upstreamUrl = "http://127.0.0.1:48080";

    /**
     * 上行连接超时时间
     */
    private Duration upstreamConnectTimeout = Duration.ofSeconds(30);

    /**
     * 上行读取超时时间
     */
    private Duration upstreamReadTimeout = Duration.ofSeconds(30);

    /**
     * 下行服务端口，用于接收主应用程序的请求
     * <p>
     * 默认：18888
     */
    private Integer downstreamPort = 18888;

    /**
     * 组件服务器唯一标识
     * <p>
     * 默认：yudao-module-iot-net-component-server
     */
    private String serverKey = "yudao-module-iot-net-component-server";

}