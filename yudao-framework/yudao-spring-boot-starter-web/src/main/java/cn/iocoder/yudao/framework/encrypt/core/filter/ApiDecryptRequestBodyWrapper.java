package cn.iocoder.yudao.framework.encrypt.core.filter;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.encrypt.config.EncryptProperties;
import cn.iocoder.yudao.framework.encrypt.core.util.EncryptUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

/**
 * 请求参数解密 Wrapper
 *
 * @author Zhougang
 */
@Slf4j
public class ApiDecryptRequestBodyWrapper extends HttpServletRequestWrapper {

    /**
     * 缓存的内容
     */
    private final byte[] body;

    public ApiDecryptRequestBodyWrapper(HttpServletRequest request, EncryptProperties encryptProperties) throws Exception {
        super(request);

        byte[] body = ServletUtils.getBodyBytes(request);
        if (ArrayUtil.isEmpty(body)) {
            this.body = body;
            return;
        }
        String bodyStr = new String(body);
        // 获取客户端 AES 加密密钥
        String aesKey = request.getHeader(encryptProperties.getAesKey());
        Assert.notBlank(aesKey, () ->
                new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), StrUtil.format("请求头 {} 为空！", encryptProperties.getAesKey())));
        // 通过 RSA 私钥解密 AES 加密密钥
        String decryptAesKey = EncryptUtils.decryptStrByRSA(aesKey, encryptProperties.getPrivateKey());
        // 通过解密后的 AES 解密数据
        String decryptBody = EncryptUtils.decryptStrByAES(bodyStr, decryptAesKey);
        if (encryptProperties.isShowLog()) {
            log.info("接收到的加密数据：{}，解密后：{}", bodyStr, decryptBody);
        }
        this.body = decryptBody.getBytes(encryptProperties.getCharset());
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        // 返回 ServletInputStream
        return new ServletInputStream() {

            @Override
            public int read() {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int available() {
                return body.length;
            }

        };
    }

}
