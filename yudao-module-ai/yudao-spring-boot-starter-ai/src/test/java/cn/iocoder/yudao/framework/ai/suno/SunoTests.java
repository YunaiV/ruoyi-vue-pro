package cn.iocoder.yudao.framework.ai.suno;

import cn.iocoder.yudao.framework.ai.core.model.suno.SunoConfig;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @Author xiaoxin
 * @Date 2024/5/27
 */
public class SunoTests {

    SunoApi sunoApi;

    @Before
    public void setup() {
        String url = "https://suno-ix9nve79x-status2xxs-projects.vercel.app";
        this.sunoApi = new SunoApi(new SunoConfig(url));
    }

    @Test
    public void selectById() {
        System.out.println(sunoApi.selectById("d460ddda-7c87-4f34-b751-419b08a590ca,ff90ea66-49cd-4fd2-b44c-44267dfd5551"));

    }

    @Test
    public void generate() {
        List<SunoApi.MusicData> generate = sunoApi.generate(new SunoApi.SunoReq("创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。"));
        System.out.println(generate);
    }


    @Test
    public void doChatCompletion() {
        List<SunoApi.MusicData> generate = sunoApi.doChatCompletion("创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。");
        System.out.println(generate);
    }


    @Test
    public void generateLyrics() {
        SunoApi.LyricsData lyricsData = sunoApi.generateLyrics("A soothing lullaby");
        System.out.println(lyricsData);
    }


    @Test
    public void selectLimit() {
        SunoApi.LimitData limitData = sunoApi.selectLimit();
        System.out.println(limitData);
    }


}
