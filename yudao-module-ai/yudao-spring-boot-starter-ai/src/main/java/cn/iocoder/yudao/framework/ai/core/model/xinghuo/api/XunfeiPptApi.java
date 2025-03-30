package cn.iocoder.yudao.framework.ai.core.model.xinghuo.api;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

// TODO @新：要不改成 XunFeiPptApi
/**
 * 讯飞智能 PPT 生成 API
 *
 * @see <a href="https://www.xfyun.cn/doc/spark/PPTv2.html">智能 PPT 生成 API</a>
 *
 * @author xiaoxin
 */
@Slf4j
public class XunfeiPptApi {

    public static final String BASE_URL = "https://zwapi.xfyun.cn/api/ppt/v2";

    private final WebClient webClient;
    private final String appId;
    private final String apiSecret;

    private final Predicate<HttpStatusCode> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

    private final Function<Object, Function<ClientResponse, Mono<? extends Throwable>>> EXCEPTION_FUNCTION =
            reqParam -> response -> response.bodyToMono(String.class).handle((responseBody, sink) -> {
                log.error("[xunfei-ppt-api] 调用失败！请求参数:[{}]，响应数据: [{}]", reqParam, responseBody);
                sink.error(new IllegalStateException("[xunfei-ppt-api] 调用失败！"));
            });

    // TODO @新：是不是不用 baseUrl 哈
    public XunfeiPptApi(String baseUrl, String appId, String apiSecret) {
        // TODO @新：建议，增加 defaultheaders，例如说 appid 之类的；或者每个请求，通过 headers customer 处理。
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.appId = appId;
        this.apiSecret = apiSecret;
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
        return new SignatureInfo(appId, ts, signature);
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
        try {
            // TODO @新：使用 hutool 简化
            String auth = md5(appId + timestamp);
            return hmacSHA1Encrypt(auth, apiSecret);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[xunfei-ppt-api] 生成签名失败", e);
            throw new IllegalStateException("[xunfei-ppt-api] 生成签名失败");
        }
    }

    /**
     * HMAC SHA1 加密
     */
    private String hmacSHA1Encrypt(String encryptText, String encryptKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(
                encryptKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(keySpec);
        byte[] result = mac.doFinal(encryptText.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * MD5 哈希
     */
    private String md5(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
        // TODO @新：可以使用 ObjUtil.defaultIfNull
        requestBody.put("pageSize", pageSize != null ? pageSize : 10);
        return this.webClient.post()
                .uri("/template/list")
                .header("appId", signInfo.appId)
                .header("timestamp", signInfo.timestamp)
                .header("signature", signInfo.signature)
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
                .header("appId", signInfo.appId)
                .header("timestamp", signInfo.timestamp)
                .header("signature", signInfo.signature)
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
        MultiValueMap<String, Object> formData = buildCreateFormData(request);
        return this.webClient.post()
                .uri("/create")
                .header("appId", signInfo.appId)
                .header("timestamp", signInfo.timestamp)
                .header("signature", signInfo.signature)
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
                .header("appId", signInfo.appId)
                .header("timestamp", signInfo.timestamp)
                .header("signature", signInfo.signature)
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
                .header("appId", signInfo.appId)
                .header("timestamp", signInfo.timestamp)
                .header("signature", signInfo.signature)
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
            String appId,
            String timestamp,
            String signature
    ) { }

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
    ) { }

    /**
     * 模板列表数据
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record TemplatePageData(
            String total,
            List<TemplateInfo> records,
            Integer pageNum
    ) { }

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
    ) { }

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
    ) { }

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
    ) { }

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
            ) { }

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
    ) { }

    /**
     * 进度响应数据
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record ProgressResponseData(
            int process,
            String pptId,
            String pptUrl,
            // TODO @新：字段注释，去掉
            String pptStatus,         // PPT构建状态：building（构建中），done（已完成），build_failed（生成失败）
            String aiImageStatus,     // ai配图状态：building（构建中），done（已完成）
            String cardNoteStatus,    // 演讲备注状态：building（构建中），done（已完成）
            String errMsg,            // 生成PPT的失败信息
            Integer totalPages,       // 生成PPT的总页数
            Integer donePages         // 生成PPT的完成页数
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

        /**
         * 创建构建器
         *
         * @return 构建器
         */
        public static Builder builder() {
            return new Builder();
        }

        // TODO @新：这个可以用 lombok 简化么？
        /**
         * 构建器类
         */
        public static class Builder {

            private String query;
            private String outlineSid;
            private OutlineData outline;
            private String templateId;
            private String businessId;
            private String author;
            private Boolean isCardNote;
            private Boolean search;
            private String language;
            private String fileUrl;
            private String fileName;
            private Boolean isFigure;
            private String aiImage;

            public Builder query(String query) {
                this.query = query;
                return this;
            }

            public Builder outlineSid(String outlineSid) {
                this.outlineSid = outlineSid;
                return this;
            }

            public Builder outline(OutlineData outline) {
                this.outline = outline;
                return this;
            }

            public Builder templateId(String templateId) {
                this.templateId = templateId;
                return this;
            }

            public Builder businessId(String businessId) {
                this.businessId = businessId;
                return this;
            }

            public Builder author(String author) {
                this.author = author;
                return this;
            }

            public Builder isCardNote(Boolean isCardNote) {
                this.isCardNote = isCardNote;
                return this;
            }

            public Builder search(Boolean search) {
                this.search = search;
                return this;
            }

            public Builder language(String language) {
                this.language = language;
                return this;
            }

            public Builder fileUrl(String fileUrl) {
                this.fileUrl = fileUrl;
                return this;
            }

            public Builder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public Builder isFigure(Boolean isFigure) {
                this.isFigure = isFigure;
                return this;
            }

            public Builder aiImage(String aiImage) {
                this.aiImage = aiImage;
                return this;
            }

            public CreatePptByOutlineRequest build() {
                return new CreatePptByOutlineRequest(
                        query, outlineSid, outline, templateId, businessId, author,
                        isCardNote, search, language, fileUrl, fileName, isFigure, aiImage
                );
            }
        }
    }

    /**
     * 构建创建 PPT 的表单数据
     *
     * @param request 请求参数
     * @return 表单数据
     */
    private MultiValueMap<String, Object> buildCreateFormData(CreatePptRequest request) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        // 添加请求参数
        if (request.query() != null) {
            formData.add("query", request.query());
        }
        if (request.file() != null) {
            try {
                formData.add("file", new ByteArrayResource(request.file().getBytes()) {
                    @Override
                    public String getFilename() {
                        return request.file().getOriginalFilename();
                    }
                });
            } catch (IOException e) {
                log.error("[xunfei-ppt-api] 文件处理失败", e);
                throw new IllegalStateException("[xunfei-ppt-api] 文件处理失败", e);
            }
        }
        // TODO @新：要不搞个 MapUtil.addIfPresent 方法？
        if (request.fileUrl() != null) {
            formData.add("fileUrl", request.fileUrl());
        }
        if (request.fileName() != null) {
            formData.add("fileName", request.fileName());
        }
        if (request.templateId() != null) {
            formData.add("templateId", request.templateId());
        }
        if (request.businessId() != null) {
            formData.add("businessId", request.businessId());
        }
        if (request.author() != null) {
            formData.add("author", request.author());
        }
        if (request.isCardNote() != null) {
            formData.add("isCardNote", request.isCardNote().toString());
        }
        if (request.search() != null) {
            formData.add("search", request.search().toString());
        }
        if (request.language() != null) {
            formData.add("language", request.language());
        }
        if (request.isFigure() != null) {
            formData.add("isFigure", request.isFigure().toString());
        }
        if (request.aiImage() != null) {
            formData.add("aiImage", request.aiImage());
        }
        return formData;
    }

    /**
     * 直接生成PPT请求参数
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
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

        /**
         * 创建构建器
         *
         * @return 构建器
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * 构建器类
         */
        public static class Builder {

            private String query;
            private MultipartFile file;
            private String fileUrl;
            private String fileName;
            private String templateId;
            private String businessId;
            private String author;
            private Boolean isCardNote;
            private Boolean search;
            private String language;
            private Boolean isFigure;
            private String aiImage;

            // TODO @新：这个可以用 lombok 简化么？

            public Builder query(String query) {
                this.query = query;
                return this;
            }

            public Builder file(MultipartFile file) {
                this.file = file;
                return this;
            }

            public Builder fileUrl(String fileUrl) {
                this.fileUrl = fileUrl;
                return this;
            }

            public Builder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public Builder templateId(String templateId) {
                this.templateId = templateId;
                return this;
            }

            public Builder businessId(String businessId) {
                this.businessId = businessId;
                return this;
            }

            public Builder author(String author) {
                this.author = author;
                return this;
            }

            public Builder isCardNote(Boolean isCardNote) {
                this.isCardNote = isCardNote;
                return this;
            }

            public Builder search(Boolean search) {
                this.search = search;
                return this;
            }

            public Builder language(String language) {
                this.language = language;
                return this;
            }

            public Builder isFigure(Boolean isFigure) {
                this.isFigure = isFigure;
                return this;
            }

            public Builder aiImage(String aiImage) {
                this.aiImage = aiImage;
                return this;
            }

            public CreatePptRequest build() {
                // 验证参数
                if (query == null && file == null && fileUrl == null) {
                    throw new IllegalArgumentException("query、file、fileUrl必填其一");
                }
                if ((file != null || fileUrl != null) && fileName == null) {
                    throw new IllegalArgumentException("如果传file或者fileUrl，fileName必填");
                }
                return new CreatePptRequest(
                        query, file, fileUrl, fileName, templateId, businessId, author,
                        isCardNote, search, language, isFigure, aiImage
                );
            }
        }
    }

}