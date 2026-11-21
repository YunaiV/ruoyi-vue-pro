package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityPolicyEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 设备 OPC UA 连接配置 DO
 * <p>
 * 不保存口令：用户名认证只保存凭据别名，账号、口令写在网关本地配置里
 *
 * @author 芋道源码
 */
@TableName("iot_device_opc_ua_config")
@KeySequence("iot_device_opc_ua_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceOpcUaConfigDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}；未删除的记录中唯一（应用层校验）
     */
    private Long deviceId;

    /**
     * 端点地址，例如 opc.tcp://192.168.1.10:4840
     */
    private String endpointUrl;
    /**
     * 安全策略
     *
     * 枚举 {@link IotOpcUaSecurityPolicyEnum}；保存 OPC UA 规范的策略名（与 Milo SecurityPolicy 的枚举名一致），
     * 与 Modbus 的字节序、原始数据类型一样使用协议自身的标识，不使用 int 枚举
     */
    private String securityPolicy;
    /**
     * 安全模式
     *
     * 枚举 {@link IotOpcUaSecurityModeEnum}；保存 OPC UA 规范的 MessageSecurityMode 名称，原因同 {@link #securityPolicy}
     */
    private String securityMode;
    /**
     * 认证方式
     *
     * 枚举 {@link IotOpcUaAuthTypeEnum}
     */
    private Integer authType;
    /**
     * 凭据别名：用户名认证时使用，对应网关本地配置的 credentials
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
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
