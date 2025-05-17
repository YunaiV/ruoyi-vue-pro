package cn.iocoder.yudao.module.ai.framework.ai.core.model.xinghuo.api;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 讯飞智能 PPT 生成 API
 *
 * @author xiaoxin
 * @see <a href="https://www.xfyun.cn/doc/spark/PPTv2.html">智能 PPT 生成 API</a>
 */
@Slf4j
public class XunFeiPptApi {

    public static final String BASE_URL = "https://zwapi.xfyun.cn/api/ppt/v2";
    private static final String HEADER_APP_ID = "appId";
    private static final String HEADER_TIMESTAMP = "timestamp";
    private static final String HEADER_SIGNATURE = "signature";

    private final WebClient webClient;
    private final String appId;
    private final String apiSecret;

    private final Predicate<HttpStatusCode> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

    private final Function<Object, Function<ClientResponse, Mono<? extends Throwable>>> EXCEPTION_FUNCTION =
            reqParam -> response -> response.bodyToMono(String.class).handle((responseBody, sink) -> {
                log.error("[XunFeiPptApi] 调用失败！请求参数:[{}]，响应数据: [{}]", reqParam, responseBody);
                sink.error(new IllegalStateException("[XunFeiPptApi] 调用失败！"));
            });

    public XunFeiPptApi(String appId, String apiSecret) {
        this.appId = appId;
        this.apiSecret = apiSecret;
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeaders((headers) -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add(HEADER_APP_ID, appId);
                })
                .build();

    }

    /**
     * 获取签名
     *
     * @return 签名信息
     */
    private SignatureInfo getSignature() {
        long timestamp = System.currentTimeMillis() / 1000;
        String ts = String.valueOf(timestamp);
        String signature = generateSignature(appId, apiSecret, timestamp);
        return new SignatureInfo(ts, signature);
    }

    /**
     * 生成签名
     *
     * @param appId     应用ID
     * @param apiSecret 应用密钥
     * @param timestamp 时间戳（秒）
     * @return 签名
     */
    private String generateSignature(String appId, String apiSecret, long timestamp) {
        String auth = SecureUtil.md5(appId + timestamp);
        return SecureUtil.hmac(HmacAlgorithm.HmacSHA1, apiSecret).digestBase64(auth, false);
    }

    /**
     * 获取 PPT 模板列表
     *
     * @param style    风格，如"商务"
     * @param pageSize 每页数量
     * @return 模板列表
     */
    public TemplatePageResponse getTemplatePage(String style, Integer pageSize) {
        SignatureInfo signInfo = getSignature();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("style", style);
        requestBody.put("pageSize", ObjUtil.defaultIfNull(pageSize, 20));
        return this.webClient.post()
                .uri("/template/list")
                .header(HEADER_TIMESTAMP, signInfo.timestamp)
                .header(HEADER_SIGNATURE, signInfo.signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(requestBody))
                .bodyToMono(TemplatePageResponse.class)
                .block();
    }

    /**
     * 创建大纲（通过文本）
     *
     * @param query 查询文本
     * @return 大纲创建响应
     */
    public CreateResponse createOutline(String query) {
        SignatureInfo signInfo = getSignature();
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("query", query);
        return this.webClient.post()
                .uri("/createOutline")
                .header(HEADER_TIMESTAMP, signInfo.timestamp)
                .header(HEADER_SIGNATURE, signInfo.signature)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(formData))
                .bodyToMono(CreateResponse.class)
                .block();
    }


    /**
     * 直接创建 PPT（简化版 - 通过文本）
     *
     * @param query 查询文本
     * @return 创建响应
     */
    public CreateResponse create(String query) {
        CreatePptRequest request = CreatePptRequest.builder()
                .query(query)
                .build();
        return create(request);
    }

    /**
     * 直接创建 PPT（简化版 - 通过文件）
     *
     * @param file     文件
     * @param fileName 文件名
     * @return 创建响应
     */
    public CreateResponse create(MultipartFile file, String fileName) {
        CreatePptRequest request = CreatePptRequest.builder()
                .file(file).fileName(fileName).build();
        return create(request);
    }

    /**
     * 直接创建 PPT（完整版）
     *
     * @param request 请求参数
     * @return 创建响应
     */
    public CreateResponse create(CreatePptRequest request) {
        SignatureInfo signInfo = getSignature();
        MultiValueMap<String, Object> formData = buildCreatePptFormData(request);
        return this.webClient.post()
                .uri("/create")
                .header(HEADER_TIMESTAMP, signInfo.timestamp)
                .header(HEADER_SIGNATURE, signInfo.signature)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(formData))
                .bodyToMono(CreateResponse.class)
                .block();
    }


    /**
     * 通过大纲创建 PPT（简化版）
     *
     * @param outline 大纲内容
     * @param query   查询文本
     * @return 创建响应
     */
    public CreateResponse createPptByOutline(OutlineData outline, String query) {
        CreatePptByOutlineRequest request = CreatePptByOutlineRequest.builder()
                .outline(outline)
                .query(query)
                .build();
        return createPptByOutline(request);
    }

    /**
     * 通过大纲创建 PPT（完整版）
     *
     * @param request 请求参数
     * @return 创建响应
     */
    public CreateResponse createPptByOutline(CreatePptByOutlineRequest request) {
        SignatureInfo signInfo = getSignature();
        return this.webClient.post()
                .uri("/createPptByOutline")
                .header(HEADER_TIMESTAMP, signInfo.timestamp)
                .header(HEADER_SIGNATURE, signInfo.signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(CreateResponse.class)
                .block();
    }

    /**
     * 检查 PPT 生成进度
     *
     * @param sid 任务 ID
     * @return 进度响应
     */
    public ProgressResponse checkProgress(String sid) {
        SignatureInfo signInfo = getSignature();
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/progress")
                        .queryParam("sid", sid)
                        .build())
                .header(HEADER_TIMESTAMP, signInfo.timestamp)
                .header(HEADER_SIGNATURE, signInfo.signature)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(sid))
                .bodyToMono(ProgressResponse.class)
                .block();
    }

    /**
     * 签名信息
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private record SignatureInfo(
            String timestamp,
            String signature
    ) {
    }

    /**
     * 模板列表响应
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record TemplatePageResponse(
            boolean flag,
            int code,
            String desc,
            Integer count,
            TemplatePageData data
    ) {
    }

    /**
     * 模板列表数据
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record TemplatePageData(
            String total,
            List<TemplateInfo> records,
            Integer pageNum
    ) {
    }

    /**
     * 模板信息
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record TemplateInfo(
            String templateIndexId,
            Integer pageCount,
            String type,
            String color,
            String industry,
            String style,
            String detailImage
    ) {
    }

    /**
     * 创建响应
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record CreateResponse(
            boolean flag,
            int code,
            String desc,
            Integer count,
            CreateResponseData data
    ) {
    }

    /**
     * 创建响应数据
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record CreateResponseData(
            String sid,
            String coverImgSrc,
            String title,
            String subTitle,
            OutlineData outline
    ) {
    }

    /**
     * 大纲数据结构
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record OutlineData(
            String title,
            String subTitle,
            List<Chapter> chapters
    ) {

        /**
         * 章节结构
         */
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        public record Chapter(
                String chapterTitle,
                List<ChapterContent> chapterContents
        ) {

            /**
             * 章节内容
             */
            @JsonInclude(value = JsonInclude.Include.NON_NULL)
            public record ChapterContent(
                    String chapterTitle
            ) {
            }

        }

        /**
         * 将大纲对象转换为JSON字符串
         *
         * @return 大纲JSON字符串
         */
        public String toJsonString() {
            return JsonUtils.toJsonString(this);
        }
    }

    /**
     * 进度响应
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record ProgressResponse(
            int code,
            String desc,
            ProgressResponseData data
    ) {
    }

    /**
     * 进度响应数据
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record ProgressResponseData(
            int process,
            String pptId,
            String pptUrl,
            String pptStatus,
            String aiImageStatus,
            String cardNoteStatus,
            String errMsg,
            Integer totalPages,
            Integer donePages
    ) {

        /**
         * 是否全部完成
         *
         * @return 是否全部完成
         */
        public boolean isAllDone() {
            return "done".equals(pptStatus)
                    && ("done".equals(aiImageStatus) || aiImageStatus == null)
                    && ("done".equals(cardNoteStatus) || cardNoteStatus == null);
        }

        /**
         * 是否失败
         *
         * @return 是否失败
         */
        public boolean isFailed() {
            return "build_failed".equals(pptStatus);
        }

        /**
         * 获取进度百分比
         *
         * @return 进度百分比
         */
        public int getProgressPercent() {
            if (totalPages == null || totalPages == 0 || donePages == null) {
                return process; // 兼容旧版返回
            }
            return (int) (donePages * 100.0 / totalPages);
        }

    }

    /**
     * 通过大纲创建 PPT 请求参数
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Builder
    public record CreatePptByOutlineRequest(
            String query,                // 用户生成PPT要求（最多8000字）
            String outlineSid,           // 已生成大纲后，响应返回的请求大纲唯一id
            OutlineData outline,         // 大纲内容
            String templateId,           // 模板ID
            String businessId,           // 业务ID（非必传）
            String author,               // PPT作者名
            Boolean isCardNote,          // 是否生成PPT演讲备注
            Boolean search,              // 是否联网搜索
            String language,             // 语种
            String fileUrl,              // 文件地址
            String fileName,             // 文件名(带文件名后缀)
            Boolean isFigure,            // 是否自动配图
            String aiImage               // ai配图类型：normal、advanced
    ) {
    }


    /**
     * 构建创建 PPT 的表单数据
     *
     * @param request 请求参数
     * @return 表单数据
     */
    private MultiValueMap<String, Object> buildCreatePptFormData(CreatePptRequest request) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        if (request.file() != null) {
            try {
                formData.add("file", new ByteArrayResource(request.file().getBytes()) {
                    @Override
                    public String getFilename() {
                        return request.file().getOriginalFilename();
                    }
                });
            } catch (IOException e) {
                log.error("[XunFeiPptApi] 文件处理失败", e);
                throw new IllegalStateException("[XunFeiPptApi] 文件处理失败", e);
            }
        }
        Map<String, Object> param = new HashMap<>();
        addIfPresent(param, "query", request.query());
        addIfPresent(param, "fileUrl", request.fileUrl());
        addIfPresent(param, "fileName", request.fileName());
        addIfPresent(param, "templateId", request.templateId());
        addIfPresent(param, "businessId", request.businessId());
        addIfPresent(param, "author", request.author());
        addIfPresent(param, "isCardNote", request.isCardNote());
        addIfPresent(param, "search", request.search());
        addIfPresent(param, "language", request.language());
        addIfPresent(param, "isFigure", request.isFigure());
        addIfPresent(param, "aiImage", request.aiImage());
        param.forEach(formData::add);
        return formData;
    }

    public static <K, V> void addIfPresent(Map<K, V> map, K key, V value) {
        if (ObjUtil.isNull(key) || ObjUtil.isNull(map)) {
            return;
        }

        boolean isPresent = false;
        if (ObjUtil.isNotNull(value)) {
            if (value instanceof String) {
                // 字符串：需要有实际内容
                isPresent = StringUtils.hasText((String) value);
            } else {
                // 其他类型：非 null 即视为存在
                isPresent = true;
            }
        }
        if (isPresent) {
            map.put(key, value);
        }
    }

    /**
     * 直接生成PPT请求参数
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Builder
    public record CreatePptRequest(
            String query,                // 用户生成PPT要求（最多8000字）
            MultipartFile file,          // 上传文件
            String fileUrl,              // 文件地址
            String fileName,             // 文件名(带文件名后缀)
            String templateId,           // 模板ID
            String businessId,           // 业务ID（非必传）
            String author,               // PPT作者名
            Boolean isCardNote,          // 是否生成PPT演讲备注
            Boolean search,              // 是否联网搜索
            String language,             // 语种
            Boolean isFigure,            // 是否自动配图
            String aiImage               // ai配图类型：normal、advanced
    ) {

    }

}