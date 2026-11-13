package cn.iocoder.yudao.module.ai.framework.ai.core.model.speech;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiSpeechUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class AiSpeechUtilsTest {

    private static final BigDecimal MIN = new BigDecimal("0.5");
    private static final BigDecimal MAX = new BigDecimal("2");

    @Test
    public void testValidateSpeed() {
        assertEquals(0, BigDecimal.ONE.compareTo(AiSpeechUtils.validateSpeed(null, MIN, MAX)));
        assertEquals(new BigDecimal("1.25"), AiSpeechUtils.validateSpeed(1.25, MIN, MAX));
        assertEquals(new BigDecimal("1.2"), AiSpeechUtils.validateSpeed(1.2, MIN, MAX));
        assertEquals(new BigDecimal("2"), AiSpeechUtils.validateSpeed(2.0, MIN, MAX));
        assertEquals(new BigDecimal("0.5"), AiSpeechUtils.validateSpeed(0.5, MIN, MAX));
        // 超过两位小数、浮点误差：拒绝，不静默截断
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateSpeed(1.234, MIN, MAX));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateSpeed(0.1 + 1.1, MIN, MAX));
        // 超出范围：拒绝，不夹到边界
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateSpeed(0.49, MIN, MAX));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateSpeed(2.01, MIN, MAX));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateSpeed(Double.NaN, MIN, MAX));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateSpeed(Double.POSITIVE_INFINITY, MIN, MAX));
        // 只允许 1 的模型
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AiSpeechUtils.validateSpeed(1.2, BigDecimal.ONE, BigDecimal.ONE));
        assertTrue(ex.getMessage().contains("不支持调节语速"));
        assertEquals(0, BigDecimal.ONE.compareTo(AiSpeechUtils.validateSpeed(1.0, BigDecimal.ONE, BigDecimal.ONE)));
    }

    @Test
    public void testValidateFormatAndModelAndText() {
        assertEquals("mp3", AiSpeechUtils.validateFormat(null, "mp3", Arrays.asList("mp3", "wav")));
        assertEquals("wav", AiSpeechUtils.validateFormat("wav", "mp3", Arrays.asList("mp3", "wav")));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateFormat("pcm", "mp3", Arrays.asList("mp3", "wav")));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateFormat("MP3", "mp3", Arrays.asList("mp3", "wav")));

        AiSpeechUtils.validateModel("speech-2.8-hd", null);
        AiSpeechUtils.validateModel("speech-2.8-hd", "speech-2.8-hd");
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateModel("speech-2.8-hd", "speech-2.8-turbo"));

        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateText(" ", 10));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateText(null, 10));
        // 按码点计数：emoji 是 2 个 char、1 个码点
        assertEquals("😀😀", AiSpeechUtils.validateText("😀😀", 2));
        assertThrows(IllegalArgumentException.class, () -> AiSpeechUtils.validateText("😀😀😀", 2));
    }

    @Test
    public void testIsAudioFormat() {
        assertTrue(AiSpeechUtils.isAudioFormat(SpeechMockServer.mp3(16), "mp3"));
        assertTrue(AiSpeechUtils.isAudioFormat("ID3\u0004xxxx".getBytes(StandardCharsets.ISO_8859_1), "mp3"));
        assertTrue(AiSpeechUtils.isAudioFormat(SpeechMockServer.wav(16), "wav"));
        assertTrue(AiSpeechUtils.isAudioFormat("fLaC1234".getBytes(StandardCharsets.US_ASCII), "flac"));
        assertTrue(AiSpeechUtils.isAudioFormat("OggS1234".getBytes(StandardCharsets.US_ASCII), "ogg_opus"));
        assertFalse(AiSpeechUtils.isAudioFormat(SpeechMockServer.wav(16), "mp3"));
        assertFalse(AiSpeechUtils.isAudioFormat("{\"code\":1}".getBytes(StandardCharsets.UTF_8), "mp3"));
        assertFalse(AiSpeechUtils.isAudioFormat(new byte[]{'R', 'I', 'F', 'F'}, "wav"));
        assertThrows(IllegalStateException.class, () -> AiSpeechUtils.validateAudio(new byte[0], "mp3"));
        assertThrows(IllegalStateException.class, () -> AiSpeechUtils.validateAudio(null, "mp3"));

        assertEquals("opus", AiSpeechUtils.getFileExtension("ogg_opus"));
        assertEquals("mp3", AiSpeechUtils.getFileExtension("mp3"));
    }

    @Test
    public void testSanitize() {
        String text = "bad key sk-secret-123 url https://oss.example.com/a.wav?Expires=1&Signature=abc"
                + " header Authorization: Bearer token-xyz\r\nnext";
        String result = AiSpeechUtils.sanitize(text, "sk-secret-123");
        assertFalse(result.contains("sk-secret-123"));
        assertFalse(result.contains("Signature=abc"));
        assertFalse(result.contains("token-xyz"));
        assertFalse(result.contains("\n"));
        assertTrue(result.contains("https://oss.example.com/a.wav?***"));
        assertTrue(AiSpeechUtils.sanitize(StrUtil.repeat("x", 1000)).length() <= 203);
        assertEquals("", AiSpeechUtils.sanitize(null));
    }

    @Test
    public void testDownload_success() throws Exception {
        byte[] wav = SpeechMockServer.wav(64);
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeResponse(out, 200, "audio/wav", wav))) {
            byte[] result = AiSpeechUtils.download(server.url() + "/a.wav?Signature=s", "wav", Duration.ofSeconds(5), 1024);
            assertArrayEquals(wav, result);
            // 下载临时地址不携带任何鉴权头
            assertNull(server.requests().get(0).header("Authorization"));
            assertEquals("Signature=s", server.requests().get(0).query());
        }
    }

    @Test
    public void testDownload_errors() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            switch (request.path()) {
                case "/403":
                    SpeechMockServer.writeResponse(out, 403, "text/plain", "denied".getBytes(StandardCharsets.UTF_8));
                    break;
                case "/big":
                    SpeechMockServer.writeResponse(out, 200, "audio/wav", SpeechMockServer.wav(2048));
                    break;
                case "/json":
                    SpeechMockServer.writeJson(out, 200, "{\"error\":\"x\"}");
                    break;
                default:
                    Thread.sleep(3000);
                    break;
            }
        })) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> AiSpeechUtils.download(server.url() + "/403?Signature=secret-sign", "wav", Duration.ofSeconds(5), 1024));
            assertTrue(ex.getMessage().contains("403"));
            assertFalse(ex.getMessage().contains("secret-sign"));
            ex = assertThrows(IllegalStateException.class,
                    () -> AiSpeechUtils.download(server.url() + "/big", "wav", Duration.ofSeconds(5), 1024));
            assertTrue(ex.getMessage().contains("大小上限"));
            ex = assertThrows(IllegalStateException.class,
                    () -> AiSpeechUtils.download(server.url() + "/json", "wav", Duration.ofSeconds(5), 1024));
            assertTrue(ex.getMessage().contains("不是 wav 格式"));
            ex = assertThrows(IllegalStateException.class,
                    () -> AiSpeechUtils.download(server.url() + "/slow", "wav", Duration.ofMillis(300), 1024));
            assertTrue(ex.getMessage().contains("超时"));
        }
        assertThrows(IllegalStateException.class, () -> AiSpeechUtils.download("file:///etc/hosts", "wav", Duration.ofSeconds(1), 1024));
        assertThrows(IllegalStateException.class, () -> AiSpeechUtils.download("ftp://example.com/a.wav", "wav", Duration.ofSeconds(1), 1024));
    }

    // ========== 传输层 ==========

    @Test
    public void testBuildWebClient_activeConnectorNoRetryNoRedirect() {
        // 检查 WebClient 实际使用的连接器：Reactor Netty，且关闭了"TCP 中断后重发一次"、不跟随重定向
        WebClient client = AiSpeechUtils.buildWebClient("http://127.0.0.1:1", 1024, null);
        Object exchangeFunction = ReflectionTestUtils.getField(client, "exchangeFunction");
        Object connector = ReflectionTestUtils.getField(exchangeFunction, "connector");
        ReactorClientHttpConnector reactorConnector = assertInstanceOf(ReactorClientHttpConnector.class, connector);
        HttpClient httpClient = (HttpClient) ReflectionTestUtils.getField(reactorConnector, "httpClient");
        assertNotNull(httpClient);
        assertTrue(httpClient.configuration().isRetryDisabled());
        assertNull(httpClient.configuration().followRedirectPredicate());
        // 对照：Reactor Netty 默认开启重发
        assertFalse(HttpClient.create().configuration().isRetryDisabled());
    }

    @Test
    public void testExchange_redirectNotFollowed() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeResponse(out, 307, "text/plain", new byte[0], "Location", "/other"))) {
            WebClient client = AiSpeechUtils.buildWebClient(server.url(), 1024, null);
            AiSpeechUtils.HttpResult result = AiSpeechUtils.exchange(client.post().uri("/paid").bodyValue("{}"),
                    Duration.ofSeconds(10), "测试");
            assertEquals(307, result.getStatus());
            Thread.sleep(200);
            assertEquals(1, server.requests().size());
            assertEquals("/paid", server.requests().get(0).path());
        }
    }

    // ========== live 冒烟的本地保护（不发起任何付费调用） ==========

    @Test
    public void testLiveSmoke_prepareOutputDir(@TempDir Path temp) throws Exception {
        // 未配置、相对路径：拒绝
        assertThrows(IllegalStateException.class, () -> AiSpeechLiveSmokeTests.prepareOutputDir(null));
        assertThrows(IllegalStateException.class, () -> AiSpeechLiveSmokeTests.prepareOutputDir("runtime/live"));
        // 合法：仓库之外，不存在时创建，返回真实路径
        Path ok = AiSpeechLiveSmokeTests.prepareOutputDir(temp.resolve("live/a").toString());
        assertTrue(Files.isDirectory(ok));
        assertEquals(temp.resolve("live/a").toRealPath(), ok);
        try (Stream<Path> files = Files.list(ok)) {
            assertEquals(0, files.count(), "可写探测文件必须删除");
        }
        // 位于 Git 仓库内（.git 目录）：拒绝，且不创建目录
        Path repo = Files.createDirectories(temp.resolve("repo"));
        Files.createDirectories(repo.resolve(".git"));
        assertThrows(IllegalStateException.class,
                () -> AiSpeechLiveSmokeTests.prepareOutputDir(repo.resolve("output/live").toString()));
        assertFalse(Files.exists(repo.resolve("output")));
        // 位于 Git worktree 内（.git 文件）：拒绝
        Path worktree = Files.createDirectories(temp.resolve("worktree"));
        Files.write(worktree.resolve(".git"), "gitdir: /elsewhere".getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalStateException.class,
                () -> AiSpeechLiveSmokeTests.prepareOutputDir(worktree.resolve("live").toString()));
        // 通过符号链接指向仓库内：拒绝，且不创建目录
        Path link = Files.createSymbolicLink(temp.resolve("link"), repo);
        assertThrows(IllegalStateException.class,
                () -> AiSpeechLiveSmokeTests.prepareOutputDir(link.resolve("live").toString()));
        assertFalse(Files.exists(repo.resolve("live")));
        // 不可写：拒绝
        Path readOnly = Files.createDirectories(temp.resolve("readonly"));
        Files.setPosixFilePermissions(readOnly, PosixFilePermissions.fromString("r-xr-xr-x"));
        try {
            assertThrows(IOException.class, () -> AiSpeechLiveSmokeTests.prepareOutputDir(readOnly.toString()));
        } finally {
            Files.setPosixFilePermissions(readOnly, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
        // 是文件而不是目录：拒绝
        Path file = Files.write(temp.resolve("file"), "x".getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> AiSpeechLiveSmokeTests.prepareOutputDir(file.toString()));
    }

    @Test
    public void testLiveSmoke_writeTencentTaskRecord(@TempDir Path temp) throws Exception {
        Path file = AiSpeechLiveSmokeTests.writeTencentTaskRecord(temp, "task-123", "req-1", "mp3", "SUBMITTED", null);
        assertEquals(temp.resolve("tencent-task-task-123.json"), file);
        JsonNode json = AiSpeechUtils.parseJson(Files.readAllBytes(file));
        assertEquals("task-123", json.get("taskId").asText());
        assertEquals("req-1", json.get("requestId").asText());
        assertEquals("SUBMITTED", json.get("status").asText());
        assertEquals("AI_SPEECH_TENCENT_TASK_ID=task-123", json.get("resume").asText());
        // 只记录编号与状态：不含凭证、结果地址；为空的字段（errorMessage）不写入
        List<String> fields = new ArrayList<>();
        json.fieldNames().forEachRemaining(fields::add);
        assertEquals(Arrays.asList("provider", "taskId", "requestId", "format", "status", "updatedAt", "resume"), fields);
        assertFalse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).contains("http"));
        // 同一任务再次写入：原子替换，不残留临时文件；任务编号中的路径字符被替换
        AiSpeechLiveSmokeTests.writeTencentTaskRecord(temp, "task-123", "req-2", "mp3", "SUCCESS", null);
        assertEquals("SUCCESS", AiSpeechUtils.parseJson(Files.readAllBytes(file)).get("status").asText());
        Path odd = AiSpeechLiveSmokeTests.writeTencentTaskRecord(temp, "../x", null, "mp3", "RESUMED", null);
        assertEquals(temp, odd.getParent());
        try (Stream<Path> files = Files.list(temp)) {
            assertEquals(2, files.count());
        }
    }

}
