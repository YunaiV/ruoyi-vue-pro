package cn.iocoder.yudao.framework.encrypt.core.filter;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.encrypt.config.EncryptProperties;
import cn.iocoder.yudao.framework.encrypt.core.annotation.ApiDecrypt;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

/**
 * 1、客户端传输重要信息给服务端，服务端返回的信息不需加密的情况：
 * (1) 服务端利用 RSA 创建一对公私钥，服务端存储私钥，将公钥给客户端
 * (2) 客户端每次请求前，将明文数据利用公钥进行加密，然后将密文传递给服务端
 * (3) 服务端拿到密文，利用私钥进行解密，得到明文数据，然后进行业务处理
 * <p>
 * 2、如果想做到返回参数也进行加密，那么两个方法
 * (1) 客户端创建一对公私钥，服务端再重新创建一对公私钥，互相各拿对方的公钥，这样就可以进行传参的加密解密，和响应数据的加解密！！！
 * PS：私钥放在客户端就违背了非对称加密 RSA 的原则性了
 * (2) 但是为了解决 RSA 加解密性能问题，所以采用了 RSA 非对称 + AES 对称加密，大致思路：
 * *** 客户端：
 * *** 1) 客户端随机产生 AES 密钥
 * *** 2) 对数据（重要信息）进行 AES 加密
 * *** 3) 通过服务端提供的 RSA 公钥对 AES 密钥进行加密
 * *** 4) 把加密后的 AES 密钥，和 AES 加密后的数据一起传递给服务端
 * *** 服务端：
 * *** 1) 拿到客户端传来的 AES 加密密钥，利用 RSA 私钥进行解密
 * *** 2) 利用解密后的 AES 对数据进行解密，然后进行业务处理
 * *** 3) 返回数据进行加密，通过 AES 密钥进行加密，返回给客户端
 * *** 4) 客户端拿到服务端返回的密文，利用 AES 密钥进行解密，得到明文数据
 * <p>
 * 这里采用的是 RSA + AES 混合加密方式，这样在传输的过程中，即时加密后的 AES 密钥被别人截取，
 * 对其也无济于事，因为他并不知道 RSA 的私钥，无法解密得到原本的 AES 密钥。
 *
 * @author Zhougang
 */
public class ApiDecryptRequestBodyFilter extends OncePerRequestFilter {

    private final GlobalExceptionHandler globalExceptionHandler;

    private final EncryptProperties encryptProperties;

    public ApiDecryptRequestBodyFilter(EncryptProperties encryptProperties, GlobalExceptionHandler globalExceptionHandler) {
        this.encryptProperties = encryptProperties;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ApiDecrypt apiEncrypt = getApiDecryptAnnotation(request);
        if (apiEncrypt != null) {
            ApiDecryptRequestBodyWrapper decryptRequestBodyWrapper;
            try {
                decryptRequestBodyWrapper = new ApiDecryptRequestBodyWrapper(request, encryptProperties);
            } catch (Exception e) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, e);
                ServletUtils.writeJSON(response, result);
                return;
            }
            filterChain.doFilter(decryptRequestBodyWrapper, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private ApiDecrypt getApiDecryptAnnotation(HttpServletRequest servletRequest) {
        RequestMappingHandlerMapping handlerMapping = SpringUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        HandlerExecutionChain mappingHandler;
        try {
            mappingHandler = handlerMapping.getHandler(servletRequest);
        } catch (Exception e) {
            return null;
        }
        Object handler = mappingHandler.getHandler();
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            return handlerMethod.getMethodAnnotation(ApiDecrypt.class);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 只处理 json 请求内容
        return !ServletUtils.isJsonRequest(request);
    }

}
