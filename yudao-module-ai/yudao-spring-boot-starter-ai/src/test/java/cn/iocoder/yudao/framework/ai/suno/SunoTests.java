package cn.iocoder.yudao.framework.ai.suno;

import cn.iocoder.yudao.framework.ai.core.model.suno.SunoConfig;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author xiaoxin
 * @Date 2024/5/27
 */
public class SunoTests {

    private SunoConfig sunoConfig;

    @Before
    public void setup() {
        String token = "16b4356581984d538652354b60d69ff0";
        this.sunoConfig = new SunoConfig(token);
    }

    @Test
    public void generateMusic() {
        SunoApi sunoApi = new SunoApi(sunoConfig);
        SunoApi.SunoReq sunoReq = new SunoApi.SunoReq()
                .setPrompt("创作一首带有轻松吉他旋律的流行歌曲，[verse] 描述夏日海滩的宁静，[chorus] 节奏加快，表达对自由的向往。");
        SunoApi.SunoResp sunoResp = sunoApi.musicGen(sunoReq);
        System.out.println(sunoResp);
    }

}
