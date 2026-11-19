package cn.iocoder.yudao.framework.web.core.handler;

import cn.iocoder.yudao.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.config.YudaoWebAutoConfiguration;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link GlobalExceptionContentTypeResolver} 的单元测试
 *
 * 重点验证：SSE 接口（Accept 为 text/event-stream）发生异常时，仍由 {@link GlobalExceptionHandler} 返回 JSON 格式的 CommonResult；
 * 而正常 JSON 请求（含 application/*+json 等 Jackson 支持的类型）、其它格式的 Accept、成功的 SSE 推送、已提交的响应，保持原有的内容协商
 *
 * @author 芋道源码
 */
public class GlobalExceptionContentTypeResolverTest {

    /**
     * Jackson 支持的 application/*+json 类型
     */
    private static final MediaType VENDOR_JSON = MediaType.parseMediaType("application/vnd.example+json");

    private AnnotationConfigWebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        // 初始化 Spring MVC 上下文：注册 GlobalExceptionContentTypeResolver、GlobalExceptionHandler、测试 Controller
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestConfiguration.class);
        context.refresh();
        // 初始化 MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    public void tearDown() {
        context.close();
    }

    // ========== 接入 Spring MVC ==========

    @Test
    public void testSse_serviceException() throws Exception {
        // 调用：SSE 接口同步抛出 ServiceException
        mockMvc.perform(post("/test/service-exception").accept(MediaType.TEXT_EVENT_STREAM))
                // 断言：返回 JSON 格式的业务错误码，而不是内容协商失败
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1001001))
                .andExpect(jsonPath("$.msg").value("业务异常"));
    }

    @Test
    public void testSse_missingParameter() throws Exception {
        // 调用：SSE 接口缺少请求参数
        mockMvc.perform(post("/test/missing-parameter").accept(MediaType.TEXT_EVENT_STREAM))
                // 断言：返回 JSON 格式的参数错误
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.msg").value("请求参数缺失:id"));
    }

    @Test
    public void testSse_asyncServiceException() throws Exception {
        // 准备参数：SSE 接口返回的 emitter，在推送数据前以 ServiceException 结束
        MvcResult mvcResult = mockMvc.perform(post("/test/async-exception").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        // 调用：异步派发
        mockMvc.perform(asyncDispatch(mvcResult))
                // 断言：返回 JSON 格式的业务错误码
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1001002))
                .andExpect(jsonPath("$.msg").value("异步业务异常"));
    }

    @Test
    public void testSse_success() throws Exception {
        // 准备参数：SSE 接口正常推送一条数据
        MvcResult mvcResult = mockMvc.perform(post("/test/success").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        // 调用：异步派发
        mockMvc.perform(asyncDispatch(mvcResult))
                // 断言：仍然是 SSE 格式
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("data:{\"code\":0")));
    }

    @Test
    public void testJson_serviceException() throws Exception {
        // 调用：普通 JSON 接口抛出 ServiceException，Accept 为 application/json
        mockMvc.perform(get("/test/json").accept(MediaType.APPLICATION_JSON))
                // 断言：保持原有的 JSON 结果
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1001003));
        // 调用：普通 JSON 接口抛出 ServiceException，Accept 为 */*
        mockMvc.perform(get("/test/json").accept(MediaType.ALL))
                // 断言：保持原有的 JSON 结果
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1001003));
    }

    @Test
    public void testJson_serviceException_vendorJson() throws Exception {
        // 调用：普通 JSON 接口抛出 ServiceException，Accept 为 Jackson 支持的 application/*+json 类型
        mockMvc.perform(get("/test/json").accept(VENDOR_JSON))
                // 断言：保持原有的内容协商，返回 application/vnd.example+json
                .andExpect(status().isOk())
                .andExpect(content().contentType(VENDOR_JSON))
                .andExpect(jsonPath("$.code").value(1001003));
    }

    @Test
    public void testSse_serviceException_vendorJson() throws Exception {
        // 调用：SSE 接口同步抛出 ServiceException，Accept 同时包含 text/event-stream 和 application/vnd.example+json
        mockMvc.perform(post("/test/service-exception").accept(MediaType.TEXT_EVENT_STREAM, VENDOR_JSON))
                // 断言：客户端可接受 JSON，保持原有的内容协商，返回 application/vnd.example+json
                .andExpect(status().isOk())
                .andExpect(content().contentType(VENDOR_JSON))
                .andExpect(jsonPath("$.code").value(1001001));
    }

    @Test
    public void testJson_serviceException_notAcceptJsonOrSse() {
        // 调用：普通 JSON 接口抛出 ServiceException，Accept 为 text/html，既不是 JSON 也不是 SSE
        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/test/json").accept(MediaType.TEXT_HTML)));
        // 断言：不预设 Content-Type，保持原有的内容协商失败（原异常继续抛出）
        assertInstanceOf(ServiceException.class, ex.getCause());
    }

    // ========== 直接调用 ==========

    @Test
    public void testResolveException_acceptSseOnly() {
        // 调用：Accept 只接受 text/event-stream
        MockHttpServletResponse response = resolveException("text/event-stream", new MockHttpServletResponse());
        // 断言：预设 JSON 的 Content-Type
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    public void testResolveException_invalidAccept() {
        // 调用：Accept 格式不正确
        MockHttpServletResponse response = resolveException("text", new MockHttpServletResponse());
        // 断言：不预设 Content-Type，保持原有的内容协商
        assertNull(response.getContentType());
    }

    @Test
    public void testResolveException_acceptJson() {
        // 调用，并断言：Accept 为空、通配、包含 application/json 或 application/*+json 时，不预设 Content-Type，由内容协商决定
        assertNull(resolveException(null, new MockHttpServletResponse()).getContentType());
        assertNull(resolveException("*/*", new MockHttpServletResponse()).getContentType());
        assertNull(resolveException("text/event-stream, application/json", new MockHttpServletResponse()).getContentType());
        assertNull(resolveException("text/event-stream, application/vnd.example+json", new MockHttpServletResponse()).getContentType());
        assertNull(resolveException("text/event-stream, application/*+json", new MockHttpServletResponse()).getContentType());
    }

    @Test
    public void testResolveException_notAcceptSse() {
        // 调用，并断言：Accept 不包含 text/event-stream 时，不预设 Content-Type，保持原有的内容协商
        assertNull(resolveException("text/html", new MockHttpServletResponse()).getContentType());
        assertNull(resolveException("application/xml", new MockHttpServletResponse()).getContentType());
    }

    @Test
    public void testResolveException_committed() {
        // 准备参数：响应已提交，例如说 SSE 已推送部分数据
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);

        // 调用
        resolveException("text/event-stream", response);
        // 断言：不预设 Content-Type
        assertNull(response.getContentType());
    }

    @Test
    public void testResolveException_contentTypePresent() {
        // 准备参数：响应已设置 Content-Type
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);

        // 调用
        resolveException("text/event-stream", response);
        // 断言：保持原有的 Content-Type
        assertEquals(MediaType.TEXT_EVENT_STREAM_VALUE, response.getContentType());
    }

    /**
     * 调用 {@link GlobalExceptionContentTypeResolver#resolveException}，并断言它不处理异常（返回 null）
     */
    private static MockHttpServletResponse resolveException(String accept, MockHttpServletResponse response) {
        // 准备参数
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (accept != null) {
            request.addHeader(HttpHeaders.ACCEPT, accept);
        }
        // 调用，并断言：交给后续的 HandlerExceptionResolver 继续处理
        assertNull(new GlobalExceptionContentTypeResolver().resolveException(request, response, null,
                new RuntimeException("test")));
        return response;
    }

    @Configuration
    @EnableWebMvc
    public static class TestConfiguration {

        @Bean
        public WebMvcConfigurer globalExceptionWebMvcConfigurer() {
            return new YudaoWebAutoConfiguration().globalExceptionWebMvcConfigurer();
        }

        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler("test", mock(ApiErrorLogCommonApi.class));
        }

        @Bean
        public TestController testController() {
            return new TestController();
        }

    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @PostMapping(value = "/service-exception", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter serviceException() {
            throw new ServiceException(1001001, "业务异常");
        }

        @PostMapping(value = "/missing-parameter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter missingParameter(@RequestParam("id") Long id) {
            return new SseEmitter();
        }

        @PostMapping(value = "/async-exception", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter asyncException() {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new ServiceException(1001002, "异步业务异常"));
            return emitter;
        }

        @PostMapping(value = "/success", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter success() throws IOException {
            SseEmitter emitter = new SseEmitter();
            emitter.send(CommonResult.success("ok"));
            emitter.complete();
            return emitter;
        }

        @GetMapping("/json")
        public CommonResult<String> json() {
            throw new ServiceException(1001003, "JSON 业务异常");
        }

    }

}
