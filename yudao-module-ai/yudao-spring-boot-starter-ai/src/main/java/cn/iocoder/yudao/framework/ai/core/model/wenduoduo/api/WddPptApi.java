package cn.iocoder.yudao.framework.ai.core.model.wenduoduo.api;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

// TODO @新：要不改成 WenDuoDuoPptApi
/**
 * 文多多 API
 *
 * @see <a href="https://docmee.cn/open-platform/api">PPT 生成 API</a>
 *
 * @author xiaoxin
 */
@Slf4j
public class WddPptApi {

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

    // TODO @新：是不是不用 baseUrl 哈
    public WddPptApi(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                // TODO @新：建议，token 作为 defaultHeader
                .defaultHeaders((headers) -> headers.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }

    /**
     * 创建 token
     *
     * @param request 请求信息
     * @return token
     */
    public String createApiToken(CreateTokenRequest request) {
        return this.webClient.post()
                .uri("/api/user/createApiToken")
                .header("Api-Key", request.apiKey)
                .body(Mono.just(request), CreateTokenRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<String>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("创建 token 异常，" + response.message));
                        return;
                    }
                    sink.next(response.data.get("token").toString());
                })
                .block();
    }

    // TODO @xin：是不是给个 API 连接，这样 type、content、files 都不用写注释太细了
    /**
     * 创建任务
     *
     * @param type    类型
     *                1.智能生成（主题、要求）
     *                2.上传文件生成
     *                3.上传思维导图生成
     *                4.通过word精准转ppt
     *                5.通过网页链接生成
     *                6.粘贴文本内容生成
     *                7.Markdown大纲生成
     * @param content 内容
     *                type=1 用户输入主题或要求（不超过1000字符）
     *                type=2、4 不传
     *                type=3 幕布等分享链接
     *                type=5 网页链接地址（http/https）
     *                type=6 粘贴文本内容（不超过20000字符）
     *                type=7 大纲内容（markdown）
     * @param files   文件列表
     *                文件列表（文件数不超过5个，总大小不超过50M）：
     *                type=1 上传参考文件（非必传，支持多个）
     *                type=2 上传文件（支持多个）
     *                type=3 上传思维导图（xmind/mm/md）（仅支持一个）
     *                type=4 上传word文件（仅支持一个）
     *                type=5、6、7 不传
     *                <p>
     *                支持格式：doc/docx/pdf/ppt/pptx/txt/md/xls/xlsx/csv/html/epub/mobi/xmind/mm
     * @return 任务ID
     */
    public ApiResponse createTask(String token, Integer type, String content, List<MultipartFile> files) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("type", type);
        if (content != null) {
            formData.add("content", content);
        }
        if (files != null) {
            for (MultipartFile file : files) {
                formData.add("file", file.getResource());
            }
        }
        return this.webClient.post()
                .uri("/api/ppt/v2/createTask")
                .header("token", token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(formData))
                .bodyToMono(ApiResponse.class)
                .block();
    }

    /**
     * 获取生成选项
     *
     * @param lang 语种
     * @return 生成选项
     */
    public Map<String, Object> getOptions(String lang) {
        String uri = "/api/ppt/v2/options";
        if (lang != null) {
            uri += "?lang=" + lang;
        }
        return this.webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(lang))
                .bodyToMono(new ParameterizedTypeReference<ApiResponse>() {
                })
                .<Map<String, Object>>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("获取生成选项异常，" + response.message));
                        return;
                    }
                    sink.next(response.data);
                })
                .block();
    }

    /**
     * 分页查询 PPT 模板
     *
     * @param token   令牌
     * @param request 请求体
     * @return 模板列表
     */
    public PagePptTemplateInfo getTemplatePage(String token, TemplateQueryRequest request) {
        return this.webClient.post()
                .uri("/api/ppt/templates")
                .header("token", token)
                .bodyValue(request)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(new ParameterizedTypeReference<PagePptTemplateInfo>() {
                })
                .block();
    }

    /**
     * 生成大纲内容
     *
     * @return 大纲内容流
     */
    public Flux<Map<String, Object>> createOutline(String token, CreateOutlineRequest request) {
        return this.webClient.post()
                .uri("/api/ppt/v2/generateContent")
                .header("token", token)
                .body(Mono.just(request), CreateOutlineRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * 修改大纲内容
     *
     * @param request 请求体
     * @return 大纲内容流
     */
    public Flux<Map<String, Object>> updateOutline(String token, UpdateOutlineRequest request) {
        return this.webClient.post()
                .uri("/api/ppt/v2/updateContent")
                .header("token", token)
                .body(Mono.just(request), UpdateOutlineRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * 生成 PPT
     *
     * @return PPT信息
     */
    public PptInfo create(String token, CreatePptRequest request) {
        return this.webClient.post()
                .uri("/api/ppt/v2/generatePptx")
                .header("token", token)
                .body(Mono.just(request), CreatePptRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(ApiResponse.class)
                .<PptInfo>handle((response, sink) -> {
                    if (response.code != 0) {
                        sink.error(new IllegalStateException("生成 PPT 异常，" + response.message));
                        return;
                    }
                    sink.next(Objects.requireNonNull(JsonUtils.parseObject(JsonUtils.toJsonString(response.data.get("pptInfo")), PptInfo.class)));
                })
                .block();
    }


    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record CreateTokenRequest(
            String apiKey,
            String uid,
            Integer limit
    ) {

        public CreateTokenRequest(String apiKey) {
            this(apiKey, null, null);
        }

    }

    /**
     * API 通用响应
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record ApiResponse(
            Integer code,
            String message,
            Map<String, Object> data
    ) { }

    /**
     * 创建任务
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record CreateTaskRequest(
            Integer type,
            String content,
            List<MultipartFile> files
    ) { }

    /**
     * 生成大纲内容请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record CreateOutlineRequest(
            String id,
            String length,
            String scene,
            String audience,
            String lang,
            String prompt
    ) { }

    /**
     * 修改大纲内容请求
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record UpdateOutlineRequest(
            String id,
            String markdown,
            String question
    ) { }

    /**
     * 生成 PPT 请求
     */
    // TODO @新：要不按照 PptCreateRequest 这样的风格
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record CreatePptRequest(
            String id,
            String templateId,
            String markdown
    ) { }

    // TODO @新：要不写下类注释
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record PptInfo(
            String id,
            String name,
            String subject,
            String coverUrl,
            String fileUrl,
            String templateId,
            String pptxProperty,
            String userId,
            String userName,
            int companyId,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime updateTime,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createTime,
            String createUser,
            String updateUser
    ) { }

    // TODO @新：要不写下类注释
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record TemplateQueryRequest(
            int page,
            int size,
            Filter filters
    ) {

        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        public record Filter(
                int type,
                String category,
                String style,
                String themeColor
        ) { }

    }

    // TODO @新：要不写下类注释
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record PagePptTemplateInfo(
            List<PptTemplateInfo> data,
            String total
    ) {}


    // TODO @新：要不写下类注释
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record PptTemplateInfo(
            String id,
            int type,
            Integer subType,
            String layout,
            String category,
            String style,
            String themeColor,
            String lang,
            boolean animation,
            String subject,
            String coverUrl,
            String fileUrl,
            List<String> pageCoverUrls,
            String pptxProperty,
            int sort,
            int num,
            Integer imgNum,
            int isDeleted,
            String userId,
            int companyId,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime updateTime,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createTime,
            String createUser,
            String updateUser
    ) { }

}