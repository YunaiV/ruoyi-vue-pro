package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileMagicNumber;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 文档的工具类
 *
 * 1. 读取：读取文件的文本，供知识库、对话附件、PPT 材料等使用
 * 2. 生成：每页一张整页图，拼接 PPTX、PDF，供 PPT 导出使用。没有文本框、备注；PDF 没有文本层；
 *    页面统一为 16:9（960x540 pt），图片等比缩放、居中，不裁剪
 *
 * @author 芋道源码
 */
@Slf4j
public class AiDocumentUtils {

    /**
     * 页面宽度（pt）
     */
    private static final int PAGE_WIDTH = 960;
    /**
     * 页面高度（pt）
     */
    private static final int PAGE_HEIGHT = 540;

    // ========== 读取 ==========

    /**
     * 读取 URL 内容：下载文件后，使用 Tika 读取文本
     *
     * @param url URL
     * @return 内容
     */
    public static String readUrl(String url) {
        // 下载文件
        ByteArrayResource resource;
        try {
            byte[] bytes = HttpUtil.downloadBytes(url);
            if (bytes.length == 0) {
                throw exception(KNOWLEDGE_DOCUMENT_FILE_EMPTY);
            }
            resource = new ByteArrayResource(bytes);
        } catch (Exception e) {
            log.error("[readUrl][url({}) 读取失败]", url, e);
            throw exception(KNOWLEDGE_DOCUMENT_FILE_DOWNLOAD_FAIL);
        }

        // 读取文件
        TikaDocumentReader loader = new TikaDocumentReader(resource);
        List<Document> documents = loader.get();
        Document document = CollUtil.getFirst(documents);
        if (document == null || StrUtil.isEmpty(document.getText())) {
            throw exception(KNOWLEDGE_DOCUMENT_FILE_READ_FAIL);
        }
        return document.getText();
    }

    // ========== 生成 ==========

    /**
     * 拼接 PPTX：每页一张图
     *
     * @param images 每页的图片，按页码排序
     * @return PPTX 文件内容
     */
    public static byte[] buildPptx(List<byte[]> images) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 1. 页面尺寸：16:9
            ppt.setPageSize(new Dimension(PAGE_WIDTH, PAGE_HEIGHT));
            // 2. 逐页添加：每页一张图，等比缩放、居中
            for (int i = 0; i < images.size(); i++) {
                byte[] image = normalizeImage(images.get(i), i + 1);
                XSLFPictureData pictureData = ppt.addPicture(image, FileMagicNumber.JPEG.match(image)
                        ? PictureData.PictureType.JPEG : PictureData.PictureType.PNG);
                XSLFPictureShape picture = ppt.createSlide().createPicture(pictureData);
                Dimension size = pictureData.getImageDimension();
                picture.setAnchor(fitPage(size.getWidth(), size.getHeight()));
            }
            // 3. 输出
            ppt.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 拼接 PDF：每页一张图
     *
     * @param images 每页的图片，按页码排序
     * @return PDF 文件内容
     */
    public static byte[] buildPdf(List<byte[]> images) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 1. 逐页添加：页面 16:9，每页一张图，等比缩放、居中
            for (int i = 0; i < images.size(); i++) {
                PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
                document.addPage(page);
                PDImageXObject image = PDImageXObject.createFromByteArray(document,
                        normalizeImage(images.get(i), i + 1), "slide-" + (i + 1));
                Rectangle2D anchor = fitPage(image.getWidth(), image.getHeight());
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.drawImage(image, (float) anchor.getX(), (float) anchor.getY(),
                            (float) anchor.getWidth(), (float) anchor.getHeight());
                }
            }
            // 2. 输出
            document.save(out);
            return out.toByteArray();
        }
    }

    /**
     * 计算图片在页面中的位置：等比缩放到页面内，居中
     *
     * @param width 图片宽度
     * @param height 图片高度
     * @return 位置
     */
    public static Rectangle2D fitPage(double width, double height) {
        double scale = Math.min(PAGE_WIDTH / width, PAGE_HEIGHT / height);
        double fitWidth = width * scale;
        double fitHeight = height * scale;
        return new Rectangle2D.Double((PAGE_WIDTH - fitWidth) / 2, (PAGE_HEIGHT - fitHeight) / 2, fitWidth, fitHeight);
    }

    /**
     * 统一图片格式：PNG、JPEG 原样返回；其它 JDK 可读的格式（GIF、BMP 等）转为 PNG
     *
     * @param image 图片
     * @param index 页码，用于错误提示
     * @return PNG 或 JPEG 图片
     */
    private static byte[] normalizeImage(byte[] image, int index) throws IOException {
        // 1. PNG、JPEG：原样返回
        if (FileMagicNumber.PNG.match(image) || FileMagicNumber.JPEG.match(image)) {
            return image;
        }
        // 2. 其它格式：JDK 可读的转为 PNG，不可读的提示不支持
        BufferedImage bufferedImage = ImageIO.read(IoUtil.toStream(image));
        if (bufferedImage == null) {
            throw new IllegalArgumentException(StrUtil.format("第 {} 页图片格式不支持（仅支持 PNG、JPEG、GIF、BMP）", index));
        }
        return ImgUtil.toBytes(bufferedImage, ImgUtil.IMAGE_TYPE_PNG);
    }

}
