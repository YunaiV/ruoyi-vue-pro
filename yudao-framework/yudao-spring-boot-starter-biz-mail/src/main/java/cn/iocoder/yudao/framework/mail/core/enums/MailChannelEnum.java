package cn.iocoder.yudao.framework.mail.core.enums;

import cn.hutool.core.util.ArrayUtil;
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
public enum MailChannelEnum {
    HUTOOL("HUTOOL" , "HUTOOL"),
    ;

    /**
     * 编码
     */
    private final String code;
    /**
     * 名字
     */
    private final String name;

    public static MailChannelEnum getByCode(String code) {
        return ArrayUtil.firstMatch(o -> o.getCode().equals(code), values());
    }

}
