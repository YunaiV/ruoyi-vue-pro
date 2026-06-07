package cn.iocoder.yudao.framework.common.util.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link HttpUtils} 的单元测试
 */
public class HttpUtilsTest {

    @Test
    public void testEncodeUrlPath() {
        // 准备参数
        String path = "avatar/中文 100%+文件.jpg";

        // 调用
        String result = HttpUtils.encodeUrlPath(path);

        // 断言
        assertEquals("avatar/%E4%B8%AD%E6%96%87%20100%25+%E6%96%87%E4%BB%B6.jpg", result);
    }

    @Test
    public void testDecodeUrlPath() {
        // 准备参数：+ 是路径字符，不应该按 query parameter 语义解码为空格
        String path = "avatar/%E4%B8%AD%E6%96%87%20100%25+%E6%96%87%E4%BB%B6.jpg";

        // 调用
        String result = HttpUtils.decodeUrlPath(path);

        // 断言
        assertEquals("avatar/中文 100%+文件.jpg", result);
    }

    @Test
    public void testRemoveUrlPathQueryAndFragment() {
        assertEquals("avatar/test.jpg", HttpUtils.removeUrlPathQueryAndFragment("avatar/test.jpg?token=1#preview"));
        assertEquals("avatar/test.jpg", HttpUtils.removeUrlPathQueryAndFragment("avatar/test.jpg#preview?token=1"));
    }

    @Test
    public void testReplaceUrlQuery_replace() {
        // 准备参数
        String url = "https://www.iocoder.cn/path?a=1&b=2";
        // 调用
        String result = HttpUtils.replaceUrlQuery(url, "a", "3");
        // 断言：被替换的 key 会移到末尾，原顺序的其它参数保留
        assertEquals("https://www.iocoder.cn/path?b=2&a=3", result);
    }

    @Test
    public void testReplaceUrlQuery_add() {
        // 准备参数
        String url = "https://www.iocoder.cn/path?a=1";
        // 调用
        String result = HttpUtils.replaceUrlQuery(url, "b", "2");
        // 断言
        assertEquals("https://www.iocoder.cn/path?a=1&b=2", result);
    }

    @Test
    public void testReplaceUrlQuery_noQuery() {
        // 准备参数：原 URL 没有 query
        String url = "https://www.iocoder.cn/path";
        // 调用
        String result = HttpUtils.replaceUrlQuery(url, "a", "1");
        // 断言
        assertEquals("https://www.iocoder.cn/path?a=1", result);
    }

    @Test
    public void testReplaceUrlQuery_emptyValue() {
        // 准备参数：value 为空字符串
        String url = "https://www.iocoder.cn/path?a=1";
        // 调用
        String result = HttpUtils.replaceUrlQuery(url, "a", "");
        // 断言：保留 key，value 为空
        assertEquals("https://www.iocoder.cn/path?a=", result);
    }

}
