package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT OPC UA Client 协议配置
 * <p>
 * 单网关长连接：每个部署只运行一个负责 OPC UA 协议的网关实例，部署多个实例会重复采集（与 Modbus TCP Client 相同）
 *
 * @author 芋道源码
 */
@Data
public class IotOpcUaClientConfig {

    /**
     * 配置刷新间隔（秒）
     */
    @NotNull(message = "配置刷新间隔不能为空")
    @Min(value = 1, message = "配置刷新间隔不能小于 1 秒")
    private Integer configRefreshInterval = 30;
    /**
     * 设备上下线消息发送失败后的重试间隔（秒）：状态消息不走离线暂存，失败后按此间隔重试
     */
    @NotNull(message = "状态重试间隔不能为空")
    @Min(value = 1, message = "状态重试间隔不能小于 1 秒")
    private Integer stateRetryInterval = 30;

    // ========== 证书与安全 ==========

    /**
     * PKI 目录：trusted/（信任的服务端证书）、rejected/（未信任的服务端证书）和本网关的客户端 keystore
     * <p>
     * 未信任的服务端证书放进 rejected/ 并拒绝连接；人工移入 trusted/certs/ 后生效
     */
    @NotEmpty(message = "PKI 目录不能为空")
    private String pkiDir = "data/opc-ua/pki";
    /**
     * 客户端 keystore（PKCS12）口令：协议启动时校验不能为空；keystore 读取失败时协议启动失败，不回退匿名
     */
    private String keystorePassword;
    /**
     * 客户端 keystore 中证书条目的别名：自动生成证书时使用；运维放入 CA 签发的证书时，私钥和证书链要放在该别名下
     */
    @NotEmpty(message = "keystore 别名不能为空")
    private String keystoreAlias = "yudao-opc-ua-client";
    /**
     * 应用名称：会话中的 ApplicationName、自动生成的自签名证书 CN；多个网关连接同一服务端时，便于在服务端区分
     * <p>
     * 修改后只影响新生成的证书，已有证书的 CN 不变；应用 URI 始终取客户端证书 SAN 中的 URI
     */
    @NotEmpty(message = "应用名称不能为空")
    private String applicationName = "Yudao IoT Gateway OPC UA Client";
    /**
     * 服务端返回的端点主机、端口与配置不一致时，是否改写为配置的主机、端口（默认报错）
     */
    @NotNull(message = "是否改写端点主机不能为空")
    private Boolean endpointHostRewrite = false;

    // ========== 采集与上报 ==========

    /**
     * 每次 Read 的最大节点数：与服务端 OperationLimits.MaxNodesPerRead 取较小值；服务端缺失或为 0 时使用本值
     */
    @NotNull(message = "每次读取的最大节点数不能为空")
    @Min(value = 1, message = "每次读取的最大节点数不能小于 1")
    private Integer maxNodesPerRead = 100;
    /**
     * 每条属性上报消息的最大点位数：超过时拆分为多条
     */
    @NotNull(message = "每条消息的最大点位数不能为空")
    @Min(value = 1, message = "每条消息的最大点位数不能小于 1")
    private Integer maxPointsPerMessage = 100;
    /**
     * 每条属性上报消息的最大字节数（属性 JSON 的 UTF-8 字节数）：超过时拆分为多条；要低于离线暂存和消息总线的单条上限
     */
    @NotNull(message = "每条消息的最大字节数不能为空")
    @Min(value = 1024, message = "每条消息的最大字节数不能小于 1024")
    private Integer maxMessageBytes = 256 * 1024;

    // ========== 订阅 ==========

    /**
     * 每次创建、修改、删除 MonitoredItem 的最大数量：与服务端 OperationLimits.MaxMonitoredItemsPerCall 取较小值；
     * 服务端缺失或为 0 时使用本值
     */
    @NotNull(message = "每次调用的最大 MonitoredItem 数不能为空")
    @Min(value = 1, message = "每次调用的最大 MonitoredItem 数不能小于 1")
    private Integer maxMonitoredItemsPerCall = 100;
    /**
     * 订阅的 MaxKeepAliveCount：没有数据变化时，每隔 发布间隔 × 本值 发送一次 KeepAlive
     */
    @NotNull(message = "订阅 KeepAlive 次数不能为空")
    @Min(value = 1, message = "订阅 KeepAlive 次数不能小于 1")
    private Integer subscriptionKeepAliveCount = 10;
    /**
     * 订阅的 LifetimeCount：必须大于等于 3 × subscriptionKeepAliveCount
     */
    @NotNull(message = "订阅 Lifetime 次数不能为空")
    @Min(value = 3, message = "订阅 Lifetime 次数不能小于 3")
    private Integer subscriptionLifetimeCount = 30;

    // ========== 属性写入 ==========

    /**
     * 属性写入超时（毫秒）：从收到 thing.property.set 开始计算，包含在会话队列中的排队、读取节点属性和写入；
     * 超过时回复超时，排队中尚未发出的写入不再执行
     */
    @NotNull(message = "属性写入超时不能为空")
    @Min(value = 100, message = "属性写入超时不能小于 100 毫秒")
    private Integer writeTimeout = 10000;
    /**
     * 每个设备排队中（含执行中）的属性写入消息上限：超过时直接回复排队已满
     */
    @NotNull(message = "每个设备排队的写入数不能为空")
    @Min(value = 1, message = "每个设备排队的写入数不能小于 1")
    private Integer maxPendingWritesPerDevice = 16;
    /**
     * 每次 Write 的最大节点数：与服务端 OperationLimits.MaxNodesPerWrite 取较小值；服务端缺失或为 0 时使用本值
     */
    @NotNull(message = "每次写入的最大节点数不能为空")
    @Min(value = 1, message = "每次写入的最大节点数不能小于 1")
    private Integer maxNodesPerWrite = 100;

    // ========== 凭据 ==========

    /**
     * 用户名凭据：key 为凭据别名（设备配置的 credentialAlias），账号、口令只保存在网关本地
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
         * 允许使用的租户：设备的租户必须命中
         */
        private List<Long> tenantIds = new ArrayList<>();
        /**
         * 允许使用的端点：设备配置的端点地址必须命中
         */
        private List<String> endpoints = new ArrayList<>();

    }

}
