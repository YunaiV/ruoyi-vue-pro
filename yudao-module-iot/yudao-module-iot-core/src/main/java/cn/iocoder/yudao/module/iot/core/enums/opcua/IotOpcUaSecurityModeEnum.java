package cn.iocoder.yudao.module.iot.core.enums.opcua;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT OPC UA 消息安全模式枚举
 * <p>
 * 取值与 OPC UA 规范的 MessageSecurityMode 名称一致
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotOpcUaSecurityModeEnum implements ArrayValuable<String> {

    NONE("None", "不签名不加密"),
    SIGN("Sign", "签名"),
    SIGN_AND_ENCRYPT("SignAndEncrypt", "签名并加密");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotOpcUaSecurityModeEnum::getMode)
            .toArray(String[]::new);

    /**
     * 安全模式
     */
    private final String mode;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotOpcUaSecurityModeEnum of(String mode) {
        return ArrayUtil.firstMatch(e -> e.getMode().equals(mode), values());
    }

}
