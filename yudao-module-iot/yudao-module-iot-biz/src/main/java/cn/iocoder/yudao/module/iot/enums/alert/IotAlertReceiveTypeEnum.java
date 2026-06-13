package cn.iocoder.yudao.module.iot.enums.alert;

import cn.hutool.core.util.ArrayUtil;
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

    SMS(1, "iot_alert_sms"), // 短信
    MAIL(2, "iot_alert_mail"), // 邮箱
    NOTIFY(3, "iot_alert_notify"); // 站内信
    // TODO 待实现（欢迎 pull request）：webhook 4

    /**
     * 接收方式
     */
    private final Integer type;
    /**
     * 模板编号
     *
     * 关联 SmsTemplateDO / MailTemplateDO / NotifyTemplateDO 的 code 属性
     */
    private final String templateCode;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotAlertReceiveTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static IotAlertReceiveTypeEnum of(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
