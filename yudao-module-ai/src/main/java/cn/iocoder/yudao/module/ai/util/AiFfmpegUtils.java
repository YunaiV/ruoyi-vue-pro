package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
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
 * 3. 可选旁白：与镜头原声混音，时长以视频为准；可选配乐：按使用的镜头区间铺设，循环补足，调节音量、起播位置，
 *    有对白时自动压低（sidechaincompress）
 * 4. 可选水印：drawtext 绘制在右下角；中文需要配置中文字体文件
 *
 * FFmpeg 需要的能力由上述功能决定：编码固定使用 libx264；有转场时用到 xfade；有水印时用到 drawtext（依赖 libfreetype）
 *
 * 另外提供镜头用的能力：截取视频的最后一帧（承接上一镜头），把素材图片渲染为静音的镜头视频，读取视频的时长、尺寸、帧率与编码
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
     * 配乐的默认音量：低于对白
     */
    public static final double DEFAULT_MUSIC_VOLUME = 0.12;

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
            // 1.3 下载配乐
            List<File> musicFiles = new ArrayList<>();
            for (int i = 0; i < request.getMusicTracks().size(); i++) {
                File file = new File(dir, "music-" + i);
                HttpUtil.downloadFile(request.getMusicTracks().get(i).getUrl(), file);
                musicFiles.add(file);
            }
            // 1.4 水印文本写入文件：drawtext 通过 textfile 读取，并关闭 expansion，避免转义
            File watermark = null;
            if (StrUtil.isNotBlank(request.getWatermarkText())) {
                watermark = new File(dir, "watermark.txt");
                FileUtil.writeUtf8String(request.getWatermarkText(), watermark);
            }

            // 2. 执行合成
            int[] canvas = parseCanvas(request.getAspectRatio());
            String filter = buildFilterComplex(clips, canvas[0], canvas[1], request.getTransitionType(),
                    request.getTransitionDuration(), narration != null, request.getMusicTracks(), fontFile,
                    watermark != null ? watermark.getAbsolutePath() : null);
            File output = new File(dir, "output.mp4");
            List<String> command = new ArrayList<>(Arrays.asList(ffmpegPath, "-y", "-hide_banner", "-loglevel", "error"));
            clips.forEach(clip -> command.addAll(Arrays.asList("-i", clip.getFile().getAbsolutePath())));
            if (narration != null) {
                command.addAll(Arrays.asList("-i", narration.getAbsolutePath()));
            }
            // 配乐循环输入：每段区间各一个输入，时长不足时循环补足
            for (int i = 0; i < musicFiles.size(); i++) {
                for (int j = 0; j < buildMusicRanges(clips, request.getMusicTracks().get(i), request.getTransitionType(),
                        request.getTransitionDuration()).size(); j++) {
                    command.addAll(Arrays.asList("-stream_loop", "-1", "-i", musicFiles.get(i).getAbsolutePath()));
                }
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
     * 截取视频的最后一帧
     *
     * @param ffmpegPath ffmpeg 命令
     * @param videoUrl 视频的可下载地址
     * @return PNG 图片内容
     */
    public static byte[] extractLastFrame(String ffmpegPath, String videoUrl) throws Exception {
        File dir = FileUtil.mkdir(FileUtil.file(FileUtil.getTmpDir(), "ai-drama-frame", IdUtil.fastSimpleUUID()));
        try {
            File input = new File(dir, "input.mp4");
            HttpUtil.downloadFile(videoUrl, input);
            // 从结尾前 1 秒开始解码，-update 1 持续覆盖同一张图，结束时即为最后一帧
            File output = new File(dir, "last.png");
            execute(Arrays.asList(ffmpegPath, "-y", "-hide_banner", "-loglevel", "error", "-sseof", "-1",
                    "-i", input.getAbsolutePath(), "-update", "1", output.getAbsolutePath()), new File(dir, "ffmpeg.log"));
            if (!output.exists() || output.length() == 0) {
                throw new IllegalStateException("没有读取到视频画面");
            }
            return FileUtil.readBytes(output);
        } finally {
            FileUtil.del(dir);
        }
    }

    /**
     * 把素材图片渲染为镜头视频：按画幅缩放补边，时长平均分配给每张图，带静音音轨（便于与其它镜头合成）
     *
     * @param ffmpegPath ffmpeg 命令
     * @param imageUrls 图片的可下载地址，按播放顺序
     * @param aspectRatio 画幅，例如 9:16
     * @param duration 总时长（秒）
     * @return MP4 视频内容
     */
    public static byte[] renderImages(String ffmpegPath, List<String> imageUrls, String aspectRatio, double duration) throws Exception {
        File dir = FileUtil.mkdir(FileUtil.file(FileUtil.getTmpDir(), "ai-drama-material", IdUtil.fastSimpleUUID()));
        try {
            // 1. 下载图片：保留扩展名，便于 FFmpeg 识别格式
            List<String> command = new ArrayList<>(Arrays.asList(ffmpegPath, "-y", "-hide_banner", "-loglevel", "error"));
            for (int i = 0; i < imageUrls.size(); i++) {
                String extName = FileUtil.extName(StrUtil.subBefore(imageUrls.get(i), '?', false));
                File file = new File(dir, "image-" + i + (StrUtil.isNotBlank(extName) ? "." + extName : ""));
                HttpUtil.downloadFile(imageUrls.get(i), file);
                command.addAll(Arrays.asList("-i", file.getAbsolutePath()));
            }
            // 2. 静音音轨，输入下标为图片数量
            command.addAll(Arrays.asList("-f", "lavfi", "-t", formatSeconds(duration), "-i", "anullsrc=r=44100:cl=stereo"));

            // 3. 执行渲染
            int[] canvas = parseCanvas(aspectRatio);
            File output = new File(dir, "output.mp4");
            command.addAll(Arrays.asList("-filter_complex", buildImagesFilter(imageUrls.size(), canvas[0], canvas[1], duration),
                    "-map", "[vout]", "-map", imageUrls.size() + ":a",
                    "-c:v", "libx264", "-preset", "veryfast", "-crf", "23", "-pix_fmt", "yuv420p", "-r", String.valueOf(FPS),
                    "-c:a", "aac", "-b:a", "128k", "-shortest", "-movflags", "+faststart", output.getAbsolutePath()));
            execute(command, new File(dir, "ffmpeg.log"));
            return FileUtil.readBytes(output);
        } finally {
            FileUtil.del(dir);
        }
    }

    /**
     * 构建素材图片的 filter_complex：每张图缩放补边到画布，重复为固定帧数，再拼接；输出标签为 [vout]
     *
     * 使用 loop 滤镜重复单帧，不依赖图片输入的 -loop 参数，图片格式由 FFmpeg 自动识别
     *
     * @param count 图片数量：输入下标 0 ~ count-1
     * @param width 画布宽
     * @param height 画布高
     * @param duration 总时长（秒）：平均分配给每张图，每张至少 1 帧
     */
    @VisibleForTesting
    static String buildImagesFilter(int count, int width, int height, double duration) {
        int frames = Math.max(1, (int) Math.round(duration * FPS / count));
        List<String> filters = new ArrayList<>();
        StringBuilder inputs = new StringBuilder();
        for (int i = 0; i < count; i++) {
            filters.add(StrUtil.format("[{}:v]scale={}:{}:force_original_aspect_ratio=decrease,"
                    + "pad={}:{}:(ow-iw)/2:(oh-ih)/2:color=black,setsar=1,format=yuv420p,"
                    + "loop=loop={}:size=1:start=0,setpts=N/{}/TB[v{}]", i, width, height, width, height, frames - 1, FPS, i));
            inputs.append("[v").append(i).append(']');
        }
        filters.add(inputs + "concat=n=" + count + ":v=1:a=0[vout]");
        return String.join(";", filters);
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
     * @param musicTracks 配乐：每段区间一个输入，下标在镜头、旁白之后依次排列
     * @param fontFile 字体文件，可为空
     * @param watermarkFile 水印文本文件，为空表示不加水印
     */
    @VisibleForTesting
    static String buildFilterComplex(List<Clip> clips, int width, int height, AiDramaTransitionTypeEnum transition,
                                     Double transitionDuration, boolean narration, List<MusicTrack> musicTracks,
                                     String fontFile, String watermarkFile) {
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

        // 4. 配乐：每段区间截取（起播位置开始）、调音量、淡入淡出、延迟到区间起点；混合后按对白压低，再与原声混音
        List<String> tracks = new ArrayList<>();
        int input = clips.size() + (narration ? 1 : 0);
        for (MusicTrack track : CollUtil.emptyIfNull(musicTracks)) {
            for (double[] range : buildMusicRanges(clips, track, transition, transitionDuration)) {
                double duration = range[1] - range[0];
                double fadeDuration = Math.min(1, duration / 3);
                String label = "m" + input;
                filters.add(StrUtil.format("[{}:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,"
                                + "atrim=start={}:duration={},asetpts=PTS-STARTPTS,volume={},afade=t=in:d={},"
                                + "afade=t=out:st={}:d={},adelay={}:all=1[{}]", input,
                        formatSeconds(ObjUtil.defaultIfNull(track.getStartOffset(), 0D)), formatSeconds(duration),
                        formatSeconds(ObjUtil.defaultIfNull(track.getVolume(), DEFAULT_MUSIC_VOLUME)),
                        formatSeconds(fadeDuration), formatSeconds(Math.max(0, duration - fadeDuration)),
                        formatSeconds(fadeDuration), Math.round(range[0] * 1000), label));
                tracks.add("[" + label + "]");
                input++;
            }
        }
        if (!tracks.isEmpty()) {
            filters.add(String.join("", tracks) + "amix=inputs=" + tracks.size() + ":normalize=0[music]");
            filters.add(StrUtil.format("[{}]asplit=2[dialogue][side]", audio));
            filters.add("[music][side]sidechaincompress=threshold=0.03:ratio=8:attack=20:release=400[ducked]");
            filters.add("[dialogue][ducked]amix=inputs=2:duration=first:dropout_transition=0:normalize=0,"
                    + "alimiter=limit=0.7:level=false[amusic]");
            audio = "amusic";
        }

        // 5. 水印：右下角。expansion=none：水印按原文绘制，避免 % 被当作表达式（例如 100% 会导致合成失败）
        if (StrUtil.isNotBlank(watermarkFile)) {
            String font = StrUtil.isNotBlank(fontFile) ? ":fontfile=" + escapeFilterPath(fontFile) : "";
            filters.add(StrUtil.format("[{}]drawtext=textfile={}:expansion=none{}:fontsize=h/28:fontcolor=white@0.85:"
                    + "shadowcolor=black@0.6:shadowx=2:shadowy=2:x=w-tw-32:y=h-th-32[vw]", video,
                    escapeFilterPath(watermarkFile), font));
            video = "vw";
        }
        filters.add(StrUtil.format("[{}]null[vout]", video));
        filters.add(StrUtil.format("[{}]anull[aout]", audio));
        return String.join(";", filters);
    }

    /**
     * 计算配乐在成片中铺设的区间：连续使用的镜头合并为一段；有转场时，后一个镜头提前转场时长开始
     *
     * @return 区间列表，每项为 [开始秒, 结束秒]
     */
    @VisibleForTesting
    static List<double[]> buildMusicRanges(List<Clip> clips, MusicTrack track, AiDramaTransitionTypeEnum transition,
                                           Double transitionDuration) {
        double overlap = transition != null && transition.getXfade() != null ? transitionDuration : 0;
        List<double[]> ranges = new ArrayList<>();
        double cursor = 0;
        double start = -1;
        double end = 0;
        for (int i = 0; i < clips.size(); i++) {
            double duration = clips.get(i).getDuration();
            if (track.getClipIndexes().contains(i)) {
                if (start < 0) {
                    start = cursor;
                }
                end = cursor + duration;
            } else if (start >= 0) {
                ranges.add(new double[]{start, end});
                start = -1;
            }
            cursor += Math.max(0, duration - overlap);
        }
        if (start >= 0) {
            ranges.add(new double[]{start, end});
        }
        return ranges;
    }

    /**
     * 读取视频信息：时长、尺寸、帧率、编码（第一路视频流）
     *
     * @param ffprobePath ffprobe 命令
     * @param videoUrl 视频的可下载地址
     * @return 视频信息
     */
    public static VideoInfo probeVideo(String ffprobePath, String videoUrl) throws Exception {
        File dir = FileUtil.mkdir(FileUtil.file(FileUtil.getTmpDir(), "ai-drama-probe", IdUtil.fastSimpleUUID()));
        try {
            File input = new File(dir, "input");
            HttpUtil.downloadFile(videoUrl, input);
            File json = new File(dir, "probe.json");
            execute(Arrays.asList(ffprobePath, "-v", "error", "-select_streams", "v:0", "-show_entries",
                    "format=duration:stream=codec_name,width,height,avg_frame_rate", "-of", "json", input.getAbsolutePath()), json);
            JsonNode root = JsonUtils.parseTree(FileUtil.readString(json, StandardCharsets.UTF_8));
            JsonNode stream = root.path("streams").path(0);
            if (stream.isMissingNode()) {
                throw new IllegalStateException("没有读取到视频画面");
            }
            List<String> rate = StrUtil.split(stream.path("avg_frame_rate").asText("0/1"), '/');
            double fps = rate.size() == 2 && NumberUtil.isNumber(rate.get(0)) && NumberUtil.isNumber(rate.get(1))
                    && Double.parseDouble(rate.get(1)) > 0 ? Double.parseDouble(rate.get(0)) / Double.parseDouble(rate.get(1)) : 0;
            return new VideoInfo(root.path("format").path("duration").asDouble(), stream.path("width").asInt(),
                    stream.path("height").asInt(), fps, stream.path("codec_name").asText());
        } finally {
            FileUtil.del(dir);
        }
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
     * 转义滤镜参数中的文件路径，返回带单引号的参数值
     *
     * FFmpeg 解析两层：先按滤镜图去掉单引号，再按 : 切分滤镜参数并处理 \ 转义。
     * Windows 路径的盘符（例如 C:\）会把参数截断，路径中的 \、' 也会被吃掉。参见 https://ffmpeg.org/ffmpeg-filters.html#Notes-on-filtergraph-escaping
     *
     * 1. 参数层：\、'、: 前加 \，路径原样传给 FFmpeg，Windows、Linux 都不需要替换分隔符
     * 2. 滤镜图层：整体加单引号；内部的 ' 需先闭合引号，写为 '\''
     */
    @VisibleForTesting
    static String escapeFilterPath(String path) {
        String value = path.replace("\\", "\\\\").replace("'", "\\'").replace(":", "\\:");
        return "'" + value.replace("'", "'\\''") + "'";
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
        /**
         * 配乐，可为空
         */
        private List<MusicTrack> musicTracks = new ArrayList<>();

    }

    /**
     * 配乐
     */
    @Data
    public static class MusicTrack {

        /**
         * 配乐的可下载地址
         */
        private String url;
        /**
         * 使用的镜头：在 {@link ComposeRequest#getVideoUrls()} 中的下标
         */
        private Set<Integer> clipIndexes;
        /**
         * 音量，为空时为 {@link AiFfmpegUtils#DEFAULT_MUSIC_VOLUME}
         */
        private Double volume;
        /**
         * 起播位置（秒）：从配乐的该位置开始播放
         */
        private Double startOffset;

    }

    /**
     * 视频信息
     */
    @Data
    @AllArgsConstructor
    public static class VideoInfo {

        /**
         * 时长（秒）
         */
        private double duration;
        private int width;
        private int height;
        /**
         * 帧率
         */
        private double fps;
        /**
         * 编码，例如 h264、hevc
         */
        private String codec;

    }

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
