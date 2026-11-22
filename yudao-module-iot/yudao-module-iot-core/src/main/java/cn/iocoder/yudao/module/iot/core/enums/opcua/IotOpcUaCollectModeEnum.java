package cn.iocoder.yudao.module.iot.core.enums.opcua;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT OPC UA 点位采集方式枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotOpcUaCollectModeEnum implements ArrayValuable<Integer> {

    /**
     * Read 轮询：网关按采集间隔主动读取
     */
    POLL(1, "轮询"),
    /**
     * MonitoredItem 订阅：服务端按采集间隔采样，值变化时推送
     */
    SUBSCRIBE(2, "订阅");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotOpcUaCollectModeEnum::getMode)
            .toArray(Integer[]::new);

    /**
     * 采集方式
     */
    private final Integer mode;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static IotOpcUaCollectModeEnum of(Integer mode) {
        return ArrayUtil.firstMatch(e -> e.getMode().equals(mode), values());
    }

}
