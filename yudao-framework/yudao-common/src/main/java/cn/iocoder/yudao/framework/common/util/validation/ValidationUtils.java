package cn.iocoder.yudao.framework.common.util.validation;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 校验工具类
 *
 * @author 芋道源码
 */
public class ValidationUtils {

    private static Pattern PATTERN_URL = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    public static boolean isMobile(String mobile) {
        if (StrUtil.length(mobile) != 11) {
            return false;
        }
        // TODO 芋艿，后面完善手机校验
        return true;
    }

    public static boolean isURL(String url) {
        return StringUtils.hasText(url)
                && PATTERN_URL.matcher(url).matches();
    }

}
