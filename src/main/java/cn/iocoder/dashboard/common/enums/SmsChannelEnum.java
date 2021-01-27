package cn.iocoder.dashboard.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信渠道枚举
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
    TEN_XUN("TEN_XUN", "腾讯");

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
