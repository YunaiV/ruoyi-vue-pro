package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT OPC UA Server 协议配置
 * <p>
 * 网关把设备属性以只读节点暴露给上位机（SCADA、MES 等）：节点只读、写入一律拒绝，只支持 Read、Browse 和 MonitoredItem 订阅；
 * 默认关闭（application.yaml 中 enabled: false），每个部署只运行一个启用该协议的网关实例
 *
 * @author 芋道源码
 */
@Data
public class IotOpcUaServerConfig {

    /**
     * 配置刷新间隔（秒）
     */
    @NotNull(message = "配置刷新间隔不能为空")
    @Min(value = 1, message = "配置刷新间隔不能小于 1 秒")
    private Integer configRefreshInterval = 30;

    // ========== 监听 ==========

    /**
     * 监听地址
     */
    @NotEmpty(message = "监听地址不能为空")
    private String bindAddress = "0.0.0.0";
    /**
     * 端点中的主机名：客户端据此连接，需要是上位机可以访问的地址
     */
    @NotEmpty(message = "端点主机名不能为空")
    private String hostname = "localhost";
    /**
     * 端点路径
     */
    @NotEmpty(message = "端点路径不能为空")
    private String path = "/yudao-iot";

    // ========== 证书与安全 ==========

    /**
     * PKI 目录：own/server.pfx（服务端证书）、trusted/certs（信任的客户端证书）、issuer/certs、rejected（未信任的客户端证书）
     * <p>
     * 未信任的客户端证书放进 rejected/ 并拒绝连接；人工移入 trusted/certs/ 后生效
     */
    @NotEmpty(message = "PKI 目录不能为空")
    private String pkiDir = "data/opc-ua-server/pki";
    /**
     * 服务端 keystore（PKCS12）口令：协议启动时校验不能为空
     */
    private String keystorePassword;
    /**
     * 服务端 keystore 中证书条目的别名
     */
    @NotEmpty(message = "keystore 别名不能为空")
    private String keystoreAlias = "yudao-opc-ua-server";
    /**
     * 应用名称：ApplicationName、自动生成的自签名证书 CN
     */
    @NotEmpty(message = "应用名称不能为空")
    private String applicationName = "Yudao IoT Gateway OPC UA Server";
    /**
     * 是否允许 None 安全策略：默认禁止，只作为显式的开发联调开关；开启后仍然只允许用户名登录（口令按 Basic256Sha256 加密）
     * <p>
     * 未开启时，None 只用于 GetEndpoints 发现端点（路径 /discovery），在 None 安全通道上激活会话一律拒绝
     */
    @NotNull(message = "是否允许 None 安全策略不能为空")
    private Boolean securityNoneAllowed = false;

    // ========== 限额（网关级上限；配置级上限取所有启用配置中的较小值） ==========

    /**
     * 网关最大会话数
     */
    @NotNull(message = "最大会话数不能为空")
    @Min(value = 1, message = "最大会话数不能小于 1")
    private Integer maxSessions = 20;
    /**
     * 每个会话的最大订阅数上限
     */
    @NotNull(message = "每个会话的最大订阅数不能为空")
    @Min(value = 1, message = "每个会话的最大订阅数不能小于 1")
    private Integer maxSubscriptionsPerSession = 5;
    /**
     * 每个会话的最大 MonitoredItem 数上限
     */
    @NotNull(message = "每个会话的最大 MonitoredItem 数不能为空")
    @Min(value = 1, message = "每个会话的最大 MonitoredItem 数不能小于 1")
    private Integer maxMonitoredItemsPerSession = 1000;

    // ========== 凭据 ==========

    /**
     * 用户名凭据：key 为凭据别名（OPC UA Server 配置的 credentialAlias），账号、口令只保存在网关本地
     */
    private Map<String, Credential> credentials = new LinkedHashMap<>();

    /**
     * 用户名凭据
     */
    @Data
    public static class Credential {

        /**
         * 用户名
         */
        private String username;
        /**
         * 口令
         */
        private String password;
        /**
         * 允许使用的租户：配置的租户必须命中
         */
        private List<Long> tenantIds = new ArrayList<>();

    }

}
