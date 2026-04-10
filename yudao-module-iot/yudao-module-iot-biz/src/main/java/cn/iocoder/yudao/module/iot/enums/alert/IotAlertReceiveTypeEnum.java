package cn.iocoder.yudao.module.iot.enums.alert;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 告警的接收方式枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotAlertReceiveTypeEnum implements ArrayValuable<Integer> {

    SMS(1), // 短信
    MAIL(2), // 邮箱
    NOTIFY(3); // 站内信
    // TODO 待实现（欢迎 pull request）：webhook 4

    private final Integer type;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotAlertReceiveTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
