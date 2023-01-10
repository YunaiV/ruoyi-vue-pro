package cn.iocoder.yudao.framework.desensitize.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cn.iocoder.yudao.framework.desensitize.constants.DesensitizeConstants.DEFAULT_KEEP_LENGTH;
import static cn.iocoder.yudao.framework.desensitize.constants.DesensitizeConstants.DEFAULT_REGEX;
import static cn.iocoder.yudao.framework.desensitize.constants.DesensitizeConstants.DEFAULT_REPLACER;

@Getter
@RequiredArgsConstructor
public enum DesensitizationStrategyEnum {
    // 常用脱敏业务
    PHONE_NUMBER(DEFAULT_REGEX, 3, 4, DEFAULT_REPLACER), // 手机号;比如：13248765917脱敏之后为132****5917
    FIXED_PHONE(DEFAULT_REGEX, 4, 2, DEFAULT_REPLACER), // 固定电话;比如：01086551122脱敏之后为0108*****22
    ID_CARD(DEFAULT_REGEX, 6, 2, DEFAULT_REPLACER), // 身份证号码;比如：530321199204074611脱敏之后为530321**********11
    BANK_CARD(DEFAULT_REGEX, 6, 2, DEFAULT_REPLACER), // 银行卡号;比如：9988002866797031脱敏之后为998800********31
    CHINESE_NAME(DEFAULT_REGEX, 1, 0, "**"),// 中文名;比如：刘子豪脱敏之后为刘**
    ADDRESS("[\\s\\S]+区", DEFAULT_KEEP_LENGTH, DEFAULT_KEEP_LENGTH, DEFAULT_REPLACER), // 地址只显示到地区，不显示详细地址;比如：广州市天河区幸福小区102号脱敏之后为广州市天河区********
    EMAIL("(^.)[^@]*(@.*$)", DEFAULT_KEEP_LENGTH, DEFAULT_KEEP_LENGTH, "$1****$2"), // 邮箱;比如：example@gmail.com脱敏之后为e******@gmail.com
    CAR_LICENSE(DEFAULT_REGEX, 3, 1, DEFAULT_REPLACER), // 车牌号;比如：粤A66666脱敏之后为粤A6***6
    PASSWORD(DEFAULT_REGEX, 0, 0, DEFAULT_REPLACER), // 密码;比如：123456脱敏之后为******

    // 自定义脱敏业务
    REGEX(DEFAULT_REGEX, DEFAULT_KEEP_LENGTH, DEFAULT_KEEP_LENGTH, DEFAULT_REPLACER), // 自定义正则表达式
    SLIDE(DEFAULT_REGEX, DEFAULT_KEEP_LENGTH, DEFAULT_KEEP_LENGTH, DEFAULT_REPLACER), // 滑动脱敏
    CUSTOM_HANDLE(DEFAULT_REGEX, DEFAULT_KEEP_LENGTH, DEFAULT_KEEP_LENGTH, DEFAULT_REPLACER); // 自定义处理器
    ;

    /**
     * 正则表达式
     */
    private final String regex;

    /**
     * 前缀保留长度
     */
    private final int preKeep;

    /**
     * 后缀保留长度
     */
    private final int suffixKeep;

    /**
     * 脱敏替换字符
     */
    private final String replacer;
}
