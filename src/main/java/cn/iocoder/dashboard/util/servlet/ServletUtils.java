package cn.iocoder.dashboard.util.servlet;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.http.MediaType;


import javax.servlet.http.HttpServletResponse;

/**
 * 客户端工具类
 *
 * @author 芋道源码
 */
public class ServletUtils {

    @SuppressWarnings("deprecation") // 必须使用 APPLICATION_JSON_UTF8_VALUE，否则会乱码
    public static void writeJSON(HttpServletResponse response, Object object) {
        String content = JSON.toJSONString(object);
        ServletUtil.write(response, content, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

}
