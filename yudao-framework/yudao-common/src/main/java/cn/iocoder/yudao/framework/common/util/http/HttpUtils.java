package cn.iocoder.yudao.framework.common.util.http;

import cn.hutool.core.map.TableMap;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ReflectUtil;

import java.nio.charset.Charset;

/**
 * HTTP 工具类
 *
 * @author 芋道源码
 */
public class HttpUtils {

    @SuppressWarnings("unchecked")
    public static String replaceUrlQuery(String url, String key, String value) {
        UrlBuilder builder = UrlBuilder.of(url, Charset.defaultCharset());
        // 先移除
        TableMap<CharSequence, CharSequence> query = (TableMap<CharSequence, CharSequence>)
                ReflectUtil.getFieldValue(builder.getQuery(), "query");
        query.remove(key);
        // 后添加
        builder.addQuery(key, value);
        return builder.build();
    }

}
