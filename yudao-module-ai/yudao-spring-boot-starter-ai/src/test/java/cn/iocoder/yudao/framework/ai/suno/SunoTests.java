package cn.iocoder.yudao.framework.ai.suno;

import cn.iocoder.yudao.framework.ai.core.model.suno.SunoApi;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author xiaoxin
 * @Date 2024/5/27
 */
public class SunoTests {


    @Test
    public void generateMusic() throws IOException {
        SunoApi sunoApi = new SunoApi();
        SunoApi.SunoRequest sunoRequest = new SunoApi
                .SunoRequest()
                .setPrompt("创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。");
        SunoApi.SunoResponse sunoResponse = sunoApi.generateMusic(sunoRequest);
        System.out.println(sunoResponse);
    }
}
