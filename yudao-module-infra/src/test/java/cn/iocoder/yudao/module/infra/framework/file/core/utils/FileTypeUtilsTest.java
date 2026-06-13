package cn.iocoder.yudao.module.infra.framework.file.core.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link FileTypeUtils} 的单元测试
 */
public class FileTypeUtilsTest {

    @Test
    public void testWriteAttachment_contentDispositionEncodeFilename() throws Exception {
        // 准备参数
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);

        // 调用
        FileTypeUtils.writeAttachment(response, "中文 100%+文件.txt", content);

        // 断言
        assertEquals("attachment;filename=\"__ 100%+__.txt\";"
                + "filename*=UTF-8''%E4%B8%AD%E6%96%87%20100%25+%E6%96%87%E4%BB%B6.txt",
                response.getHeader("Content-Disposition"));
        assertArrayEquals(content, response.getContentAsByteArray());
    }

}
