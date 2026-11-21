package cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityPolicyEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.net.URI;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备 OPC UA 连接配置新增/修改 Request VO")
@Data
public class IotDeviceOpcUaConfigSaveReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "端点地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "opc.tcp://192.168.1.10:4840")
    @NotEmpty(message = "端点地址不能为空")
    @Size(max = 255, message = "端点地址长度不能超过 255 个字符")
    private String endpointUrl;

    @Schema(description = "安全策略", requiredMode = Schema.RequiredMode.REQUIRED, example = "Basic256Sha256")
    @NotEmpty(message = "安全策略不能为空")
    @InEnum(IotOpcUaSecurityPolicyEnum.class)
    private String securityPolicy;

    @Schema(description = "安全模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "SignAndEncrypt")
    @NotEmpty(message = "安全模式不能为空")
    @InEnum(IotOpcUaSecurityModeEnum.class)
    private String securityMode;

    @Schema(description = "认证方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "认证方式不能为空")
    @InEnum(IotOpcUaAuthTypeEnum.class)
    private Integer authType;

    @Schema(description = "凭据别名（用户名认证时必填，对应网关本地配置的 credentials）", example = "kep-reader")
    @Size(max = 64, message = "凭据别名长度不能超过 64 个字符")
    @Pattern(regexp = "^[A-Za-z0-9_.-]*$", message = "凭据别名只能包含字母、数字、下划线、点和中划线")
    private String credentialAlias;

    @Schema(description = "请求超时时间（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    @NotNull(message = "请求超时时间不能为空")
    @Min(value = 1000, message = "请求超时时间不能小于 1000 毫秒")
    @Max(value = 600000, message = "请求超时时间不能大于 600000 毫秒")
    private Integer timeout;

    @Schema(description = "重试间隔（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    @NotNull(message = "重试间隔不能为空")
    @Min(value = 1000, message = "重试间隔不能小于 1000 毫秒")
    @Max(value = 600000, message = "重试间隔不能大于 600000 毫秒")
    private Integer retryInterval;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @AssertTrue(message = "端点地址必须以 opc.tcp:// 开头，并包含主机和端口，不能带用户信息、查询参数和片段")
    @JsonIgnore
    public boolean isEndpointUrlValid() {
        if (endpointUrl == null) {
            return true;
        }
        try {
            URI uri = new URI(StrUtil.trim(endpointUrl));
            return StrUtil.equalsIgnoreCase("opc.tcp", uri.getScheme()) && StrUtil.isNotEmpty(uri.getHost())
                    && uri.getPort() > 0 && uri.getPort() <= 65535 && uri.getUserInfo() == null
                    && uri.getRawQuery() == null && uri.getRawFragment() == null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * None 允许保存（便于开发联调），网关默认拒绝，只有显式开启开关才放行
     */
    @AssertTrue(message = "安全策略和安全模式不匹配：None 策略只能配合 None 模式，其它策略只能配合 Sign 或 SignAndEncrypt 模式")
    @JsonIgnore
    public boolean isSecurityValid() {
        if (securityPolicy == null || securityMode == null) {
            return true;
        }
        return IotOpcUaSecurityPolicyEnum.NONE.getPolicy().equals(securityPolicy)
                == IotOpcUaSecurityModeEnum.NONE.getMode().equals(securityMode);
    }

    @AssertTrue(message = "用户名认证只能使用 SignAndEncrypt 模式，并且必须填写凭据别名")
    @JsonIgnore
    public boolean isAuthValid() {
        return !IotOpcUaAuthTypeEnum.USERNAME.getType().equals(authType)
                || (IotOpcUaSecurityModeEnum.SIGN_AND_ENCRYPT.getMode().equals(securityMode)
                && StrUtil.isNotBlank(credentialAlias));
    }

}
