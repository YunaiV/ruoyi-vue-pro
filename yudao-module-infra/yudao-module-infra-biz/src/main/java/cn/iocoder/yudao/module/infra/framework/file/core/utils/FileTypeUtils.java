package cn.iocoder.yudao.module.infra.framework.file.core.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.SneakyThrows;
import org.apache.tika.Tika;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 文件类型 Utils
 *
 * @author 芋道源码
 */
public class FileTypeUtils {

    private static final ThreadLocal<Tika> TIKA = TransmittableThreadLocal.withInitial(Tika::new);

    /**
     * 获得文件的 mineType，对于doc，jar等文件会有误差
     *
     * @param data 文件内容
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    @SneakyThrows
    public static String getMineType(byte[] data) {
        return TIKA.get().detect(data);
    }

    /**
     * 已知文件名，获取文件类型，在某些情况下比通过字节数组准确，例如使用jar文件时，通过名字更为准确
     *
     * @param name 文件名
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(String name) {
        return TIKA.get().detect(name);
    }

    /**
     * 在拥有文件和数据的情况下，最好使用此方法，最为准确
     *
     * @param data 文件内容
     * @param name 文件名
     * @return mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(byte[] data, String name) {
        return TIKA.get().detect(data, name);
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
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        String contentType = getMineType(content, filename);
        response.setContentType(contentType);
        // 针对 video 的特殊处理，解决视频地址在移动端播放的兼容性问题
        if (StrUtil.containsIgnoreCase(contentType, "video")) {
            response.setHeader("Content-Length", String.valueOf(content.length - 1));
            response.setHeader("Content-Range", String.valueOf(content.length - 1));
            response.setHeader("Accept-Ranges", "bytes");
        }
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, content);
    }

}
