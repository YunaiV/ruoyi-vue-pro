package cn.iocoder.yudao.module.infra.framework.file.core.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.io.LookaheadInputStream;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件类型 Utils
 *
 * @author 芋道源码
 */
@Slf4j
public class FileTypeUtils {

    private static final Tika TIKA = new Tika();

    /**
     * 通过文件流探测类型时，最多读取的文件头部字节数
     *
     * 目的：探测只缓冲有限的头部，避免大文件整体读入内存
     */
    private static final int DETECT_LIMIT = 256 * 1024;

    /**
     * 获得文件的 mineType，对于 doc，jar 等文件会有误差
     *
     * @param data 文件内容
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(byte[] data) {
        return detectContent(data, null);
    }

    /**
     * 已知文件名，获取文件类型，在某些情况下比通过字节数组准确，例如使用 jar 文件时，通过名字更为准确
     *
     * @param name 文件名
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(String name) {
        return detectByName(name);
    }

    /**
     * 在拥有文件和数据的情况下，最好使用此方法，最为准确
     *
     * @param data 文件内容
     * @param name 文件名
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(byte[] data, String name) {
        return detectContent(data, name);
    }

    /**
     * 通过文件流和文件名获得 mineType，只读取文件头部，读取后重置到原位置，不会吞掉已读取的内容
     *
     * @param data 文件流，必须支持 mark / reset
     * @param name 文件名
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(InputStream data, String name) {
        Assert.isTrue(data.markSupported(), "文件流必须支持 mark / reset");
        // 关闭 LookaheadInputStream 时，会将 data 重置到读取前的位置
        try (InputStream head = new LookaheadInputStream(data, DETECT_LIMIT)) {
            return detectContent(IoUtil.readBytes(head, DETECT_LIMIT), name);
        } catch (IOException ex) {
            log.warn("[getMineType][读取文件头失败，改用文件名({})]", name, ex);
            return detectByName(name);
        }
    }

    /**
     * 内容识别失败时退回文件名。
     * Tika 4 的 Detector 接口与 classpath 上的 {@code org.gagravarr.tika.OggDetector} 不兼容时，
     * 会抛出 AbstractMethodError，把正常文件下载打成 500。
     */
    private static String detectContent(byte[] data, String name) {
        try {
            return name == null ? TIKA.detect(data) : TIKA.detect(data, name);
        } catch (Throwable ex) {
            log.warn("[getMineType][内容识别失败，改用文件名({})]", name, ex);
            return detectByName(name);
        }
    }

    /**
     * 只按扩展名识别。不能再调用 {@link Tika#detect(String)}，那条路径仍会加载全部 SPI Detector。
     */
    private static String detectByName(String name) {
        if (StrUtil.isBlank(name)) {
            return "application/octet-stream";
        }
        try {
            return MimeTypes.getDefaultMimeTypes().getMimeType(name).getName();
        } catch (Throwable ex) {
            log.warn("[getMineType][按文件名识别失败({})]", name, ex);
            return "application/octet-stream";
        }
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

    /**
     * 返回附件，边读边写，不把整个文件读入内存
     *
     * 注意：流式输出不设置 Content-Length 和 Accept-Ranges：真实长度未知时不能凭空声明，且不支持 Range 分段请求，
     * 带 Range 的请求仍返回完整内容
     *
     * @param response 响应
     * @param filename 文件名
     * @param content  附件内容，由调用方关闭
     */
    public static void writeAttachment(HttpServletResponse response, String filename, InputStream content) throws IOException {
        // 设置 header 和 contentType：按文件头部和文件名探测类型
        InputStream input = content.markSupported() ? content : new BufferedInputStream(content);
        String mineType = getMineType(input, filename);
        // 文件名可能伪装，按内容识别为 SVG、HTML、XML 等可携带脚本的类型时，以内容为准
        String contentMineType = getMineType(input, null);
        if (!isScriptable(mineType) && isScriptable(contentMineType)) {
            mineType = contentMineType;
        }
        response.setContentType(mineType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        // 设置内容显示、下载文件名：普通图片 inline，其它 attachment
        if (isImage(mineType) && !isScriptable(mineType)) {
            response.setHeader("Content-Disposition", buildContentDisposition("inline", filename));
        } else {
            // SVG 等可携带脚本的文件：直接打开时按附件下载；保留原类型，页面中作为图片引用时仍可显示
            response.setHeader("Content-Disposition", buildContentDisposition("attachment", filename));
            // 被 object、embed 等嵌入时，按沙箱执行，禁止脚本、隔离来源
            if (isScriptable(mineType)) {
                response.setHeader("Content-Security-Policy", "sandbox");
            }
        }
        // 输出附件
        IoUtil.copy(input, response.getOutputStream());
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
