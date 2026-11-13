package cn.iocoder.yudao.framework.web.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * 全局异常的 Content-Type 预处理器
 *
 * 问题：SSE 流式接口的 Accept 为 text/event-stream 时，{@link GlobalExceptionHandler} 返回的 {@link CommonResult}
 * 会因为内容协商失败，抛出 HttpMediaTypeNotAcceptableException 异常，最终转发到 /error 地址；
 * 而 ERROR 转发不会重新认证，导致被 Spring Security 错误地返回 401 未登录。
 * 详见 <a href="https://gitee.com/zhijiantianya/yudao-cloud/issues/IDCO5D">https://gitee.com/zhijiantianya/yudao-cloud/issues/IDCO5D</a>
 *
 * 解决：在 {@link GlobalExceptionHandler} 处理异常前，预设 JSON 的 Content-Type，从而跳过内容协商，统一返回 {@link CommonResult}。
 * 注意：它不处理异常，返回 null 交给后续的 HandlerExceptionResolver 继续处理
 *
 * @author 芋道源码
 */
public class GlobalExceptionContentTypeResolver implements HandlerExceptionResolver {

    /**
     * Jackson 可输出的 JSON 类型，与 MappingJackson2HttpMessageConverter 默认支持的类型一致
     */
    private static final List<MediaType> JSON_MEDIA_TYPES = List.of(
            MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        // 已经提交（例如说 SSE 已推送部分数据），或者已设置 Content-Type，或者不是仅接受 SSE 的请求，则保持原有的内容协商
        if (response.isCommitted() || response.getContentType() != null || !isAcceptSseOnly(request)) {
            return null;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return null;
    }

    /**
     * 判断客户端是否仅接受 SSE 响应：Accept 包含 text/event-stream，且不包含 Jackson 可输出的 JSON 类型
     *
     * 1. Accept 为空或格式不正确，返回 false，保持原有的内容协商
     * 2. Accept 包含 application/json、application/*+json（例如说 application/vnd.example+json）或通配类型，
     *    返回 false，由内容协商选择对应的 JSON 类型
     * 3. Accept 不包含 text/event-stream（例如说 text/html），返回 false，保持原有的内容协商
     *
     * @param request 请求
     * @return 是否仅接受 SSE
     */
    private static boolean isAcceptSseOnly(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (StrUtil.isBlank(accept)) {
            return false;
        }
        List<MediaType> mediaTypes;
        try {
            mediaTypes = MediaType.parseMediaTypes(accept);
        } catch (InvalidMediaTypeException ex) {
            return false;
        }
        return mediaTypes.stream().anyMatch(MediaType.TEXT_EVENT_STREAM::equalsTypeAndSubtype)
                && mediaTypes.stream().noneMatch(mediaType -> JSON_MEDIA_TYPES.stream().anyMatch(mediaType::isCompatibleWith));
    }

}
