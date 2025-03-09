package cn.iocoder.yudao.framework.ai.core.model.wenduoduo.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 文多多 API
 * <p>
 * <p>
 * * 对接文多多：<a href="https://docmee.cn/open-platform/api">PPT 生成 API</a>
 *
 * @author xiaoxin
 */
@Slf4j
public class WddApi {

    public static final String BASE_URL = "https://docmee.cn";

    private final WebClient webClient;

    private final Predicate<HttpStatusCode> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

    private final Function<Object, Function<ClientResponse, Mono<? extends Throwable>>> EXCEPTION_FUNCTION =
            reqParam -> response -> response.bodyToMono(String.class).handle((responseBody, sink) -> {
                HttpRequest request = response.request();
                log.error("[wdd-api] 调用失败！请求方式:[{}]，请求地址:[{}]，请求参数:[{}]，响应数据: [{}]",
                        request.getMethod(), request.getURI(), reqParam, responseBody);
                sink.error(new IllegalStateException("[wdd-api] 调用失败！"));
            });

    public WddApi(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders((headers) -> headers.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }

    /**
     * 创建API令牌
     *
     * @param apiKey API密钥
     * @param uid    用户ID
     * @param limit  限制
     * @return API令牌
     */
    public String createApiToken(String apiKey, String uid, Integer limit) {
        CreateApiTokenRequest request = new CreateApiTokenRequest(uid, limit);
        return this.webClient.post()
                .uri("/api/user/createApiToken")
                .header("Api-Key", apiKey)
                .body(Mono.just(request), CreateApiTokenRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<String>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("创建apiToken异常，" + response.message));
                        return;
                    }
                    sink.next(response.data.get("token").toString());
                })
                .block();
    }

    /**
     * 解析文件数据
     *
     * @param apiToken API令牌
     * @param content  内容
     * @param fileUrl  文件URL
     * @return 数据URL
     */
    public String parseFileData(String apiToken, String content, String fileUrl) {
        ParseFileDataRequest request = new ParseFileDataRequest(content, fileUrl);
        return this.webClient.post()
                .uri("/api/ppt/parseFileData")
                .header("token", apiToken)
                .body(Mono.just(request), ParseFileDataRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<String>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("解析文件或内容异常，" + response.message));
                        return;
                    }
                    sink.next(response.data.get("dataUrl").toString());
                })
                .block();
    }

    /**
     * 生成大纲
     *
     * @param apiToken API令牌
     * @param subject  主题
     * @param dataUrl  数据URL
     * @param prompt   提示词
     * @return 大纲内容
     */
    public String generateOutline(String apiToken, String subject, String dataUrl, String prompt) {
        GenerateOutlineRequest request = new GenerateOutlineRequest(subject, dataUrl, prompt);
        return this.webClient.post()
                .uri("/api/ppt/generateOutline")
                .header("token", apiToken)
                .body(Mono.just(request), GenerateOutlineRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 生成大纲内容
     *
     * @param apiToken        API令牌
     * @param outlineMarkdown 大纲Markdown
     * @param dataUrl         数据URL
     * @param prompt          提示词
     * @return 大纲内容
     */
    public String generateContent(String apiToken, String outlineMarkdown, String dataUrl, String prompt) {
        GenerateContentRequest request = new GenerateContentRequest(outlineMarkdown, dataUrl, prompt);
        return this.webClient.post()
                .uri("/api/ppt/generateContent")
                .header("token", apiToken)
                .body(Mono.just(request), GenerateContentRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 异步生成大纲内容
     *
     * @param apiToken        API令牌
     * @param outlineMarkdown 大纲Markdown
     * @param dataUrl         数据URL
     * @param templateId      模板ID
     * @param prompt          提示词
     * @return 大纲内容和PPT ID
     */
    public Map<String, Object> asyncGenerateContent(String apiToken, String outlineMarkdown, String dataUrl, String templateId, String prompt) {
        AsyncGenerateContentRequest request = new AsyncGenerateContentRequest(outlineMarkdown, dataUrl, templateId, prompt);
        return this.webClient.post()
                .uri("/api/ppt/generateContent")
                .header("token", apiToken)
                .body(Mono.just(request), AsyncGenerateContentRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    /**
     * 随机获取一个模板ID
     *
     * @param apiToken API令牌
     * @return 模板ID
     */
    public String randomOneTemplateId(String apiToken) {
        RandomTemplateRequest request = new RandomTemplateRequest(1, new TemplateFilter(1));
        return this.webClient.post()
                .uri("/api/ppt/randomTemplates")
                .header("token", apiToken)
                .body(Mono.just(request), RandomTemplateRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<String>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("获取模板异常，" + response.message));
                        return;
                    }
                    sink.next(((Map<String, Object>) ((Object[]) response.data.get("data"))[0]).get("id").toString());
                })
                .block();
    }

    /**
     * 生成PPT
     *
     * @param apiToken     API令牌
     * @param templateId   模板ID
     * @param markdown     Markdown内容
     * @param pptxProperty PPT属性
     * @return PPT信息
     */
    public Map<String, Object> generatePptx(String apiToken, String templateId, String markdown, boolean pptxProperty) {
        GeneratePptxRequest request = new GeneratePptxRequest(templateId, markdown, pptxProperty);
        return this.webClient.post()
                .uri("/api/ppt/generatePptx")
                .header("token", apiToken)
                .body(Mono.just(request), GeneratePptxRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<Map<String, Object>>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("生成PPT异常，" + response.message));
                        return;
                    }
                    sink.next((Map<String, Object>) response.data.get("pptInfo"));
                })
                .block();
    }

    /**
     * 下载PPT
     *
     * @param apiToken API令牌
     * @param id       PPT ID
     * @return 下载信息
     */
    public Map<String, Object> downloadPptx(String apiToken, String id) {
        DownloadPptxRequest request = new DownloadPptxRequest(id);
        return this.webClient.post()
                .uri("/api/ppt/downloadPptx")
                .header("token", apiToken)
                .body(Mono.just(request), DownloadPptxRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<Map<String, Object>>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("下载PPT异常，" + response.message));
                        return;
                    }
                    sink.next(response.data);
                })
                .block();
    }

    /**
     * 直接生成PPT
     *
     * @param apiToken     API令牌
     * @param templateId   模板ID
     * @param subject      主题
     * @param dataUrl      数据URL
     * @param prompt       提示词
     * @param pptxProperty PPT属性
     * @return PPT信息
     */
    public Map<String, Object> directGeneratePptx(String apiToken, String templateId, String subject, String dataUrl, String prompt, boolean pptxProperty) {
        DirectGeneratePptxRequest request = new DirectGeneratePptxRequest(false, templateId, subject, dataUrl, prompt, pptxProperty);
        return this.webClient.post()
                .uri("/api/ppt/directGeneratePptx")
                .header("token", apiToken)
                .body(Mono.just(request), DirectGeneratePptxRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<Map<String, Object>>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("生成PPT异常，" + response.message));
                        return;
                    }
                    sink.next((Map<String, Object>) response.data.get("pptInfo"));
                })
                .block();
    }

    /**
     * 查询所有PPT列表
     *
     * @param apiToken API令牌
     * @param body     请求体
     * @return PPT列表
     */
    public Map<String, Object> listAllPptx(String apiToken, String body) {
        return this.webClient.post()
                .uri("/api/ppt/listAllPptx")
                .header("token", apiToken)
                .bodyValue(body)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(body))
                .bodyToMono(ApiResponse.class)
                .<Map<String, Object>>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("查询所有PPT列表异常，" + response.message));
                        return;
                    }
                    sink.next(response.data);
                })
                .block();
    }

    /**
     * 分页查询PPT模板
     *
     * @param apiToken API令牌
     * @param body     请求体
     * @return 模板列表
     */
    public Map<String, Object> getPptTemplates(String apiToken, String body) {
        return this.webClient.post()
                .uri("/api/ppt/templates")
                .header("token", apiToken)
                .bodyValue(body)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(body))
                .bodyToMono(ApiResponse.class)
                .<Map<String, Object>>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("分页查询PPT模板异常，" + response.message));
                        return;
                    }
                    sink.next(response.data);
                })
                .block();
    }

    /**
     * API响应
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record ApiResponse(
            Integer code,
            String message,
            Map<String, Object> data
    ) {
    }

    /**
     * 创建API令牌请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record CreateApiTokenRequest(
            String uid,
            Integer limit
    ) {
    }

    /**
     * 解析文件数据请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record ParseFileDataRequest(
            String content,
            String fileUrl
    ) {
    }

    /**
     * 生成大纲请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record GenerateOutlineRequest(
            String subject,
            String dataUrl,
            String prompt
    ) {
    }

    /**
     * 生成大纲内容请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record GenerateContentRequest(
            String outlineMarkdown,
            String dataUrl,
            String prompt
    ) {
    }

    /**
     * 异步生成大纲内容请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record AsyncGenerateContentRequest(
            String outlineMarkdown,
            String dataUrl,
            String templateId,
            String prompt,
            @JsonProperty("asyncGenPptx") boolean asyncGenPptx
    ) {
        public AsyncGenerateContentRequest(String outlineMarkdown, String dataUrl, String templateId, String prompt) {
            this(outlineMarkdown, dataUrl, templateId, prompt, true);
        }
    }

    /**
     * 随机模板请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record RandomTemplateRequest(
            Integer size,
            TemplateFilter filters
    ) {
    }

    /**
     * 模板过滤器
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record TemplateFilter(
            Integer type
    ) {
    }

    /**
     * 生成PPT请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record GeneratePptxRequest(
            String templateId,
            @JsonProperty("outlineContentMarkdown") String outlineContentMarkdown,
            boolean pptxProperty
    ) {
    }

    /**
     * 下载PPT请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record DownloadPptxRequest(
            String id
    ) {
    }

    /**
     * 直接生成PPT请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record DirectGeneratePptxRequest(
            boolean stream,
            String templateId,
            String subject,
            String dataUrl,
            String prompt,
            boolean pptxProperty
    ) {
    }
} 