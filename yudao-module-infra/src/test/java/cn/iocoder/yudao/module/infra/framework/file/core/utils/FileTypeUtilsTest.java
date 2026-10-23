package cn.iocoder.yudao.module.infra.framework.file.core.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertNull(response.getHeader("Content-Security-Policy"));
        assertArrayEquals(content, response.getContentAsByteArray());
    }

    @Test
    public void testWriteAttachment_imageInline() throws Exception {
        // 准备参数
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");

        // 调用
        FileTypeUtils.writeAttachment(response, "头像.jpg", content);

        // 断言：普通位图保持 inline，可直接预览
        assertEquals("image/jpeg", response.getContentType());
        assertEquals("inline;filename=\"__.jpg\";filename*=UTF-8''%E5%A4%B4%E5%83%8F.jpg",
                response.getHeader("Content-Disposition"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertNull(response.getHeader("Content-Security-Policy"));
        assertArrayEquals(content, response.getContentAsByteArray());
    }

    @Test
    public void testWriteAttachment_scriptableAttachment() throws Exception {
        // 历史已上传的 SVG、HTML、XML 文件，没有文件记录时按路径中的文件名下载
        for (String[] document : new String[][]{
                {"图标.svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>",
                        "image/svg+xml", "attachment;filename=\"__.svg\";filename*=UTF-8''%E5%9B%BE%E6%A0%87.svg"},
                {"页面.html", "<html><script>alert(1)</script></html>",
                        "text/html", "attachment;filename=\"__.html\";filename*=UTF-8''%E9%A1%B5%E9%9D%A2.html"},
                {"数据.xml", "<?xml version=\"1.0\"?><root>test</root>",
                        "application/xml", "attachment;filename=\"__.xml\";filename*=UTF-8''%E6%95%B0%E6%8D%AE.xml"}}) {
            // 准备参数
            MockHttpServletResponse response = new MockHttpServletResponse();
            byte[] content = document[1].getBytes(StandardCharsets.UTF_8);

            // 调用
            FileTypeUtils.writeAttachment(response, document[0], content);

            // 断言：保留原类型，直接打开时按附件下载，嵌入时按沙箱执行
            assertEquals(document[2], response.getContentType());
            assertEquals(document[3], response.getHeader("Content-Disposition"));
            assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
            assertEquals("sandbox", response.getHeader("Content-Security-Policy"));
            assertArrayEquals(content, response.getContentAsByteArray());
        }
    }

    @Test
    public void testWriteAttachment_fakeImageNameDetectContent() throws Exception {
        // 准备参数：SVG 内容伪装成 png 文件名
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);

        // 调用
        FileTypeUtils.writeAttachment(response, "图标.png", content);

        // 断言：以内容识别的 SVG 类型为准，按附件下载
        assertEquals("image/svg+xml", response.getContentType());
        assertEquals("attachment;filename=\"__.png\";filename*=UTF-8''%E5%9B%BE%E6%A0%87.png",
                response.getHeader("Content-Disposition"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("sandbox", response.getHeader("Content-Security-Policy"));
    }

    @Test
    public void testWriteAttachment_imageNameHintDetectContent() throws Exception {
        // 准备参数：XML 内容，文件名提示会把类型识别为图片（image/vnd.adobe.premiere）
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = "<?xml version=\"1.0\"?><root>test</root>".getBytes(StandardCharsets.UTF_8);

        // 调用
        FileTypeUtils.writeAttachment(response, "test.ppj", content);

        // 断言：仅按内容识别为 XML 时以内容为准，不按图片 inline
        assertEquals("application/xml", response.getContentType());
        assertEquals("attachment;filename=\"test.ppj\";filename*=UTF-8''test.ppj",
                response.getHeader("Content-Disposition"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("sandbox", response.getHeader("Content-Security-Policy"));
        assertArrayEquals(content, response.getContentAsByteArray());
    }

    @Test
    public void testIsScriptable() {
        assertTrue(FileTypeUtils.isScriptable("image/svg+xml"));
        assertTrue(FileTypeUtils.isScriptable("text/html"));
        assertTrue(FileTypeUtils.isScriptable("application/xhtml+xml"));
        assertTrue(FileTypeUtils.isScriptable("application/xml"));
        assertTrue(FileTypeUtils.isScriptable("text/xml"));
        assertFalse(FileTypeUtils.isScriptable("image/png"));
        assertFalse(FileTypeUtils.isScriptable("application/pdf"));
        assertFalse(FileTypeUtils.isScriptable("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertFalse(FileTypeUtils.isScriptable(null));
    }

}
