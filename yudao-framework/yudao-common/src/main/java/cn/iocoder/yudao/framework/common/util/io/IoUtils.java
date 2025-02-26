package cn.iocoder.yudao.framework.common.util.io;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * IO 工具类，用于 {@link IoUtil} 缺失的方法
 *
 * @author 芋道源码
 */
public class IoUtils {



    /**
     * 从流中读取 UTF8 编码的内容
     *
     * @param inputStream 输入流
     * @param isClose 读取完后是否关闭流
     * @return 内容
     * @throws IORuntimeException IO 异常
     */
    public static String readUtf8(InputStream inputStream, boolean isClose) throws IOException {
        var result = StrUtil.utf8Str(IoUtil.read(inputStream, isClose));

//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        IOUtils.copy(inputStream, byteArrayOutputStream);
//        String result = byteArrayOutputStream.toString();

//        byte[] fileBytes = inputStream.readAllBytes();
//        var result = new String(fileBytes, StandardCharsets.UTF_8); // Convert bytes to String
//
//        if (isClose) {
//            inputStream.close();
//        }

        return result;
    }

    /**
     * 创建一个 Reader 对象，使用 UTF-8 编码。
     *
     * @param inputStream 输入流
     * @return Reader 对象
     */
    public static Reader createUtf8Reader(InputStream inputStream) {
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

}
