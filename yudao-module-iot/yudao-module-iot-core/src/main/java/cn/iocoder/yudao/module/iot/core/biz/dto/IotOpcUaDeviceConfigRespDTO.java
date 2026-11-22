package cn.iocoder.yudao.module.iot.core.biz.dto;

import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityPolicyEnum;
import lombok.Data;

import java.util.List;

/**
 * IoT OPC UA 设备配置 Response DTO
 * <p>
 * 不含任何秘密：用户名认证只下发凭据别名，账号、口令由网关本地配置提供
 *
 * @author 芋道源码
 */
@Data
public class IotOpcUaDeviceConfigRespDTO {

    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 租户编号（网关按它校验凭据别名允许的租户，并作为会话 key 的一部分，不同租户的设备不共用会话）
     */
    private Long tenantId;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;

    // ========== OPC UA 连接配置 ==========

    /**
     * 端点地址，例如 opc.tcp://192.168.1.10:4840
     */
    private String endpointUrl;
    /**
     * 安全策略
     *
     * 枚举 {@link IotOpcUaSecurityPolicyEnum}
     */
    private String securityPolicy;
    /**
     * 安全模式
     *
     * 枚举 {@link IotOpcUaSecurityModeEnum}
     */
    private String securityMode;
    /**
     * 认证方式
     *
     * 枚举 {@link IotOpcUaAuthTypeEnum}
     */
    private Integer authType;
    /**
     * 凭据别名（用户名认证时使用，对应网关本地配置的 credentials）
     */
    private String credentialAlias;
    /**
     * 请求超时时间，单位：毫秒
     */
    private Integer timeout;
    /**
     * 重试间隔，单位：毫秒
     */
    private Integer retryInterval;

    // ========== OPC UA 点位配置 ==========

    /**
     * 点位列表（只含启用、且物模型属性有效的点位）
     */
    private List<IotOpcUaPointRespDTO> points;

}
