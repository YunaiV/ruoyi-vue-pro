package cn.iocoder.yudao.module.iot.core.enums.opcua;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// TODO @AI：是不是去掉噢？这个字段，以及历史数据；还有数据库字段；
/**
 * IoT OPC UA 认证方式枚举
 * <p>
 * 用户名的账号、口令不入库：配置只保存凭据别名，账号、口令写在网关本地配置里
 * <p>
 * OPC UA Client 只支持用户名认证：管理后台不允许保存匿名，网关拒绝匿名设备；
 * {@link #ANONYMOUS} 只保留用于识别、展示历史数据
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotOpcUaAuthTypeEnum implements ArrayValuable<Integer> {

    ANONYMOUS(1, "匿名"),
    USERNAME(2, "用户名");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotOpcUaAuthTypeEnum::getType)
            .toArray(Integer[]::new);

    /**
     * 认证方式
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static IotOpcUaAuthTypeEnum of(Integer type) {
        return ArrayUtil.firstMatch(e -> e.getType().equals(type), values());
    }

}
