package cn.iocoder.yudao.module.infra.framework.file.core.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import org.apache.tika.Tika;
import org.apache.tika.detect.CompositeDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
    public void testGetMineType_streamKeepsPrefix() throws Exception {
        // 准备参数：图片后追加超过探测上限的内容
        byte[] image = ResourceUtil.readBytes("file/erweima.jpg");
        byte[] content = Arrays.copyOf(image, image.length + 1024 * 1024);
        InputStream input = new BufferedInputStream(new ByteArrayInputStream(content));

        // 调用
        String type = FileTypeUtils.getMineType(input, "二维码.jpg");

        // 断言：按文件头部识别类型，探测后流回到起点，后续读取得到完整内容
        assertEquals("image/jpeg", type);
        assertArrayEquals(content, input.readAllBytes());
    }

    @Test
    public void testGetMineType_streamWithoutMarkRejected() {
        // 不支持 mark / reset 的流会在探测时吞掉头部，直接拒绝
        InputStream input = new InputStream() {

            @Override
            public int read() {
                return -1;
            }

        };

        assertThrows(IllegalArgumentException.class, () -> FileTypeUtils.getMineType(input, "a.txt"));
    }

    @Test
    public void testGetMineType_streamResetFailedThrows() {
        // 准备参数：重置失败的流
        InputStream input = new BufferedInputStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8))) {

            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("reset failed");
            }

        };

        // 调用，并断言：直接抛出，不按文件名兜底，避免后续输出缺失头部的内容
        assertThrows(IOException.class, () -> FileTypeUtils.getMineType(input, "a.txt"));
    }

    @Test
    public void testGetMineType_streamReadFailedThrows() {
        // 准备参数：读取文件头失败的流
        InputStream input = new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException("read failed");
            }

            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public void mark(int readlimit) {
            }

            @Override
            public void reset() {
            }

        };

        // 调用，并断言：直接抛出，不按文件名兜底
        assertThrows(IOException.class, () -> FileTypeUtils.getMineType(input, "a.txt"));
    }

    @Test
    public void testDetectContent_incompatibleDetectorFallbackName() {
        // 准备参数：与 Tika 默认相同，组合全部 Detector，其中一个与 Tika 4 接口不兼容
        Tika tika = new Tika(new CompositeDetector(new IncompatibleDetector()));
        byte[] image = ResourceUtil.readBytes("file/erweima.jpg");
        assertThrows(AbstractMethodError.class, () -> tika.detect(image, "二维码.jpg"));

        // 调用，并断言：不抛出，按扩展名识别；扩展名不区分大小写
        assertEquals("image/jpeg", FileTypeUtils.detectContent(tika, image, "二维码.jpg"));
        assertEquals("application/pdf", FileTypeUtils.detectContent(tika, image, "2026/11/REPORT.PDF"));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                FileTypeUtils.detectContent(tika, image, "合同.docx"));
        // 调用，并断言：没有文件名、没有扩展名、扩展名未知时，返回 application/octet-stream
        assertEquals("application/octet-stream", FileTypeUtils.detectContent(tika, image, null));
        assertEquals("application/octet-stream", FileTypeUtils.detectContent(tika, image, ""));
        assertEquals("application/octet-stream", FileTypeUtils.detectContent(tika, image, " "));
        assertEquals("application/octet-stream", FileTypeUtils.detectContent(tika, image, "附件"));
        assertEquals("application/octet-stream", FileTypeUtils.detectContent(tika, image, "a.unknown-ext"));
    }

    @Test
    public void testGetMineType_name() {
        // 只按扩展名识别，不调用 Tika 的 Detector
        assertEquals("image/png", FileTypeUtils.getMineType("头像.PNG"));
        assertEquals("image/svg+xml", FileTypeUtils.getMineType("2026/11/图标.svg"));
        assertEquals("video/mp4", FileTypeUtils.getMineType("演示.mp4"));
        assertEquals("application/octet-stream", FileTypeUtils.getMineType("附件"));
        assertEquals("application/octet-stream", FileTypeUtils.getMineType((String) null));
        assertEquals("application/octet-stream", FileTypeUtils.getMineType(""));
    }

    @Test
    public void testWriteAttachment_streamImageInline() throws Exception {
        // 准备参数
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");

        // 调用
        FileTypeUtils.writeAttachment(response, "二维码.jpg", new ByteArrayInputStream(content));

        // 断言：普通位图 inline 展示，不加沙箱，非 ASCII 文件名编码，正文完整
        assertEquals("image/jpeg", response.getContentType());
        assertEquals("inline;filename=\"___.jpg\";filename*=UTF-8''%E4%BA%8C%E7%BB%B4%E7%A0%81.jpg",
                response.getHeader("Content-Disposition"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertNull(response.getHeader("Content-Security-Policy"));
        assertArrayEquals(content, response.getContentAsByteArray());
    }

    @Test
    public void testWriteAttachment_streamVideoWithoutFakeLength() throws Exception {
        // 准备参数：超过探测上限的视频内容
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = new byte[3 * 1024 * 1024 + 7];
        Arrays.fill(content, (byte) 1);

        // 调用
        FileTypeUtils.writeAttachment(response, "演示.mp4", new ByteArrayInputStream(content));

        // 断言：流式输出完整正文；长度未知且不支持 Range，不声明 Content-Length / Accept-Ranges
        assertEquals("video/mp4", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").startsWith("attachment;"));
        assertNull(response.getHeader("Content-Length"));
        assertNull(response.getHeader("Accept-Ranges"));
        assertArrayEquals(content, response.getContentAsByteArray());
    }

    @Test
    public void testWriteAttachment_streamScriptableAttachment() throws Exception {
        // 文件下载接口走流式输出：SVG、HTML、XML 文件同样按附件下载
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
            FileTypeUtils.writeAttachment(response, document[0], new ByteArrayInputStream(content));

            // 断言：保留原类型，直接打开时按附件下载，嵌入时按沙箱执行，正文完整
            assertEquals(document[2], response.getContentType());
            assertEquals(document[3], response.getHeader("Content-Disposition"));
            assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
            assertEquals("sandbox", response.getHeader("Content-Security-Policy"));
            assertArrayEquals(content, response.getContentAsByteArray());
        }
    }

    @Test
    public void testWriteAttachment_streamFakeImageNameDetectContent() throws Exception {
        // 准备参数：SVG 内容伪装成 png 文件名，正文超过探测上限，且文件流不支持 mark / reset
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = ("<svg xmlns=\"http://www.w3.org/2000/svg\"><desc>" + "a".repeat(512 * 1024)
                + "</desc><script>alert(1)</script></svg>").getBytes(StandardCharsets.UTF_8);
        InputStream input = new FilterInputStream(new ByteArrayInputStream(content)) {

            @Override
            public boolean markSupported() {
                return false;
            }

        };

        // 调用
        FileTypeUtils.writeAttachment(response, "图标.png", input);

        // 断言：以内容识别的 SVG 类型为准，按附件下载；两次探测都不吞掉头部，正文完整
        assertEquals("image/svg+xml", response.getContentType());
        assertEquals("attachment;filename=\"__.png\";filename*=UTF-8''%E5%9B%BE%E6%A0%87.png",
                response.getHeader("Content-Disposition"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("sandbox", response.getHeader("Content-Security-Policy"));
        assertArrayEquals(content, response.getContentAsByteArray());
    }

    @Test
    public void testWriteAttachment_streamImageNameHintDetectContent() throws Exception {
        // 准备参数：XML 内容，文件名提示会把类型识别为图片（image/vnd.adobe.premiere）
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = "<?xml version=\"1.0\"?><root>test</root>".getBytes(StandardCharsets.UTF_8);

        // 调用
        FileTypeUtils.writeAttachment(response, "test.ppj", new ByteArrayInputStream(content));

        // 断言：仅按内容识别为 XML 时以内容为准，不按图片 inline
        assertEquals("application/xml", response.getContentType());
        assertEquals("attachment;filename=\"test.ppj\";filename*=UTF-8''test.ppj",
                response.getHeader("Content-Disposition"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("sandbox", response.getHeader("Content-Security-Policy"));
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

    /**
     * 模拟旧的 {@code org.gagravarr.tika.OggDetector}：按 Tika 2.x 接口编译，只实现了 detect(InputStream, Metadata)，
     * Tika 4 调用 detect(TikaInputStream, Metadata, ParseContext) 时，JVM 抛出 AbstractMethodError
     */
    private static class IncompatibleDetector implements Detector {

        @Override
        public MediaType detect(TikaInputStream input, Metadata metadata, ParseContext parseContext) {
            throw new AbstractMethodError("Receiver class org.gagravarr.tika.OggDetector does not define or inherit "
                    + "an implementation of the resolved method 'abstract org.apache.tika.mime.MediaType "
                    + "detect(org.apache.tika.io.TikaInputStream, org.apache.tika.metadata.Metadata, "
                    + "org.apache.tika.parser.ParseContext)' of interface org.apache.tika.detect.Detector.");
        }

    }

}
