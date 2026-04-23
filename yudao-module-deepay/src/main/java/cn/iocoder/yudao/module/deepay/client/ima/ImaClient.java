package cn.iocoder.yudao.module.deepay.client.ima;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * ima 知识库 HTTP 客户端。
 */
public class ImaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final HttpHeaders defaultHeaders;

    public ImaClient(String baseUrl, String apiKey) {
        this.baseUrl       = baseUrl;
        this.restTemplate  = new RestTemplate();
        this.defaultHeaders = new HttpHeaders();
        this.defaultHeaders.setBearerAuth(apiKey);
        this.defaultHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    public String createKnowledgeBase(String name, String description) {
        CreateKbRequest request = new CreateKbRequest();
        request.setName(name);
        request.setDescription(description);

        HttpEntity<CreateKbRequest> entity = new HttpEntity<>(request, defaultHeaders);
        CreateKbResponse response = restTemplate.postForObject(
                baseUrl + "/openapi/v1/knowledge-base", entity, CreateKbResponse.class);

        if (response == null || response.getId() == null) {
            throw new IllegalStateException("ima 返回的知识库 ID 为空，name=" + name);
        }
        return response.getId();
    }

    public void uploadImage(String kbId, String imageUrl) {
        UploadDocumentRequest request = new UploadDocumentRequest();
        request.setUrl(imageUrl);
        request.setType("image");

        HttpEntity<UploadDocumentRequest> entity = new HttpEntity<>(request, defaultHeaders);
        restTemplate.postForObject(
                baseUrl + "/openapi/v1/knowledge-base/" + kbId + "/document",
                entity, Void.class);
    }

    // -------------------- 请求 / 响应 VO --------------------

    @Data
    public static class CreateKbRequest {
        @JsonProperty("name")        private String name;
        @JsonProperty("description") private String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateKbResponse {
        @JsonProperty("id") private String id;
    }

    @Data
    public static class UploadDocumentRequest {
        @JsonProperty("url")  private String url;
        @JsonProperty("type") private String type;
    }

}
