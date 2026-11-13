package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao.DouBaoSeedanceVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao.DouBaoSeedanceVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng.JimengVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng.JimengVisualApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.kling.KlingCredentials;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.kling.KlingVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.kling.KlingVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxH3VideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxH3VideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxHailuoVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxHailuoVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.HappyHorseVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.HappyHorseVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.TongYiWanVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.TongYiWanVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.veo.VertexVeoVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.veo.VertexVeoVideoRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 视频生成真实调用的冒烟测试：会产生服务商费用，默认不执行
 *
 * 启用方式：
 * 1. 环境变量 YUDAO_AI_VIDEO_LIVE=true；
 * 2. AI_VIDEO_LIVE_OUTPUT_DIR 为仓库之外的本地绝对路径；
 * 3. AI_VIDEO_LIVE_CASES 显式列出要执行的用例（逗号分隔），未列出的用例不发请求，避免误计费。
 *
 * 防重复计费：
 * 1. 提交之前先在 {输出目录}/{用例}/ 写 submitting.json；提交成功后立即写 task.json（任务编号、路由，不含凭证），再删除 submitting.json；
 * 2. 再次运行时，存在 task.json 则只按编号继续查询，不重新提交；存在 submitting.json 或 submit-unknown.json
 *    （上次提交超时、中断、5xx，任务是否已创建未知）则拒绝提交，需人工到控制台确认后删除；
 * 3. 查询的超时、网络异常、限流、5xx 会在下一轮继续查询，不会重新提交；
 * 4. 等待超过 AI_VIDEO_LIVE_MAX_WAIT_SECONDS（默认 900）仍未完成时中止（不算通过），稍后重跑即可继续查询。
 *
 * 成功后把视频下载到 {输出目录}/{用例}/video-0.mp4（不带任何凭证、有大小上限），校验非空且为 MP4（ftyp），
 * result.json 只记录脱敏后的地址、大小、SHA-256。
 *
 * 凭证只从环境变量读取（XXX，或 XXX_FILE 指向仓库之外的文件）：
 *
 * | 用例 | 凭证 | 可选参数 |
 * | minimax-v1 | AI_VIDEO_MINIMAX_API_KEY | AI_VIDEO_MINIMAX_URL、AI_VIDEO_MINIMAX_V1_MODEL |
 * | minimax-v2 | AI_VIDEO_MINIMAX_API_KEY | AI_VIDEO_MINIMAX_URL、AI_VIDEO_MINIMAX_V2_MODEL |
 * | wan | AI_VIDEO_DASHSCOPE_API_KEY | AI_VIDEO_DASHSCOPE_URL、AI_VIDEO_WAN_MODEL |
 * | happyhorse | AI_VIDEO_DASHSCOPE_API_KEY | AI_VIDEO_DASHSCOPE_URL、AI_VIDEO_HAPPYHORSE_MODEL |
 * | doubao | AI_VIDEO_ARK_API_KEY | AI_VIDEO_ARK_URL、AI_VIDEO_DOUBAO_MODEL |
 * | kling | AI_VIDEO_KLING_API_KEY | AI_VIDEO_KLING_URL、AI_VIDEO_KLING_MODEL |
 * | jimeng | AI_VIDEO_VOLC_ACCESS_KEY + AI_VIDEO_VOLC_SECRET_KEY，或 AI_VIDEO_JIMENG_API_KEY | AI_VIDEO_JIMENG_URL |
 * | veo | AI_VIDEO_VERTEX_PROJECT + AI_VIDEO_VERTEX_ACCESS_TOKEN | AI_VIDEO_VERTEX_LOCATION、AI_VIDEO_VEO_MODEL、AI_VIDEO_VEO_STORAGE_URI |
 *
 * @author 芋道源码
 */
@EnabledIfEnvironmentVariable(named = "YUDAO_AI_VIDEO_LIVE", matches = "true")
public class AiVideoLiveSmokeTests {

    private static final String PROMPT = "一只橘猫在窗台上慢慢伸懒腰，午后阳光柔和，固定镜头";
    private static final String PROMPT_EN = "An orange cat slowly stretches on a sunny windowsill, static camera";
    private static final long MAX_VIDEO_BYTES = 300L * 1024 * 1024;
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(5);

    // ========== 用例 ==========

    @Test
    public void testMiniMaxV1() throws Exception {
        String apiKey = requireCase("minimax-v1", "AI_VIDEO_MINIMAX_API_KEY");
        MiniMaxHailuoVideoApi api = new MiniMaxHailuoVideoApi(env("AI_VIDEO_MINIMAX_URL"), apiKey);
        run("minimax-v1", () -> api.createTask(new MiniMaxHailuoVideoRequest()
                        .setModel(StrUtil.blankToDefault(env("AI_VIDEO_MINIMAX_V1_MODEL"), "MiniMax-Hailuo-02"))
                        .setPrompt(PROMPT).setDuration(6).setResolution("768P").setPromptOptimizer(false)),
                ref -> api.getTask(ref.getTaskId()));
    }

    @Test
    public void testMiniMaxV2() throws Exception {
        String apiKey = requireCase("minimax-v2", "AI_VIDEO_MINIMAX_API_KEY");
        MiniMaxH3VideoApi api = new MiniMaxH3VideoApi(env("AI_VIDEO_MINIMAX_URL"), apiKey);
        run("minimax-v2", () -> api.createTask(new MiniMaxH3VideoRequest()
                        .setModel(StrUtil.blankToDefault(env("AI_VIDEO_MINIMAX_V2_MODEL"), MiniMaxH3VideoApi.MODEL_H3_LITE))
                        .setPrompt(PROMPT).setResolution("480P").setDuration(4).setRatio("16:9")),
                ref -> api.getTask(ref.getTaskId()));
    }

    @Test
    public void testTongYiWan() throws Exception {
        String apiKey = requireCase("wan", "AI_VIDEO_DASHSCOPE_API_KEY");
        TongYiWanVideoApi api = new TongYiWanVideoApi(env("AI_VIDEO_DASHSCOPE_URL"), apiKey);
        run("wan", () -> api.createTask(new TongYiWanVideoRequest()
                        .setModel(StrUtil.blankToDefault(env("AI_VIDEO_WAN_MODEL"), "wan2.7-t2v"))
                        .setPrompt(PROMPT).setResolution("720P").setRatio("16:9").setDuration(2).setPromptExtend(false)),
                ref -> api.getTask(ref.getTaskId()));
    }

    @Test
    public void testHappyHorse() throws Exception {
        String apiKey = requireCase("happyhorse", "AI_VIDEO_DASHSCOPE_API_KEY");
        HappyHorseVideoApi api = new HappyHorseVideoApi(env("AI_VIDEO_DASHSCOPE_URL"), apiKey);
        run("happyhorse", () -> api.createTask(new HappyHorseVideoRequest()
                        .setModel(StrUtil.blankToDefault(env("AI_VIDEO_HAPPYHORSE_MODEL"), "happyhorse-1.1-t2v"))
                        .setPrompt(PROMPT).setResolution("480P").setRatio("16:9").setDuration(3)),
                ref -> api.getTask(ref.getTaskId()));
    }

    @Test
    public void testDouBaoSeedance() throws Exception {
        String apiKey = requireCase("doubao", "AI_VIDEO_ARK_API_KEY");
        DouBaoSeedanceVideoApi api = new DouBaoSeedanceVideoApi(env("AI_VIDEO_ARK_URL"), apiKey);
        run("doubao", () -> api.createTask(new DouBaoSeedanceVideoRequest()
                        .setModel(StrUtil.blankToDefault(env("AI_VIDEO_DOUBAO_MODEL"), "doubao-seedance-1-0-pro-fast-251015"))
                        .setPrompt(PROMPT).setResolution("480p").setRatio("16:9").setDuration(2).setWatermark(false)),
                ref -> api.getTask(ref.getTaskId()));
    }

    @Test
    public void testKling() throws Exception {
        String apiKey = requireCase("kling", "AI_VIDEO_KLING_API_KEY");
        KlingVideoApi api = new KlingVideoApi(env("AI_VIDEO_KLING_URL"), KlingCredentials.ofApiKey(apiKey));
        run("kling", () -> api.createTask(new KlingVideoRequest()
                        .setModel(StrUtil.blankToDefault(env("AI_VIDEO_KLING_MODEL"), KlingVideoApi.MODEL_2_5_TURBO))
                        .setPrompt(PROMPT).setResolution("720p").setAspectRatio("16:9").setDuration(5)),
                api::getTask);
    }

    @Test
    public void testJimeng() throws Exception {
        assumeTrue(isCaseEnabled("jimeng"), "AI_VIDEO_LIVE_CASES 未包含 jimeng");
        String accessKey = secret("AI_VIDEO_VOLC_ACCESS_KEY");
        String secretKey = secret("AI_VIDEO_VOLC_SECRET_KEY");
        String apiKey = secret("AI_VIDEO_JIMENG_API_KEY");
        assumeTrue((accessKey != null && secretKey != null) || apiKey != null,
                "未配置 AI_VIDEO_VOLC_ACCESS_KEY/AI_VIDEO_VOLC_SECRET_KEY 或 AI_VIDEO_JIMENG_API_KEY");
        JimengVisualApi api = accessKey != null && secretKey != null
                ? JimengVisualApi.ofAccessKey(accessKey, secretKey, env("AI_VIDEO_JIMENG_URL"), null)
                : JimengVisualApi.ofApiKey(apiKey, env("AI_VIDEO_JIMENG_URL"), null);
        run("jimeng", () -> api.submitVideo(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_T2V_V30)
                .setPrompt(PROMPT).setFrames(121).setAspectRatio("16:9")), api::getTask);
    }

    @Test
    public void testVeo() throws Exception {
        String token = requireCase("veo", "AI_VIDEO_VERTEX_ACCESS_TOKEN");
        String project = env("AI_VIDEO_VERTEX_PROJECT");
        assumeTrue(project != null, "未配置 AI_VIDEO_VERTEX_PROJECT");
        VertexVeoVideoApi api = new VertexVeoVideoApi(project, env("AI_VIDEO_VERTEX_LOCATION"), () -> token);
        run("veo", () -> api.createTask(new VertexVeoVideoRequest()
                        .setModel(StrUtil.blankToDefault(env("AI_VIDEO_VEO_MODEL"), VertexVeoVideoApi.MODEL_3_1_LITE))
                        .setPrompt(PROMPT_EN).setAspectRatio("16:9").setDurationSeconds(4).setResolution("720p")
                        .setGenerateAudio(false).setSampleCount(1).setStorageUri(env("AI_VIDEO_VEO_STORAGE_URI"))),
                api::getTask);
    }

    // ========== 提交、续查、下载 ==========

    private void run(String name, Supplier<AiVideoTaskRef> submit, Function<AiVideoTaskRef, AiVideoTaskResult> query)
            throws Exception {
        Path dir = outputDir().resolve(name);
        Files.createDirectories(dir);
        Path taskFile = dir.resolve("task.json");
        Path submittingFile = dir.resolve("submitting.json");
        Path unknownFile = dir.resolve("submit-unknown.json");

        // 1. 已提交过：只继续查询；提交结果未知：拒绝重新提交
        AiVideoTaskRef ref;
        if (Files.exists(taskFile)) {
            ref = readTaskRef(taskFile);
            log(name, "继续查询已提交的任务 " + ref.getTaskId());
        } else {
            if (Files.exists(submittingFile) || Files.exists(unknownFile)) {
                fail(name + " 上次提交的结果未知（" + dir + "），请到控制台确认是否已创建任务，确认后再删除标记文件；不自动重新提交");
            }
            ObjectNode marker = AiVideoUtils.newObject();
            marker.put("case", name);
            marker.put("startedAt", OffsetDateTime.now().toString());
            write(submittingFile, marker);
            try {
                ref = submit.get();
            } catch (AiVideoException ex) {
                ObjectNode error = toJson(ex);
                if (ex.isSubmitOutcomeUnknown()) {
                    write(unknownFile, error);
                } else {
                    write(dir.resolve("submit-failed.json"), error);
                }
                Files.deleteIfExists(submittingFile);
                throw ex;
            } catch (RuntimeException ex) {
                // 参数校验失败等：请求没有发出
                Files.deleteIfExists(submittingFile);
                throw ex;
            }
            write(taskFile, toJson(ref));
            Files.deleteIfExists(submittingFile);
            log(name, "已提交，任务编号 " + ref.getTaskId());
        }

        // 2. 轮询
        long deadline = System.currentTimeMillis() + Long.parseLong(StrUtil.blankToDefault(env("AI_VIDEO_LIVE_MAX_WAIT_SECONDS"), "900")) * 1000;
        long interval = Long.parseLong(StrUtil.blankToDefault(env("AI_VIDEO_LIVE_POLL_SECONDS"), "15")) * 1000;
        AiVideoTaskResult result = null;
        while (true) {
            try {
                result = query.apply(ref);
                write(dir.resolve("status.json"), toJson(result));
                log(name, "状态 " + result.getStatus() + "（" + result.getRawStatus() + "）");
                if (result.isFinished()) {
                    break;
                }
            } catch (AiVideoException ex) {
                write(dir.resolve("query-error.json"), toJson(ex));
                if (!isRetryable(ex)) {
                    throw ex;
                }
                log(name, "查询异常，下一轮继续：" + ex.getKind());
            }
            if (System.currentTimeMillis() + interval > deadline) {
                throw new TestAbortedException(name + " 在等待时间内未完成，任务编号已保存在 " + taskFile + "，稍后重跑继续查询");
            }
            Thread.sleep(interval);
        }

        // 3. 结果
        if (!result.isSucceeded()) {
            fail(name + " 任务结束但未成功：" + result.getStatus() + " " + result.getErrorCode() + " " + result.getErrorMessage());
        }
        ObjectNode summary = AiVideoUtils.newObject();
        summary.put("case", name);
        summary.put("taskId", ref.getTaskId());
        summary.put("model", ref.getModel());
        summary.put("finishedAt", OffsetDateTime.now().toString());
        ArrayNode videos = summary.putArray("videos");
        for (int i = 0; i < result.getVideos().size(); i++) {
            AiVideoOutput output = result.getVideos().get(i);
            ObjectNode video = videos.addObject();
            Path file = dir.resolve("video-" + i + ".mp4");
            if (output.getUrl() != null) {
                video.put("url", AiVideoUtils.maskUrl(output.getUrl()));
                AiVideoHttpClient.downloadToFile(output.getUrl(), file, MAX_VIDEO_BYTES, DOWNLOAD_TIMEOUT);
            } else if (output.getData() != null) {
                Files.write(file, output.getData());
            } else {
                // gs:// 需要凭证下载，不在测试中转发凭证
                video.put("gcsUri", output.getGcsUri());
                continue;
            }
            byte[] head = readHead(file);
            video.put("file", file.getFileName().toString());
            video.put("size", Files.size(file));
            video.put("sha256", sha256(file));
            video.put("mp4", AiVideoUtils.isMp4(head));
            assertTrue(Files.size(file) > 0, name + " 下载的视频为空");
            assertTrue(AiVideoUtils.isMp4(head), name + " 下载的文件不是 MP4");
        }
        write(dir.resolve("result.json"), summary);
        log(name, "完成，视频 " + videos.size() + " 个");
    }

    private static boolean isRetryable(AiVideoException ex) {
        return ex.getKind() == AiVideoException.Kind.TIMEOUT || ex.getKind() == AiVideoException.Kind.NETWORK
                || ex.getKind() == AiVideoException.Kind.SERVER_ERROR || ex.getKind() == AiVideoException.Kind.RATE_LIMITED;
    }

    private static ObjectNode toJson(AiVideoTaskRef ref) {
        ObjectNode node = AiVideoUtils.newObject();
        node.put("platform", ref.getPlatform());
        node.put("model", ref.getModel());
        node.put("taskId", ref.getTaskId());
        node.put("route", ref.getRoute());
        node.put("requestId", ref.getRequestId());
        node.put("submittedAt", OffsetDateTime.now().toString());
        return node;
    }

    private static AiVideoTaskRef readTaskRef(Path file) throws IOException {
        JsonNode node = AiVideoUtils.parseJson(Files.readAllBytes(file));
        if (node == null) {
            throw new IllegalStateException("任务文件格式不正确：" + file);
        }
        return new AiVideoTaskRef(AiVideoUtils.text(node, "platform"), AiVideoUtils.text(node, "model"),
                AiVideoUtils.text(node, "taskId"), AiVideoUtils.text(node, "route"), AiVideoUtils.text(node, "requestId"));
    }

    private static ObjectNode toJson(AiVideoTaskResult result) {
        ObjectNode node = AiVideoUtils.newObject();
        node.put("taskId", result.getTaskId());
        node.put("status", String.valueOf(result.getStatus()));
        node.put("rawStatus", result.getRawStatus());
        node.put("errorCode", result.getErrorCode());
        node.put("errorMessage", result.getErrorMessage());
        node.put("requestId", result.getRequestId());
        node.put("videoCount", result.getVideos().size());
        ObjectNode metadata = node.putObject("metadata");
        result.getMetadata().forEach(metadata::put);
        node.put("polledAt", OffsetDateTime.now().toString());
        return node;
    }

    private static ObjectNode toJson(AiVideoException ex) {
        ObjectNode node = AiVideoUtils.newObject();
        node.put("platform", ex.getPlatform());
        node.put("kind", String.valueOf(ex.getKind()));
        node.put("httpStatus", ex.getHttpStatus());
        node.put("code", ex.getCode());
        node.put("requestId", ex.getRequestId());
        node.put("submitOutcomeUnknown", ex.isSubmitOutcomeUnknown());
        node.put("message", ex.getMessage());
        node.put("at", OffsetDateTime.now().toString());
        return node;
    }

    private static void write(Path file, JsonNode node) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temp, AiVideoUtils.toJsonBytes(node));
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private static byte[] readHead(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] head = new byte[16];
            int read = in.read(head);
            return read <= 0 ? new byte[0] : Arrays.copyOf(head, read);
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static void log(String name, String message) {
        System.out.println("[AiVideoLive][" + name + "] " + message);
    }

    // ========== 环境 ==========

    private static boolean isCaseEnabled(String name) {
        List<String> cases = StrUtil.splitTrim(StrUtil.nullToEmpty(env("AI_VIDEO_LIVE_CASES")), ',');
        return cases.contains(name);
    }

    private static String requireCase(String name, String secretName) {
        assumeTrue(isCaseEnabled(name), "AI_VIDEO_LIVE_CASES 未包含 " + name);
        String value = secret(secretName);
        assumeTrue(value != null, "未配置 " + secretName);
        return value;
    }

    private static String env(String name) {
        return StrUtil.trimToNull(System.getenv(name));
    }

    /**
     * 读取凭证：环境变量 XXX，或 XXX_FILE 指向仓库之外的文件
     */
    private static String secret(String name) {
        String value = env(name);
        if (value != null) {
            return value;
        }
        String file = env(name + "_FILE");
        if (file == null) {
            return null;
        }
        Path path = Paths.get(file).toAbsolutePath().normalize();
        if (isInsideGitRepository(path)) {
            throw new IllegalStateException(name + "_FILE 不能位于 Git 仓库内");
        }
        try {
            return StrUtil.trimToNull(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException(name + "_FILE 读取失败：" + ex.getClass().getSimpleName());
        }
    }

    private static Path outputDir() throws IOException {
        String value = env("AI_VIDEO_LIVE_OUTPUT_DIR");
        assumeTrue(value != null, "未配置 AI_VIDEO_LIVE_OUTPUT_DIR");
        Path path = Paths.get(value);
        if (!path.isAbsolute()) {
            throw new IllegalStateException("AI_VIDEO_LIVE_OUTPUT_DIR 必须为绝对路径");
        }
        path = path.normalize();
        if (isInsideGitRepository(path)) {
            throw new IllegalStateException("AI_VIDEO_LIVE_OUTPUT_DIR 不能位于 Git 仓库内");
        }
        Files.createDirectories(path);
        if (!Files.isWritable(path)) {
            throw new IllegalStateException("AI_VIDEO_LIVE_OUTPUT_DIR 不可写");
        }
        return path;
    }

    private static boolean isInsideGitRepository(Path path) {
        for (Path current = path; current != null; current = current.getParent()) {
            if (Files.exists(current.resolve(".git"))) {
                return true;
            }
        }
        return false;
    }

}
