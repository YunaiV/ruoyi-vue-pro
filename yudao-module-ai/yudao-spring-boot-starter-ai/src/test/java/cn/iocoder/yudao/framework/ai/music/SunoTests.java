package cn.iocoder.yudao.framework.ai.music;

import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import org.junit.jupiter.api.Test;

import java.util.List;

// TODO 芋艿：整理单测
/**
 * @Author xiaoxin
 * @Date 2024/5/27
 */
public class SunoTests {

    private final SunoApi sunoApi = new SunoApi("https://suno-55ishh05u-status2xxs-projects.vercel.app");
//    private final SunoApi sunoApi = new SunoApi("http://127.0.0.1:3001");

    @Test
    public void selectById() {
        System.out.println(sunoApi.getMusicList(List.of("d460ddda-7c87-4f34-b751-419b08a590ca,ff90ea66-49cd-4fd2-b44c-44267dfd5551")));
    }

    @Test
    public void generate() {
        List<SunoApi.MusicData> generate = sunoApi.generate(new SunoApi.MusicGenerateRequest("创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。"));
        System.out.println(generate);
    }


    @Test
    public void doChatCompletion() {
        List<SunoApi.MusicData> generate = sunoApi.chatCompletion("创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。");
        System.out.println(generate);
    }


    @Test
    public void generateLyrics() {
        SunoApi.LyricsData lyricsData = sunoApi.generateLyrics("A soothing lullaby");
        System.out.println(lyricsData);
    }


    @Test
    public void selectLimit() {
        SunoApi.LimitUsageData limitUsageData = sunoApi.getLimitUsage();
        System.out.println(limitUsageData);
    }

}
