package cn.iocoder.yudao.module.yaya.framework.ai;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_AI_REQUEST_FAILED;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_AI_UNAUTHORIZED;

@Component
public class YayaAiClient {

    private static final String HEADER_INTERNAL_KEY = "X-Yaya-Internal-Key";
    private static final String HEADER_REQUEST_ID = "X-Yaya-Request-Id";

    private final RestTemplate restTemplate;
    private final YayaAiProperties properties;

    public YayaAiClient(RestTemplate restTemplate, YayaAiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public EvaluationAcceptedResponse createEvaluation(EvaluationCreateRequest request, String requestId) {
        try {
            return restTemplate.exchange(evaluationsUrl(), HttpMethod.POST,
                    new HttpEntity<>(request, headers(requestId)), EvaluationAcceptedResponse.class).getBody();
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw exception(YAYA_AI_UNAUTHORIZED);
        } catch (RestClientException ex) {
            throw exception(YAYA_AI_REQUEST_FAILED);
        }
    }

    public EvaluationTaskResponse getEvaluation(String taskId, String requestId) {
        try {
            return restTemplate.exchange(evaluationsUrl() + "/" + taskId, HttpMethod.GET,
                    new HttpEntity<>(headers(requestId)), EvaluationTaskResponse.class).getBody();
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw exception(YAYA_AI_UNAUTHORIZED);
        } catch (RestClientException ex) {
            throw exception(YAYA_AI_REQUEST_FAILED);
        }
    }

    private HttpHeaders headers(String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HEADER_INTERNAL_KEY, properties.getInternalKey());
        headers.set(HEADER_REQUEST_ID, StrUtil.blankToDefault(requestId, UUID.randomUUID().toString()));
        return headers;
    }

    private String evaluationsUrl() {
        return StrUtil.removeSuffix(properties.getBaseUrl(), "/") + "/internal/evaluations";
    }

    @Data
    @Accessors(chain = true)
    public static class EvaluationCreateRequest {

        @JsonProperty("task_id")
        private String taskId;
        @JsonProperty("evaluation_id")
        private String evaluationId;
        @JsonProperty("recording_id")
        private String recordingId;
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("topic_id")
        private String topicId;
        private AudioPayload audio;
        private TopicPayload topic;
        private Map<String, Object> options;

    }

    @Data
    @Accessors(chain = true)
    public static class AudioPayload {

        @JsonProperty("storage_path")
        private String storagePath;
        @JsonProperty("mime_type")
        private String mimeType;
        @JsonProperty("duration_seconds")
        private Double durationSeconds;

    }

    @Data
    @Accessors(chain = true)
    public static class TopicPayload {

        @JsonProperty("title_en")
        private String titleEn;
        @JsonProperty("title_zh")
        private String titleZh;
        @JsonProperty("prompt_en")
        private String promptEn;
        @JsonProperty("prompt_zh")
        private String promptZh;
        @JsonProperty("cue_bullets")
        private List<String> cueBullets;

    }

    @Data
    @Accessors(chain = true)
    public static class EvaluationAcceptedResponse {

        @JsonProperty("task_id")
        private String taskId;
        private String status;
        private Boolean accepted;

    }

    @Data
    @Accessors(chain = true)
    public static class EvaluationTaskResponse {

        @JsonProperty("task_id")
        private String taskId;
        @JsonProperty("evaluation_id")
        private String evaluationId;
        private String status;
        private Map<String, Object> progress;
        private Map<String, Object> result;
        private Map<String, Object> error;

    }

}
