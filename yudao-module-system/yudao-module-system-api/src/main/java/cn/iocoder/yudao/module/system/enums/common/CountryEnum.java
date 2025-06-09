package cn.iocoder.yudao.module.system.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 国家的枚举类
 */
@Getter
@AllArgsConstructor
public enum CountryEnum {

    CHINA("CN", "中国"),
    USA("US", "美国"),
    JAPAN("JP", "日本");
    private final String countryCode;
    private final String countryName;
}
