package cn.iocoder.yudao.framework.file.core.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.SneakyThrows;
import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;

/**
 * 文件类型 Utils
 *
 * @author 芋道源码
 */
public class FileTypeUtils {

    private static final ThreadLocal<Tika> TIKA = TransmittableThreadLocal.withInitial(Tika::new);

    /**
     * 获得文件的 mineType
     *
     * @param data 文件内容
     * @return mineType
     */
    @SneakyThrows
    public static String getMineType(byte[] data) {
        return TIKA.get().detect(new ByteArrayInputStream(data));
    }

}
