package cn.iocoder.dashboard.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 短信渠道枚举 TODO FROM 芋艿 TO zzf：属于短信的枚举类，可以放到 framework/sms 下
 *
 * @author zzf
 * @date 2021/1/25 10:56
 */
@Getter
@AllArgsConstructor
public enum SmsChannelEnum {

    ALI("ALI", "阿里"),
    HUA_WEI("HUA_WEI", "华为"),
    QI_NIU("QI_NIU", "七牛"),
    TEN_XUN("TEN_XUN", "腾讯"); // TODO FROM 芋艿 to zzf：TEN 有后鼻音哈，要被马爸爸打了。。。

    private final String code;

    private final String name;

    public static SmsChannelEnum getByCode(String code) {
        for (SmsChannelEnum value : SmsChannelEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
