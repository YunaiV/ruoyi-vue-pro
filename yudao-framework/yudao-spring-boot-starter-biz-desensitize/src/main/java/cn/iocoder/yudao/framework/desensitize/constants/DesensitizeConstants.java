package cn.iocoder.yudao.framework.desensitize.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DesensitizeConstants {

    /**
     * 默认正则
     */
    public static final String DEFAULT_REGEX = null;

    /**
     * 默认保持长度
     */
    public static final int DEFAULT_KEEP_LENGTH = -1;

    /**
     * 默认替换字符
     */
    public static final String DEFAULT_REPLACER = "****";

}
