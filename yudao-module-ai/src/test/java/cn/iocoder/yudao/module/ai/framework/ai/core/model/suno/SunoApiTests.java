package cn.iocoder.yudao.module.ai.framework.ai.core.model.suno;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

// DONE @AI：挪到 suno 目录（package 已对齐）
/**
 * {@link SunoApi} 集成测试：需要可用的 New API 中转地址与密钥，手动执行
 *
 * @author xiaoxin
 */
public class SunoApiTests {

    private final SunoApi sunoApi = new SunoApi("https://api.example.com", "sk-xxx");

    @Test // 灵感模式
    @Disabled
    public void testSubmitMusic_inspiration() {
        // 准备参数
        SunoApi.MusicRequest request = new SunoApi.MusicRequest("一首关于夏日海边的轻快流行歌",
                null, null, null, "chirp-v4", false, null, null, null);

        // 调用方法
        String taskId = sunoApi.submitMusic(request);
        // 打印结果
        System.out.println(taskId);
    }

    @Test // 自定义模式
    @Disabled
    public void testSubmitMusic_custom() {
        // 准备参数
        SunoApi.MusicRequest request = new SunoApi.MusicRequest(null,
                "[Verse]\n海风吹过沙滩\n[Chorus]\n我们一起奔跑", "pop, female vocals", "夏日海风",
                "chirp-v4", false, null, null, null);

        // 调用方法
        String taskId = sunoApi.submitMusic(request);
        // 打印结果
        System.out.println(taskId);
    }

    @Test // 续写
    @Disabled
    public void testSubmitMusic_extend() {
        // 准备参数
        SunoApi.MusicRequest request = new SunoApi.MusicRequest(null,
                "[Verse 2]\n夕阳落在海面", "pop", "夏日海风（续）",
                "chirp-v4", false, "task-id", "clip-id", 60);

        // 调用方法
        String taskId = sunoApi.submitMusic(request);
        // 打印结果
        System.out.println(taskId);
    }

    @Test
    @Disabled
    public void testSubmitConcat() {
        String taskId = sunoApi.submitConcat("clip-id");
        System.out.println(taskId);
    }

    @Test
    @Disabled
    public void testUploadAudioUrl() {
        String taskId = sunoApi.uploadAudioUrl("https://example.com/demo.mp3");
        System.out.println(taskId);
    }

    @Test
    @Disabled
    public void testFetchTasks() {
        List<SunoApi.Task> tasks = sunoApi.fetchTasks(List.of("task-id"));
        System.out.println(tasks);
    }

}
