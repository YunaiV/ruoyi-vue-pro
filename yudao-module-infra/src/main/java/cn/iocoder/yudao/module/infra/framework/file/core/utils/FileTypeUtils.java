package cn.iocoder.yudao.module.infra.framework.file.core.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.IOException;

/**
 * 文件类型 Utils
 *
 * @author 芋道源码
 */
@Slf4j
public class FileTypeUtils {

    private static final Tika TIKA = new Tika();

    /**
     * 获得文件的 mineType，对于 doc，jar 等文件会有误差
     *
     * @param data 文件内容
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    @SneakyThrows
    public static String getMineType(byte[] data) {
        return TIKA.detect(data);
    }

    /**
     * 已知文件名，获取文件类型，在某些情况下比通过字节数组准确，例如使用 jar 文件时，通过名字更为准确
     *
     * @param name 文件名
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(String name) {
        return TIKA.detect(name);
    }

    /**
     * 在拥有文件和数据的情况下，最好使用此方法，最为准确
     *
     * @param data 文件内容
     * @param name 文件名
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(byte[] data, String name) {
        return TIKA.detect(data, name);
    }

    /**
     * 根据 mineType 获得文件后缀
     *
     * 注意：如果获取不到，或者发生异常，都返回 null
     *
     * @param mineType 类型
     * @return 后缀，例如说 .pdf
     */
    public static String getExtension(String mineType) {
        try {
            return MimeTypes.getDefaultMimeTypes().forName(mineType).getExtension();
        } catch (MimeTypeException e) {
            log.warn("[getExtension][获取文件后缀({}) 失败]", mineType, e);
            return null;
        }
    }

    /**
     * 返回附件
     *
     * @param response 响应
     * @param filename 文件名
     * @param content  附件内容
     */
    public static void writeAttachment(HttpServletResponse response, String filename, byte[] content) throws IOException {
        // 设置 header 和 contentType
        response.setHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(filename));
        String contentType = getMineType(content, filename);
        response.setContentType(contentType);
        // 针对 video 的特殊处理，解决视频地址在移动端播放的兼容性问题
        if (StrUtil.containsIgnoreCase(contentType, "video")) {
            response.setHeader("Content-Length", String.valueOf(content.length));
            response.setHeader("Content-Range", "bytes 0-" + (content.length - 1) + "/" + content.length);
            response.setHeader("Accept-Ranges", "bytes");
        }
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, content);
    }

}
