package cn.iocoder.yudao.module.ai.framework.ai.core.model.speech;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.baidu.BaiduSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax.MiniMaxSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi.TongYiSpeechModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 语音合成真实调用的冒烟测试：会产生服务商费用，默认不执行
 *
 * 启用方式：环境变量 YUDAO_AI_SPEECH_LIVE=true，且 AI_SPEECH_LIVE_OUTPUT_DIR 为仓库之外的本地目录；
 * 每个平台只在配置了自己的凭证时执行，每次只发一个最小示例，不自动重试付费请求。
 * 付费调用之前先校验输出目录（绝对路径、不在任何 Git 仓库内、可写），校验失败不发请求；
 * 腾讯云任务受理后立即把 taskId 写入输出目录的 tencent-task-*.json（不含凭证与结果地址），中断后可按编号继续查询。
 *
 * 凭证只从环境变量读取（XXX，或 XXX_FILE 指向仓库之外的文件），不写入代码、资源、日志；
 * 输出只打印格式、大小与排查编号，音频保存到 AI_SPEECH_LIVE_OUTPUT_DIR
 *
 * | 平台 | 凭证 | 其它参数 |
 * | 通义 | AI_SPEECH_TONGYI_API_KEY | AI_SPEECH_TONGYI_VOICE（默认 Cherry）、AI_SPEECH_TONGYI_URL |
 * | MiniMax | AI_SPEECH_MINIMAX_API_KEY | AI_SPEECH_MINIMAX_VOICE（必填）、AI_SPEECH_MINIMAX_MODEL（默认 speech-2.8-hd）、AI_SPEECH_MINIMAX_URL |
 * | 豆包 | AI_SPEECH_DOUBAO_API_KEY | AI_SPEECH_DOUBAO_SPEAKER（必填）、AI_SPEECH_DOUBAO_RESOURCE_ID（默认 seed-tts-2.0）、AI_SPEECH_DOUBAO_URL |
 * | 腾讯云 | AI_SPEECH_TENCENT_CREDENTIAL（SecretId|SecretKey） | AI_SPEECH_TENCENT_VOICE_TYPE（必填）、AI_SPEECH_TENCENT_TASK_ID（只轮询已提交的任务）、
 *            AI_SPEECH_TENCENT_POLL_SECONDS（默认 5）、AI_SPEECH_TENCENT_MAX_WAIT_SECONDS（默认 900） |
 * | 百度 | AI_SPEECH_BAIDU_API_KEY | AI_SPEECH_BAIDU_VOICE（必填）、AI_SPEECH_BAIDU_URL |
 *
 * @author 芋道源码
 */
@EnabledIfEnvironmentVariable(named = "YUDAO_AI_SPEECH_LIVE", matches = "true")
public class AiSpeechLiveSmokeTests {

    private static final String SHORT_TEXT = "你好，这是芋道语音合成的接入测试。";
    /**
     * 腾讯云长文本：超过 150 字，验证原生异步任务不在客户端切分
     */
    private static final String LONG_TEXT = "芋道语音合成接入测试。这段文字用于验证腾讯云长文本语音合成的异步任务："
            + "调用方只提交一次任务并保存任务编号，之后按编号查询任务状态，直到任务成功或失败。"
            + "服务端负责长文本的分句与合成，调用方不在本地切分文本，也不拼接多段音频。"
            + "任务成功后，调用方在结果地址的有效期内下载音频，并校验音频格式与大小，不保存临时地址。"
            + "如果提交时出现网络异常，任务是否已创建是未知的，调用方不能自动重新提交，以免重复计费。";

    @Test
    public void testTongYi() throws Exception {
        String apiKey = secret("AI_SPEECH_TONGYI_API_KEY");
        assumeTrue(apiKey != null, "未配置 AI_SPEECH_TONGYI_API_KEY");
        Path dir = outputDir();
        TextToSpeechModel model = new TongYiSpeechModel(env("AI_SPEECH_TONGYI_URL"), apiKey, "qwen3-tts-flash");
        TextToSpeechResponse response = model.call(new TextToSpeechPrompt(SHORT_TEXT, TextToSpeechOptions.builder()
                .voice(StrUtil.blankToDefault(env("AI_SPEECH_TONGYI_VOICE"), "Cherry")).build()));
        save(dir, "tongyi", response);
    }

    @Test
    public void testMiniMax() throws Exception {
        String apiKey = secret("AI_SPEECH_MINIMAX_API_KEY");
        assumeTrue(apiKey != null, "未配置 AI_SPEECH_MINIMAX_API_KEY");
        Path dir = outputDir();
        String voice = required("AI_SPEECH_MINIMAX_VOICE");
        TextToSpeechModel model = new MiniMaxSpeechModel(env("AI_SPEECH_MINIMAX_URL"), apiKey,
                StrUtil.blankToDefault(env("AI_SPEECH_MINIMAX_MODEL"), "speech-2.8-hd"));
        TextToSpeechResponse response = model.call(new TextToSpeechPrompt(SHORT_TEXT, TextToSpeechOptions.builder()
                .voice(voice).format("mp3").speed(1.2).build()));
        save(dir, "minimax", response);
    }

    @Test
    public void testDouBao() throws Exception {
        String apiKey = secret("AI_SPEECH_DOUBAO_API_KEY");
        assumeTrue(apiKey != null, "未配置 AI_SPEECH_DOUBAO_API_KEY");
        Path dir = outputDir();
        String speaker = required("AI_SPEECH_DOUBAO_SPEAKER");
        TextToSpeechModel model = new DouBaoSpeechModel(env("AI_SPEECH_DOUBAO_URL"), apiKey,
                StrUtil.blankToDefault(env("AI_SPEECH_DOUBAO_RESOURCE_ID"), "seed-tts-2.0"));
        TextToSpeechResponse response = model.call(new TextToSpeechPrompt(SHORT_TEXT, TextToSpeechOptions.builder()
                .voice(speaker).format("mp3").speed(1.2).build()));
        save(dir, "doubao", response);
    }

    @Test
    public void testBaidu() throws Exception {
        String apiKey = secret("AI_SPEECH_BAIDU_API_KEY");
        assumeTrue(apiKey != null, "未配置 AI_SPEECH_BAIDU_API_KEY");
        Path dir = outputDir();
        String voice = required("AI_SPEECH_BAIDU_VOICE");
        TextToSpeechModel model = new BaiduSpeechModel(env("AI_SPEECH_BAIDU_URL"), apiKey, "baidu-tts");
        TextToSpeechResponse response = model.call(new TextToSpeechPrompt(SHORT_TEXT, TextToSpeechOptions.builder()
                .voice(voice).format("mp3").speed(1.2).build()));
        save(dir, "baidu", response);
    }

    @Test
    public void testTencent() throws Exception {
        String credential = secret("AI_SPEECH_TENCENT_CREDENTIAL");
        assumeTrue(credential != null, "未配置 AI_SPEECH_TENCENT_CREDENTIAL");
        // 0. 付费提交之前：输出目录可写、轮询参数合法
        Path dir = outputDir();
        long pollSeconds = Long.parseLong(StrUtil.blankToDefault(env("AI_SPEECH_TENCENT_POLL_SECONDS"), "5"));
        long maxWaitSeconds = Long.parseLong(StrUtil.blankToDefault(env("AI_SPEECH_TENCENT_MAX_WAIT_SECONDS"), "900"));
        assertTrue(pollSeconds > 0 && maxWaitSeconds > 0, "轮询参数必须为正数");
        TencentTtsApi api = new TencentTtsApi(credential, env("AI_SPEECH_TENCENT_URL"));
        String format = "mp3";
        // 1. 提交一次；已配置任务编号时只轮询，不重复提交
        String taskId = env("AI_SPEECH_TENCENT_TASK_ID");
        if (StrUtil.isBlank(taskId)) {
            String voiceType = required("AI_SPEECH_TENCENT_VOICE_TYPE");
            assertTrue(LONG_TEXT.codePointCount(0, LONG_TEXT.length()) > 150);
            TencentTtsApi.TtsTask task = api.createTask(LONG_TEXT, voiceType, format, 1.2);
            taskId = task.getTaskId();
            System.out.println("[tencent] 已提交任务 taskId=" + taskId + "，requestId=" + task.getRequestId()
                    + "（如需继续轮询，设置 AI_SPEECH_TENCENT_TASK_ID）");
            // 受理后立即落盘，中断后可按编号继续查询，不重新提交
            Path record = writeTencentTaskRecord(dir, taskId, task.getRequestId(), format, "SUBMITTED", null);
            System.out.println("[tencent] 任务记录 " + record);
        } else {
            writeTencentTaskRecord(dir, taskId, null, format, "RESUMED", null);
        }
        // 2. 按编号有界轮询：查询不计费，可以重复；提交不重复
        long deadline = System.nanoTime() + Duration.ofSeconds(maxWaitSeconds).toNanos();
        TencentTtsApi.TtsTaskResult result = null;
        while (System.nanoTime() < deadline) {
            try {
                result = api.getTask(taskId, format);
                System.out.println("[tencent] taskId=" + taskId + "，status=" + result.getStatus());
                if (result.getStatus().isFinished()) {
                    break;
                }
            } catch (IllegalStateException ex) {
                System.out.println("[tencent] 查询失败，稍后重试查询：" + ex.getMessage());
            }
            Thread.sleep(Duration.ofSeconds(pollSeconds).toMillis());
        }
        assertNotNull(result, "轮询超时，taskId=" + taskId);
        writeTencentTaskRecord(dir, taskId, result.getRequestId(), format, result.getStatus().name(),
                StrUtil.maxLength(result.getErrorMessage(), 200));
        assertEquals(TencentTtsApi.TtsTaskStatus.SUCCESS, result.getStatus(),
                "任务未成功，taskId=" + taskId + "，原因：" + result.getErrorMessage());
        save(dir, "tencent", format, result.getAudio(), "taskId=" + taskId + "，requestId=" + result.getRequestId());
    }

    // ========== 工具方法 ==========

    private static Path outputDir() throws IOException {
        return prepareOutputDir(env("AI_SPEECH_LIVE_OUTPUT_DIR"));
    }

    /**
     * 付费调用之前校验输出目录：绝对路径、不在任何 Git 仓库（含 worktree）内、可写；不存在时创建
     *
     * @return 解析符号链接后的真实目录
     */
    static Path prepareOutputDir(String value) throws IOException {
        if (StrUtil.isBlank(value)) {
            throw new IllegalStateException("未配置 AI_SPEECH_LIVE_OUTPUT_DIR：付费调用之前必须指定仓库之外的输出目录");
        }
        Path path = Paths.get(value);
        if (!path.isAbsolute()) {
            throw new IllegalStateException("AI_SPEECH_LIVE_OUTPUT_DIR 必须是绝对路径");
        }
        // 创建之前：按字面路径、以及最近一个已存在祖先的真实路径检查，避免在仓库内创建目录
        assertOutsideGitRepository(path.normalize());
        Path existing = path.normalize();
        while (existing != null && !Files.exists(existing)) {
            existing = existing.getParent();
        }
        if (existing != null) {
            assertOutsideGitRepository(existing.toRealPath());
        }
        Files.createDirectories(path);
        Path real = path.toRealPath();
        if (!Files.isDirectory(real)) {
            throw new IllegalStateException("AI_SPEECH_LIVE_OUTPUT_DIR 不是目录：" + real);
        }
        assertOutsideGitRepository(real);
        // 实际写入并删除一个探测文件，确认可写
        Path probe = Files.createTempFile(real, ".write-check-", ".tmp");
        Files.delete(probe);
        return real;
    }

    private static void assertOutsideGitRepository(Path path) {
        for (Path current = path; current != null; current = current.getParent()) {
            if (Files.exists(current.resolve(".git"), LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalStateException("AI_SPEECH_LIVE_OUTPUT_DIR 不能位于 Git 仓库内：" + current);
            }
        }
    }

    /**
     * 写入腾讯云任务记录（先写临时文件再原子替换）：只含任务编号、请求编号、格式、状态，不含凭证与结果地址
     */
    static Path writeTencentTaskRecord(Path dir, String taskId, String requestId, String format,
                                       String status, String errorMessage) throws IOException {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("provider", "TencentCloud");
        record.put("taskId", taskId);
        record.put("requestId", requestId);
        record.put("format", format);
        record.put("status", status);
        record.put("errorMessage", errorMessage);
        record.put("updatedAt", OffsetDateTime.now().toString());
        record.put("resume", "AI_SPEECH_TENCENT_TASK_ID=" + taskId);
        Path file = dir.resolve("tencent-task-" + taskId.replaceAll("[^A-Za-z0-9_-]", "_") + ".json");
        Path temp = Files.createTempFile(dir, ".tencent-task-", ".tmp");
        try {
            Files.write(temp, JsonUtils.toJsonByte(record));
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
        return file;
    }

    private static void save(Path dir, String provider, TextToSpeechResponse response) throws IOException {
        String format = response.getMetadata().get(AbstractAiSpeechModel.METADATA_FORMAT);
        StringBuilder diagnostics = new StringBuilder();
        response.getMetadata().entrySet().forEach(entry -> diagnostics.append(entry.getKey()).append('=')
                .append(entry.getValue()).append(' '));
        save(dir, provider, format, response.getResult().getOutput(), diagnostics.toString().trim());
    }

    private static void save(Path dir, String provider, String format, byte[] audio, String diagnostics) throws IOException {
        assertNotNull(audio);
        assertTrue(audio.length > 1024, "音频过小：" + audio.length);
        assertTrue(AiSpeechUtils.isAudioFormat(audio, format), "音频文件头与格式不一致：" + format);
        if (AiSpeechUtils.FORMAT_WAV.equals(format)) {
            try {
                AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(audio));
                System.out.println("[" + provider + "] wav 可解析：" + fileFormat.getFormat());
            } catch (Exception ex) {
                fail("wav 无法解析：" + ex.getMessage());
            }
        }
        String extension = AiSpeechUtils.getFileExtension(format);
        Path file = dir.resolve(provider + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "." + extension);
        Files.write(file, audio);
        System.out.println("[" + provider + "] 已保存 " + file + "，格式 " + format + "，" + audio.length + " 字节，" + diagnostics);
    }

    private static String env(String name) {
        return StrUtil.emptyToNull(StrUtil.trim(System.getenv(name)));
    }

    private static String required(String name) {
        String value = env(name);
        assumeTrue(value != null, "未配置 " + name);
        return value;
    }

    /**
     * 读取凭证：环境变量 NAME，或 NAME_FILE 指向的文件；不打印内容
     */
    private static String secret(String name) throws IOException {
        String value = env(name);
        if (value != null) {
            return value;
        }
        String file = env(name + "_FILE");
        if (file == null) {
            return null;
        }
        return StrUtil.emptyToNull(new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8).trim());
    }

}
