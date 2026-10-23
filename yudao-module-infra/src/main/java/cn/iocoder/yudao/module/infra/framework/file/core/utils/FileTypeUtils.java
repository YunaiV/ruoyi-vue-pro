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
        String mineType = getMineType(content, filename);
        // 文件名可能伪装，按内容识别为 SVG、HTML、XML 等可携带脚本的类型时，以内容为准
        String contentMineType = getMineType(content);
        if (!isScriptable(mineType) && isScriptable(contentMineType)) {
            mineType = contentMineType;
        }
        response.setContentType(mineType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        // 设置内容显示、下载文件名：https://www.cnblogs.com/wq-9/articles/12165056.html
        if (isImage(mineType) && !isScriptable(mineType)) {
            // 参见 https://github.com/YunaiV/ruoyi-vue-pro/issues/692 讨论
            response.setHeader("Content-Disposition", buildContentDisposition("inline", filename));
        } else {
            // SVG 等可携带脚本的文件：直接打开时按附件下载；保留原类型，页面中作为图片引用时仍可显示
            response.setHeader("Content-Disposition", buildContentDisposition("attachment", filename));
            // 被 object、embed 等嵌入时，按沙箱执行，禁止脚本、隔离来源
            if (isScriptable(mineType)) {
                response.setHeader("Content-Security-Policy", "sandbox");
            }
        }
        // 针对 video 的特殊处理，解决视频地址在移动端播放的兼容性问题
        if (StrUtil.containsIgnoreCase(mineType, "video")) {
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", String.valueOf(content.length));
        }
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, content);
    }

    private static String buildContentDisposition(String dispositionType, String filename) {
        return StrUtil.format("{};filename=\"{}\";filename*=UTF-8''{}",
                dispositionType, buildFallbackFilename(filename), HttpUtils.encodeUrlPathSegment(filename));
    }

    private static String buildFallbackFilename(String filename) {
        if (StrUtil.isEmpty(filename)) {
            return "download";
        }
        StringBuilder result = new StringBuilder(filename.length());
        for (int i = 0; i < filename.length(); i++) {
            char ch = filename.charAt(i);
            if (ch == '"' || ch == '\\') {
                result.append('\\').append(ch);
            } else if (ch >= 0x20 && ch <= 0x7E) {
                result.append(ch);
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }

    /**
     * 判断是否是图片
     *
     * @param mineType 类型
     * @return 是否是图片
     */
    public static boolean isImage(String mineType) {
        return StrUtil.startWith(mineType, "image/");
    }

    /**
     * 判断是否是可携带脚本的类型，例如说 SVG、HTML、XHTML、XML，同源直接打开时会执行其中的脚本
     *
     * 注意：Office 的 openxmlformats 类型以 document、sheet 等结尾，不属于此类
     *
     * @param mineType 类型
     * @return 是否可携带脚本
     */
    public static boolean isScriptable(String mineType) {
        return StrUtil.containsAnyIgnoreCase(mineType, "svg", "html") || StrUtil.endWithIgnoreCase(mineType, "xml");
    }

}
