package cn.iocoder.yudao.framework.ai.music;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * {@link SunoApi} 集成测试
 *
 * @author xiaoxin
 */
public class SunoApiTests {

    private final SunoApi sunoApi = new SunoApi("https://suno-3tah0ycyt-status2xxs-projects.vercel.app");
//    private final SunoApi sunoApi = new SunoApi("http://127.0.0.1:3001");

    @Test // 描述模式
    @Disabled
    public void testGenerate() {
        // 准备参数
        SunoApi.MusicGenerateRequest generateRequest = new SunoApi.MusicGenerateRequest(
                "happy music",
                "chirp-v3-5",
                false);

        // 调用方法
        List<SunoApi.MusicData> musicList = sunoApi.generate(generateRequest);
        // 打印结果
        System.out.println(musicList);
    }

    @Test // 歌词模式
    @Disabled
    public void testCustomGenerate() {
        // 准备参数
        SunoApi.MusicGenerateRequest generateRequest = new SunoApi.MusicGenerateRequest(
                "创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。",
                "Happy",
                "Happy Song",
                "chirp-v3.5",
                false,
                false);

        // 调用方法
        List<SunoApi.MusicData> musicList = sunoApi.customGenerate(generateRequest);
        // 打印结果
        System.out.println(musicList);
    }

    @Test
    @Disabled
    public void testGenerateLyrics() {
        // 调用方法
        SunoApi.LyricsData lyricsData = sunoApi.generateLyrics("A soothing lullaby");
        // 打印结果
        System.out.println(lyricsData);
    }

    @Test
    @Disabled
    public void testGetMusicList() {
        // 准备参数
//        String id = "d460ddda-7c87-4f34-b751-419b08a590ca";
        String id = "584729e5-0fe9-4157-86da-1b4803ff42bf";

        // 调用方法
        List<SunoApi.MusicData> musicList = sunoApi.getMusicList(ListUtil.of(id));
        // 打印结果
        System.out.println(musicList);
    }

    @Test
    @Disabled
    public void testGetLimitUsage() {
        // 调用方法
        SunoApi.LimitUsageData limitUsageData = sunoApi.getLimitUsage();
        // 打印结果
        System.out.println(limitUsageData);
    }

}
