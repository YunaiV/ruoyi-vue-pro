package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpMethod;

import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AiVideoHttpClient}、{@link AiVideoUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class AiVideoHttpClientTest {

    @TempDir
    Path tempDir;

    @Test
    public void testExecute_non2xxReturned() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(400, "{\"error\":\"bad\"}");
            AiVideoHttpClient client = new AiVideoHttpClient("test", Duration.ofSeconds(5), 0);

            AiVideoHttpClient.HttpResult result = client.execute(HttpMethod.POST, URI.create(server.url() + "/x"),
                    Collections.singletonMap("Authorization", "Bearer k"), "{}".getBytes(StandardCharsets.UTF_8), "提交", true);

            assertEquals(400, result.getStatus());
            assertEquals("{\"error\":\"bad\"}", new String(result.getBody(), StandardCharsets.UTF_8));
            assertEquals("Bearer k", server.lastRequest().header("Authorization"));
        }
    }

    @Test
    public void testExecute_submitTimeoutIsUnknownAndNotRetried() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            AiVideoHttpClient client = new AiVideoHttpClient("test", Duration.ofMillis(500), 0, "secret-key");

            assertThatThrownBy(() -> client.execute(HttpMethod.POST, URI.create(server.url() + "/x"), null,
                    new byte[0], "提交", true))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                        assertTrue(ex.isSubmitOutcomeUnknown());
                    });
            Thread.sleep(200);
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testExecute_queryTimeoutIsNotSubmitUnknown() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            AiVideoHttpClient client = new AiVideoHttpClient("test", Duration.ofMillis(500), 0);

            assertThatThrownBy(() -> client.execute(HttpMethod.GET, URI.create(server.url() + "/x"), null, null, "查询", false))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                        assertFalse(ex.isSubmitOutcomeUnknown());
                    });
        }
    }

    @Test
    public void testExecute_responseTooLarge() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            byte[] big = new byte[4096];
            server.enqueue(new VideoMockServer.Response(200, big));
            AiVideoHttpClient client = new AiVideoHttpClient("test", Duration.ofSeconds(5), 1024);

            assertThatThrownBy(() -> client.execute(HttpMethod.GET, URI.create(server.url() + "/x"), null, null, "查询", false))
                    .isInstanceOfSatisfying(AiVideoException.class,
                            ex -> assertEquals(AiVideoException.Kind.INVALID_RESPONSE, ex.getKind()));
        }
    }

    @Test
    public void testExecute_connectionRefusedIsKnownNotSent() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        AiVideoHttpClient client = new AiVideoHttpClient("test", Duration.ofSeconds(5), 0);
        assertThatThrownBy(() -> client.execute(HttpMethod.POST, URI.create("http://127.0.0.1:" + port + "/x"), null,
                new byte[0], "提交", true))
                .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                    assertEquals(AiVideoException.Kind.NETWORK, ex.getKind());
                    // 连接被拒绝：请求未发出，可以安全重试
                    assertFalse(ex.isSubmitOutcomeUnknown());
                });
    }

    @Test
    public void testDownloadToFile() throws Exception {
        byte[] mp4 = new byte[]{0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(new VideoMockServer.Response(200, mp4));
            server.enqueue(new VideoMockServer.Response(200, new byte[2048]));
            Path target = tempDir.resolve("a.mp4");

            long size = AiVideoHttpClient.downloadToFile(server.url() + "/v.mp4?sign=abc", target, 1024, Duration.ofSeconds(5));
            assertEquals(mp4.length, size);
            assertTrue(AiVideoUtils.isMp4(Files.readAllBytes(target)));
            // 下载不带任何鉴权头
            assertThat(server.lastRequest().header("Authorization")).isNull();

            // 超过上限
            assertThatThrownBy(() -> AiVideoHttpClient.downloadToFile(server.url() + "/big.mp4", tempDir.resolve("b.mp4"),
                    1024, Duration.ofSeconds(5))).isInstanceOf(IllegalStateException.class).hasMessageContaining("上限");
        }
        // 只允许 http(s)
        assertThatThrownBy(() -> AiVideoHttpClient.downloadToFile("file:///etc/passwd", tempDir.resolve("c.mp4"),
                1024, Duration.ofSeconds(5))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSanitize() {
        String text = "key sk-abc123 Bearer xyz.123 https://a.com/v.mp4?X-Tos-Signature=1&e=2 "
                + "data:image/png;base64,AAAAAAAAAAAAAAAAAAAAAAAAAAAA Signature=deadbeef ya29.a0AfB_token\n";
        String result = AiVideoUtils.sanitize(text, "sk-abc123");
        assertThat(result).doesNotContain("sk-abc123", "xyz.123", "X-Tos-Signature", "AAAAAAAAAAAAAAAA", "deadbeef",
                "a0AfB_token", "\n");
        assertThat(result).contains("https://a.com/v.mp4?***");
        assertEquals("https://a.com/v.mp4?***", AiVideoUtils.maskUrl("https://a.com/v.mp4?sign=1"));
    }

    @Test
    public void testTaskResult_successWithoutOutputIsFailure() {
        AiVideoTaskResult result = new AiVideoTaskResult();
        result.succeedOrFailIfEmpty();
        assertEquals(AiVideoTaskStatus.FAILED, result.getStatus());
        assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, result.getErrorCode());
        assertFalse(AiVideoTaskStatus.UNKNOWN.isFinished());
    }

    @Test
    public void testCheckMediaUrl() {
        assertEquals("https://a.com/a.png", AiVideoUtils.checkMediaUrl("https://a.com/a.png", "图片"));
        assertThatThrownBy(() -> AiVideoUtils.checkMediaUrl("ftp://a.com/a.png", "图片"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AiVideoUtils.checkMediaUrl("file:///etc/passwd", "图片"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
