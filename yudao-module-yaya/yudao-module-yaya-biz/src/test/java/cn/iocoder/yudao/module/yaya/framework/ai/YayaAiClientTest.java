package cn.iocoder.yudao.module.yaya.framework.ai;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_AI_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;

class YayaAiClientTest {

    private HttpServer server;
    private CapturedRequest capturedRequest;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createEvaluationShouldSendInternalHeadersAndMapAcceptedResponse() throws IOException {
        startServer(200, """
                {"task_id":"task-1","status":"PENDING","accepted":true}
                """);
        YayaAiClient client = client();

        YayaAiClient.EvaluationAcceptedResponse response = client.createEvaluation(
                new YayaAiClient.EvaluationCreateRequest()
                        .setTaskId("task-1")
                        .setEvaluationId("evaluation-1")
                        .setRecordingId("recording-1")
                        .setUserId("10001")
                        .setTopicId("part2-family-home")
                        .setAudio(new YayaAiClient.AudioPayload()
                                .setStoragePath("recordings/10001/a.webm")
                                .setMimeType("audio/webm")
                                .setDurationSeconds(12.4))
                        .setTopic(new YayaAiClient.TopicPayload()
                                .setTitleEn("Describe a room you like")
                                .setCueBullets(List.of("where it is")))
                        .setOptions(Map.of("run_text_route", true)),
                null);

        assertEquals("task-1", response.getTaskId());
        assertEquals("PENDING", response.getStatus());
        assertTrue(response.getAccepted());
        assertEquals("secret", capturedRequest.internalKey);
        assertNotNull(capturedRequest.requestId);
        assertFalse(capturedRequest.requestId.isBlank());
        assertEquals("/internal/evaluations", capturedRequest.path);
        assertTrue(capturedRequest.body.contains("\"task_id\":\"task-1\""));
        assertTrue(capturedRequest.body.contains("\"recording_id\":\"recording-1\""));
    }

    @Test
    void createEvaluationShouldMapUnauthorizedResponse() throws IOException {
        startServer(401, """
                {"detail":"invalid X-Yaya-Internal-Key"}
                """);
        YayaAiClient client = client();

        ServiceException exception = assertThrows(ServiceException.class,
                () -> client.createEvaluation(new YayaAiClient.EvaluationCreateRequest()
                        .setTaskId("task-401")
                        .setEvaluationId("evaluation-401"), "request-401"));

        assertEquals(YAYA_AI_UNAUTHORIZED.getCode(), exception.getCode());
        assertEquals("request-401", capturedRequest.requestId);
    }

    private YayaAiClient client() {
        YayaAiProperties properties = new YayaAiProperties()
                .setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort())
                .setInternalKey("secret")
                .setTimeoutSeconds(3);
        return new YayaAiClient(new RestTemplate(), properties);
    }

    private void startServer(int status, String responseBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal/evaluations", exchange -> respond(exchange, status, responseBody));
        server.start();
    }

    private void respond(HttpExchange exchange, int status, String responseBody) throws IOException {
        capturedRequest = new CapturedRequest(
                exchange.getRequestURI().getPath(),
                exchange.getRequestHeaders().getFirst("X-Yaya-Internal-Key"),
                exchange.getRequestHeaders().getFirst("X-Yaya-Request-Id"),
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private record CapturedRequest(String path, String internalKey, String requestId, String body) {
    }

}
