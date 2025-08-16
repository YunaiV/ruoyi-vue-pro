package cn.iocoder.yudao.framework.encrypt.core.filter;

import cn.hutool.crypto.asymmetric.AsymmetricEncryptor;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.symmetric.SymmetricEncryptor;
import cn.iocoder.yudao.framework.encrypt.config.ApiEncryptProperties;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 加密响应 {@link HttpServletResponseWrapper} 实现类
 *
 * @author 芋道源码
 */
public class ApiEncryptResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final ServletOutputStream servletOutputStream;
    private final PrintWriter printWriter;

    public ApiEncryptResponseWrapper(HttpServletResponse response) {
        super(response);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.servletOutputStream = this.getOutputStream();
        this.printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream));
    }

    public void encrypt(ApiEncryptProperties properties,
                        SymmetricEncryptor symmetricEncryptor,
                        AsymmetricEncryptor asymmetricEncryptor) throws IOException {
        // 1.1 清空 body
        HttpServletResponse response = (HttpServletResponse) this.getResponse();
        response.resetBuffer();
        // 1.2 获取 body
        this.flushBuffer();
        byte[] body = byteArrayOutputStream.toByteArray();

        // 2. 加密 body
        String encryptedBody = symmetricEncryptor != null ? symmetricEncryptor.encryptBase64(body)
                : asymmetricEncryptor.encryptBase64(body, KeyType.PublicKey);
        response.getWriter().write(encryptedBody);

        // 3. 添加加密 header 标识
        this.addHeader(properties.getHeader(), "true");
        // 特殊：特殊：https://juejin.cn/post/6867327674675625992
        this.addHeader("Access-Control-Expose-Headers", properties.getHeader());
    }

    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        if (printWriter != null) {
            printWriter.flush();
        }
    }

    @Override
    public void reset() {
        byteArrayOutputStream.reset();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) {
                byteArrayOutputStream.write(b);
            }

            @Override
            @SuppressWarnings("NullableProblems")
            public void write(byte[] b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            @SuppressWarnings("NullableProblems")
            public void write(byte[] b, int off, int len) {
                byteArrayOutputStream.write(b, off, len);
            }

        };
    }

}