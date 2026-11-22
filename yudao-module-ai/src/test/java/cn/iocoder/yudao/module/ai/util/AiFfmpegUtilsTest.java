package cn.iocoder.yudao.module.ai.util;

import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTransitionTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiFfmpegUtils} 的单元测试类：只校验命令构建，不执行 FFmpeg
 *
 * @author 芋道源码
 */
public class AiFfmpegUtilsTest {

    @Test
    public void testParseCanvas() {
        assertArrayEquals(new int[]{720, 1280}, AiFfmpegUtils.parseCanvas("9:16"));
        assertArrayEquals(new int[]{1280, 720}, AiFfmpegUtils.parseCanvas("16:9"));
        assertArrayEquals(new int[]{720, 720}, AiFfmpegUtils.parseCanvas("1:1"));
        assertArrayEquals(new int[]{720, 960}, AiFfmpegUtils.parseCanvas("3:4"));
        assertThrows(IllegalArgumentException.class, () -> AiFfmpegUtils.parseCanvas("abc"));
        assertThrows(IllegalArgumentException.class, () -> AiFfmpegUtils.parseCanvas("0:1"));
    }

    @Test
    public void testBuildFilterComplex_concat() {
        List<AiFfmpegUtils.Clip> clips = Arrays.asList(
                new AiFfmpegUtils.Clip(null, 5, true), new AiFfmpegUtils.Clip(null, 4.5, false));

        String filter = AiFfmpegUtils.buildFilterComplex(clips, 720, 1280, AiDramaTransitionTypeEnum.NONE,
                0.5, false, null, null, null);

        // 画面统一到画布；有音轨的补齐截断，没有音轨的补静音
        assertTrue(filter.contains("[0:v]scale=720:1280:force_original_aspect_ratio=decrease,pad=720:1280"));
        assertTrue(filter.contains("[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,apad,atrim=0:5,"));
        assertTrue(filter.contains("anullsrc=r=44100:cl=stereo,atrim=0:4.5"));
        // 无转场直接拼接
        assertTrue(filter.contains("[v0][a0][v1][a1]concat=n=2:v=1:a=1[vc][ac]"));
        assertFalse(filter.contains("xfade"));
        assertTrue(filter.endsWith("[vc]null[vout];[ac]anull[aout]"));
    }

    @Test
    public void testBuildFilterComplex_xfadeNarrationWatermark() {
        List<AiFfmpegUtils.Clip> clips = Arrays.asList(new AiFfmpegUtils.Clip(null, 5, true),
                new AiFfmpegUtils.Clip(null, 4, true), new AiFfmpegUtils.Clip(null, 6, true));

        String filter = AiFfmpegUtils.buildFilterComplex(clips, 720, 1280, AiDramaTransitionTypeEnum.FADE,
                0.5, true, null, "/fonts/cjk.ttc", "/tmp/watermark.txt");

        // 转场：offset 为前面镜头时长之和减去已重叠的转场
        assertTrue(filter.contains("[v0][v1]xfade=transition=fadeblack:duration=0.5:offset=4.5[vx1]"));
        assertTrue(filter.contains("[vx1][v2]xfade=transition=fadeblack:duration=0.5:offset=8[vx2]"));
        assertTrue(filter.contains("[a0][a1]acrossfade=d=0.5[ax1]"));
        assertTrue(filter.contains("[ax1][a2]acrossfade=d=0.5[ax2]"));
        // 旁白：输入下标为镜头数量
        assertTrue(filter.contains("[3:a]aformat="));
        assertTrue(filter.contains("[ax2][nar]amix=inputs=2:duration=first"));
        // 水印：按原文绘制，不展开 %
        assertTrue(filter.contains("[vx2]drawtext=textfile='/tmp/watermark.txt':expansion=none:fontfile='/fonts/cjk.ttc'"));
        assertTrue(filter.endsWith("[vw]null[vout];[am]anull[aout]"));
    }

    @Test
    public void testBuildFilterComplex_music() {
        List<AiFfmpegUtils.Clip> clips = Arrays.asList(new AiFfmpegUtils.Clip(null, 5, true),
                new AiFfmpegUtils.Clip(null, 4, true), new AiFfmpegUtils.Clip(null, 6, true));
        AiFfmpegUtils.MusicTrack track = new AiFfmpegUtils.MusicTrack();
        track.setClipIndexes(new HashSet<>(Arrays.asList(1, 2)));
        track.setVolume(0.2);
        track.setStartOffset(3.0);

        String filter = AiFfmpegUtils.buildFilterComplex(clips, 720, 1280, AiDramaTransitionTypeEnum.NONE,
                0.5, false, Collections.singletonList(track), null, null);

        // 第 2、3 个镜头连续使用，合并为 5 ~ 15 秒一段：从配乐第 3 秒开始，延迟到第 5 秒
        assertTrue(filter.contains("[3:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,"
                + "atrim=start=3:duration=10,asetpts=PTS-STARTPTS,volume=0.2,"));
        assertTrue(filter.contains("adelay=5000:all=1[m3]"));
        // 按对白压低后，与原声混音
        assertTrue(filter.contains("[m3]amix=inputs=1:normalize=0[music]"));
        assertTrue(filter.contains("[ac]asplit=2[dialogue][side]"));
        assertTrue(filter.contains("[music][side]sidechaincompress="));
        assertTrue(filter.endsWith("[vc]null[vout];[amusic]anull[aout]"));
    }

    @Test
    public void testBuildMusicRanges_transition() {
        List<AiFfmpegUtils.Clip> clips = Arrays.asList(new AiFfmpegUtils.Clip(null, 5, true),
                new AiFfmpegUtils.Clip(null, 4, true), new AiFfmpegUtils.Clip(null, 6, true));
        AiFfmpegUtils.MusicTrack track = new AiFfmpegUtils.MusicTrack();
        track.setClipIndexes(new HashSet<>(Arrays.asList(0, 2)));

        List<double[]> ranges = AiFfmpegUtils.buildMusicRanges(clips, track, AiDramaTransitionTypeEnum.FADE, 0.5);

        // 不连续的镜头分为两段；有转场时后一个镜头提前转场时长开始
        assertEquals(2, ranges.size());
        assertArrayEquals(new double[]{0, 5}, ranges.get(0), 0.001);
        assertArrayEquals(new double[]{8, 14}, ranges.get(1), 0.001);
    }

    @Test
    public void testBuildFilterComplex_windowsWatermarkPath() {
        List<AiFfmpegUtils.Clip> clips = Collections.singletonList(new AiFfmpegUtils.Clip(null, 5, true));

        String filter = AiFfmpegUtils.buildFilterComplex(clips, 720, 1280, AiDramaTransitionTypeEnum.NONE,
                0.5, false, null, "C:\\Windows\\Fonts\\msyh.ttc",
                "C:\\Users\\yudao\\AppData\\Local\\Temp\\ai-drama-compose\\1\\watermark.txt");

        // Windows 路径：盘符的 : 需要转义，否则 FFmpeg 会在 : 处截断 drawtext 参数；\ 转义后原样保留
        assertTrue(filter.contains("drawtext=textfile='C\\:\\\\Users\\\\yudao\\\\AppData\\\\Local\\\\Temp"
                + "\\\\ai-drama-compose\\\\1\\\\watermark.txt':expansion=none"
                + ":fontfile='C\\:\\\\Windows\\\\Fonts\\\\msyh.ttc':fontsize="));
    }

    @Test
    public void testEscapeFilterPath() {
        // Linux 普通路径：只加单引号
        assertEquals("'/tmp/ai-drama-compose/1/watermark.txt'",
                AiFfmpegUtils.escapeFilterPath("/tmp/ai-drama-compose/1/watermark.txt"));
        // Windows 路径：盘符的 : 转义为 \:，\ 转义为 \\，不替换为 /
        assertEquals("'C\\:\\\\Windows\\\\Fonts\\\\msyh.ttc'", AiFfmpegUtils.escapeFilterPath("C:\\Windows\\Fonts\\msyh.ttc"));
        // Linux 文件名可以包含 \，不能当作分隔符替换
        assertEquals("'/data/a\\\\b/watermark.txt'", AiFfmpegUtils.escapeFilterPath("/data/a\\b/watermark.txt"));
        // 单引号：参数层转义为 \'，滤镜图层再把其中的 ' 写为 '\''（闭合引号、转义、重新打开）
        assertEquals("'C\\:\\\\Users\\\\O\\'\\''Brien\\\\watermark.txt'",
                AiFfmpegUtils.escapeFilterPath("C:\\Users\\O'Brien\\watermark.txt"));
    }

    @Test
    public void testBuildFilterComplex_transitionTooLong() {
        List<AiFfmpegUtils.Clip> clips = Arrays.asList(
                new AiFfmpegUtils.Clip(null, 5, true), new AiFfmpegUtils.Clip(null, 1, true));

        assertThrows(IllegalArgumentException.class, () -> AiFfmpegUtils.buildFilterComplex(clips, 720, 1280,
                AiDramaTransitionTypeEnum.DISSOLVE, 1.0, false, null, null, null));
    }

    @Test
    public void testBuildFilterComplex_germanLocale() {
        // 德语等 Locale 的小数分隔符为逗号：FFmpeg 参数仍需输出 0.5
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
        try {
            List<AiFfmpegUtils.Clip> clips = Arrays.asList(
                    new AiFfmpegUtils.Clip(null, 5, true), new AiFfmpegUtils.Clip(null, 4.5, true));

            String filter = AiFfmpegUtils.buildFilterComplex(clips, 720, 1280, AiDramaTransitionTypeEnum.FADE,
                    0.5, false, null, null, null);

            assertTrue(filter.contains("atrim=0:4.5,"));
            assertTrue(filter.contains("[v0][v1]xfade=transition=fadeblack:duration=0.5:offset=4.5[vx1]"));
            assertTrue(filter.contains("[a0][a1]acrossfade=d=0.5[ax1]"));
            assertEquals("1234.5", AiFfmpegUtils.formatSeconds(1234.5));
            assertEquals("3", AiFfmpegUtils.formatSeconds(3.0));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

}
