package cn.iocoder.yudao.module.ai.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiDocumentUtils} 的单元测试类
 *
 * 生成：用 JDK 生成真实的图片，再用 POI、PDFBox 读回导出的文件，不启动 Spring 容器
 *
 * @author 芋道源码
 */
public class AiDocumentUtilsTest {

    @Test
    public void testBuildPptx() throws IOException {
        // 准备参数：16:9 的 PNG、1:1 的 JPEG、GIF（转为 PNG）
        List<byte[]> images = Arrays.asList(buildImage(1600, 900, "png"),
                buildImage(1024, 1024, "jpg"), buildImage(400, 300, "gif"));

        // 调用
        byte[] content = AiDocumentUtils.buildPptx(images);
        // 断言：16:9 页面，每页一张图，没有其它元素
        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(content))) {
            assertEquals(960, ppt.getPageSize().width);
            assertEquals(540, ppt.getPageSize().height);
            assertEquals(3, ppt.getSlides().size());
            for (XSLFSlide slide : ppt.getSlides()) {
                assertEquals(1, slide.getShapes().size());
                assertInstanceOf(XSLFPictureShape.class, slide.getShapes().get(0));
            }
            // 断言：16:9 的图铺满
            assertAnchor(0, 0, 960, 540, ppt.getSlides().get(0).getShapes().get(0));
            // 断言：1:1 的图等比居中，左右留白
            assertAnchor(210, 0, 540, 540, ppt.getSlides().get(1).getShapes().get(0));
        }
    }

    @Test
    public void testBuildPdf() throws IOException {
        // 准备参数
        List<byte[]> images = Arrays.asList(buildImage(1600, 900, "png"), buildImage(1024, 1024, "jpg"));

        // 调用
        byte[] content = AiDocumentUtils.buildPdf(images);
        // 断言：每页 16:9，只有一张图
        try (PDDocument document = Loader.loadPDF(content)) {
            assertEquals(2, document.getNumberOfPages());
            for (PDPage page : document.getPages()) {
                assertEquals(960F, page.getMediaBox().getWidth());
                assertEquals(540F, page.getMediaBox().getHeight());
                int imageCount = 0;
                for (COSName ignored : page.getResources().getXObjectNames()) {
                    imageCount++;
                }
                assertEquals(1, imageCount);
            }
        }
    }

    @Test
    public void testBuildPdf_imageNotSupported() {
        // 准备参数：不是 JDK 可读的图片（例如 WEBP）
        List<byte[]> images = Collections.singletonList(new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'});

        // 调用，并断言异常
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AiDocumentUtils.buildPdf(images));
        assertEquals("第 1 页图片格式不支持（仅支持 PNG、JPEG、GIF、BMP）", ex.getMessage());
    }

    @Test
    public void testFitPage() {
        // 横版更宽：上下留白
        Rectangle2D wide = AiDocumentUtils.fitPage(2000, 500);
        assertEquals(0, wide.getX(), 0.001);
        assertEquals(150, wide.getY(), 0.001);
        assertEquals(960, wide.getWidth(), 0.001);
        assertEquals(240, wide.getHeight(), 0.001);
        // 竖版：左右留白
        Rectangle2D tall = AiDocumentUtils.fitPage(900, 1600);
        assertEquals(540 * 900 / 1600D, tall.getWidth(), 0.001);
        assertEquals(540, tall.getHeight(), 0.001);
        assertEquals((960 - tall.getWidth()) / 2, tall.getX(), 0.001);
        assertEquals(0, tall.getY(), 0.001);
    }

    private static void assertAnchor(double x, double y, double width, double height, XSLFShape shape) {
        Rectangle2D anchor = shape.getAnchor();
        assertEquals(x, anchor.getX(), 0.5);
        assertEquals(y, anchor.getY(), 0.5);
        assertEquals(width, anchor.getWidth(), 0.5);
        assertEquals(height, anchor.getHeight(), 0.5);
    }

    private static byte[] buildImage(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }

}
