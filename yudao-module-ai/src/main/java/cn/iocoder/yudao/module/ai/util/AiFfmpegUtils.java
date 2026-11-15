package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTransitionTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI FFmpeg 工具类：调用本机 FFmpeg，把多个镜头视频合成为一个 MP4，用于短剧成片
 *
 * 1. 每个镜头缩放并补边到同一画布，统一帧率、像素格式；没有音轨的镜头补静音
 * 2. 无转场时直接拼接；有转场时，视频使用 xfade、音频使用 acrossfade
 * 3. 可选旁白：与镜头原声混音，时长以视频为准
 * 4. 可选水印：drawtext 绘制在右下角；中文需要配置中文字体文件
 *
 * FFmpeg 需要的能力由上述功能决定：编码固定使用 libx264；有转场时用到 xfade；有水印时用到 drawtext（依赖 libfreetype）
 *
 * @author 芋道源码
 */
@Slf4j
public class AiFfmpegUtils {

    /**
     * 合成画布短边的像素
     */
    private static final int CANVAS_SHORT_SIDE = 720;
    /**
     * 合成输出帧率
     */
    private static final int FPS = 30;
    /**
     * 单次 FFmpeg 执行的时限（分钟）
     */
    private static final int TIMEOUT_MINUTES = 30;

    /**
     * 合成成片
     *
     * @param ffmpegPath ffmpeg 命令
     * @param ffprobePath ffprobe 命令
     * @param fontFile 水印使用的字体文件，可为空
     * @param request 合成参数
     * @return 成片内容与时长
     */
    public static ComposeResult compose(String ffmpegPath, String ffprobePath, String fontFile,
                                        ComposeRequest request) throws Exception {
        File dir = FileUtil.mkdir(FileUtil.file(FileUtil.getTmpDir(), "ai-drama-compose", IdUtil.fastSimpleUUID()));
        try {
            // 1.1 下载镜头，探测时长与是否有音轨
            List<Clip> clips = new ArrayList<>();
            for (int i = 0; i < request.getVideoUrls().size(); i++) {
                File file = new File(dir, "clip-" + i + ".mp4");
                HttpUtil.downloadFile(request.getVideoUrls().get(i), file);
                clips.add(probeClip(ffprobePath, file));
            }
            // 1.2 下载旁白
            File narration = null;
            if (StrUtil.isNotBlank(request.getNarrationUrl())) {
                narration = new File(dir, "narration");
                HttpUtil.downloadFile(request.getNarrationUrl(), narration);
            }
            // 1.3 水印文本写入文件：drawtext 通过 textfile 读取，避免转义
            File watermark = null;
            if (StrUtil.isNotBlank(request.getWatermarkText())) {
                watermark = new File(dir, "watermark.txt");
                FileUtil.writeUtf8String(request.getWatermarkText(), watermark);
            }

            // 2. 执行合成
            int[] canvas = parseCanvas(request.getAspectRatio());
            String filter = buildFilterComplex(clips, canvas[0], canvas[1], request.getTransitionType(),
                    request.getTransitionDuration(), narration != null, fontFile,
                    watermark != null ? watermark.getAbsolutePath() : null);
            File output = new File(dir, "output.mp4");
            List<String> command = new ArrayList<>(Arrays.asList(ffmpegPath, "-y", "-hide_banner", "-loglevel", "error"));
            clips.forEach(clip -> command.addAll(Arrays.asList("-i", clip.getFile().getAbsolutePath())));
            if (narration != null) {
                command.addAll(Arrays.asList("-i", narration.getAbsolutePath()));
            }
            command.addAll(Arrays.asList("-filter_complex", filter, "-map", "[vout]", "-map", "[aout]",
                    "-c:v", "libx264", "-preset", "veryfast", "-crf", "23", "-pix_fmt", "yuv420p", "-r", String.valueOf(FPS),
                    "-c:a", "aac", "-b:a", "128k", "-movflags", "+faststart", output.getAbsolutePath()));
            execute(command, new File(dir, "ffmpeg.log"));

            // 3. 读取成片与时长
            return new ComposeResult(FileUtil.readBytes(output), probeClip(ffprobePath, output).getDuration());
        } finally {
            FileUtil.del(dir);
        }
    }

    /**
     * 构建 filter_complex：输出标签为 [vout]、[aout]
     *
     * @param clips 镜头：输入下标与列表下标一致
     * @param width 画布宽
     * @param height 画布高
     * @param transition 转场类型
     * @param transitionDuration 转场时长（秒）
     * @param narration 是否有旁白：旁白的输入下标为镜头数量
     * @param fontFile 字体文件，可为空
     * @param watermarkFile 水印文本文件，为空表示不加水印
     */
    @VisibleForTesting
    static String buildFilterComplex(List<Clip> clips, int width, int height, AiDramaTransitionTypeEnum transition,
                                     Double transitionDuration, boolean narration, String fontFile, String watermarkFile) {
        boolean crossfade = transition != null && transition.getXfade() != null;
        double fade = crossfade ? transitionDuration : 0;
        if (crossfade) {
            for (Clip clip : clips) {
                if (clip.getDuration() <= fade) {
                    throw new IllegalArgumentException(StrUtil.format("转场时长（{} 秒）需小于每个镜头的时长", fade));
                }
            }
        }
        List<String> filters = new ArrayList<>();
        // 1. 统一每个镜头的画面与声音：画面缩放补边到画布；声音补齐、截断到画面时长，没有音轨的补静音
        for (int i = 0; i < clips.size(); i++) {
            Clip clip = clips.get(i);
            String duration = formatSeconds(clip.getDuration());
            filters.add(StrUtil.format("[{}:v]scale={}:{}:force_original_aspect_ratio=decrease,"
                    + "pad={}:{}:(ow-iw)/2:(oh-ih)/2:color=black,setsar=1,fps={},format=yuv420p,settb=AVTB[v{}]",
                    i, width, height, width, height, FPS, i));
            if (clip.isHasAudio()) {
                filters.add(StrUtil.format("[{}:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,"
                        + "apad,atrim=0:{},asetpts=PTS-STARTPTS[a{}]", i, duration, i));
            } else {
                filters.add(StrUtil.format("anullsrc=r=44100:cl=stereo,atrim=0:{},aformat=sample_fmts=fltp[a{}]",
                        duration, i));
            }
        }

        // 2. 拼接：无转场直接 concat；有转场逐段 xfade、acrossfade
        String video;
        String audio;
        if (!crossfade) {
            StringBuilder inputs = new StringBuilder();
            for (int i = 0; i < clips.size(); i++) {
                inputs.append("[v").append(i).append("][a").append(i).append(']');
            }
            filters.add(inputs + "concat=n=" + clips.size() + ":v=1:a=1[vc][ac]");
            video = "vc";
            audio = "ac";
        } else {
            video = "v0";
            audio = "a0";
            double offset = 0;
            for (int i = 1; i < clips.size(); i++) {
                offset += clips.get(i - 1).getDuration() - fade;
                String nextVideo = "vx" + i;
                String nextAudio = "ax" + i;
                filters.add(StrUtil.format("[{}][v{}]xfade=transition={}:duration={}:offset={}[{}]",
                        video, i, transition.getXfade(), formatSeconds(fade), formatSeconds(offset), nextVideo));
                filters.add(StrUtil.format("[{}][a{}]acrossfade=d={}[{}]", audio, i, formatSeconds(fade), nextAudio));
                video = nextVideo;
                audio = nextAudio;
            }
        }

        // 3. 旁白：与原声混音，时长以视频为准
        if (narration) {
            filters.add(StrUtil.format("[{}:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo[nar]",
                    clips.size()));
            filters.add(StrUtil.format("[{}][nar]amix=inputs=2:duration=first:dropout_transition=0:normalize=0[am]", audio));
            audio = "am";
        }

        // 4. 水印：右下角
        if (StrUtil.isNotBlank(watermarkFile)) {
            String font = StrUtil.isNotBlank(fontFile) ? ":fontfile='" + fontFile + "'" : "";
            filters.add(StrUtil.format("[{}]drawtext=textfile='{}'{}:fontsize=h/28:fontcolor=white@0.85:"
                    + "shadowcolor=black@0.6:shadowx=2:shadowy=2:x=w-tw-32:y=h-th-32[vw]", video, watermarkFile, font));
            video = "vw";
        }
        filters.add(StrUtil.format("[{}]null[vout]", video));
        filters.add(StrUtil.format("[{}]anull[aout]", audio));
        return String.join(";", filters);
    }

    /**
     * 按画幅计算画布：短边 {@link #CANVAS_SHORT_SIDE}，宽高取偶数
     *
     * @param aspectRatio 画幅，例如 9:16
     * @return [宽, 高]
     */
    @VisibleForTesting
    static int[] parseCanvas(String aspectRatio) {
        List<String> parts = StrUtil.split(aspectRatio, ':');
        if (parts.size() != 2 || !NumberUtil.isNumber(parts.get(0)) || !NumberUtil.isNumber(parts.get(1))) {
            throw new IllegalArgumentException("画幅不正确：" + aspectRatio);
        }
        double w = Double.parseDouble(parts.get(0));
        double h = Double.parseDouble(parts.get(1));
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("画幅不正确：" + aspectRatio);
        }
        if (w >= h) {
            return new int[]{even(CANVAS_SHORT_SIDE * w / h), CANVAS_SHORT_SIDE};
        }
        return new int[]{CANVAS_SHORT_SIDE, even(CANVAS_SHORT_SIDE * h / w)};
    }

    private static int even(double value) {
        return (int) Math.round(value / 2) * 2;
    }

    /**
     * 格式化秒数，用于 FFmpeg 参数：固定使用 Locale.ROOT，避免受 JVM 默认 Locale 影响输出逗号（例如德语的 0,5）
     */
    @VisibleForTesting
    static String formatSeconds(double seconds) {
        return new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(seconds);
    }

    /**
     * 探测时长与是否有音轨
     */
    private static Clip probeClip(String ffprobePath, File file) throws Exception {
        File json = new File(file.getParentFile(), file.getName() + ".probe.json");
        execute(Arrays.asList(ffprobePath, "-v", "error", "-show_entries", "format=duration:stream=codec_type",
                "-of", "json", file.getAbsolutePath()), json);
        JsonNode root = JsonUtils.parseTree(FileUtil.readString(json, StandardCharsets.UTF_8));
        String duration = root.path("format").path("duration").asText();
        if (!NumberUtil.isNumber(duration)) {
            throw new IllegalStateException("无法读取视频时长：" + file.getName());
        }
        boolean hasAudio = false;
        for (JsonNode stream : root.path("streams")) {
            if ("audio".equals(stream.path("codec_type").asText())) {
                hasAudio = true;
            }
        }
        return new Clip(file, Double.parseDouble(duration), hasAudio);
    }

    /**
     * 执行命令：标准输出、错误输出写入 output 文件；失败时抛出异常，异常信息取最后一行输出
     */
    private static void execute(List<String> command, File output) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output).start();
        if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            throw new IllegalStateException("FFmpeg 执行超时");
        }
        if (process.exitValue() != 0) {
            String content = FileUtil.readString(output, StandardCharsets.UTF_8);
            log.error("[execute][命令({}) 执行失败，输出：{}]", command, content);
            List<String> lines = StrUtil.split(StrUtil.trim(content), '\n');
            throw new IllegalStateException("FFmpeg 执行失败：" + (lines.isEmpty() ? "exit " + process.exitValue()
                    : StrUtil.trim(lines.get(lines.size() - 1))));
        }
    }

    // DONE @AI：class；lombok（原 AiDramaProjectServiceImpl 的 record ComposeRequest，改为 Lombok 类，链式 set 构建）
    /**
     * 合成参数
     */
    @Data
    public static class ComposeRequest {

        /**
         * 镜头视频的可下载地址，按播放顺序
         */
        private List<String> videoUrls;
        /**
         * 画幅
         */
        private String aspectRatio;
        /**
         * 转场类型
         */
        private AiDramaTransitionTypeEnum transitionType;
        /**
         * 转场时长（秒）
         */
        private Double transitionDuration;
        /**
         * 旁白的可下载地址，可为空
         */
        private String narrationUrl;
        /**
         * 水印文本，可为空
         */
        private String watermarkText;

    }

    // DONE @AI：class；lombok（原 AiDramaProjectServiceImpl 的 record ComposeResult，改为 Lombok 类；镜头 Clip 一并改）
    /**
     * 合成结果
     */
    @Data
    @AllArgsConstructor
    public static class ComposeResult {

        /**
         * 成片内容
         */
        private byte[] content;
        /**
         * 成片时长（秒）
         */
        private Double duration;

    }

    /**
     * 镜头
     */
    @Data
    @AllArgsConstructor
    @VisibleForTesting
    static class Clip {

        /**
         * 本地文件
         */
        private File file;
        /**
         * 时长（秒）
         */
        private double duration;
        /**
         * 是否有音轨
         */
        private boolean hasAudio;

    }

}
