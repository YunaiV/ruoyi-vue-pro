package cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * 腾讯云语音模型：把长文本语音合成的原生异步任务，适配为「调用一次，拿到完整音频」
 *
 * 只提交一次（{@link TencentTtsApi#createTask}），立即交出任务编号；再在 {@link #WAIT_TIMEOUT} 内轮询结果
 * （{@link TencentTtsApi#waitTask}，只查询）。长文本由服务端处理，不手工切分、拼接音频
 *
 * @author 芋道源码
 */
public class TencentSpeechModel implements AiSpeechModel {

    /**
     * 等待结果的时限：与同步合成单次请求的时限一致
     */
    public static final Duration WAIT_TIMEOUT = AiSpeechUtils.REQUEST_TIMEOUT;
    /**
     * 查询间隔（查询免费，不产生费用）
     */
    public static final Duration POLL_INTERVAL = Duration.ofSeconds(3);

    private final TencentTtsApi api;

    public TencentSpeechModel(TencentTtsApi api) {
        this.api = api;
    }

    @Override
    public void validateOptions(String text, String voice, String format, Double speed) {
        api.validateOptions(text, voice, format, speed);
    }

    @Override
    public byte[] synthesize(String text, String voice, String format, Double speed, Consumer<String> taskIdConsumer) {
        // 1. 提交一次，立即交出任务编号
        String taskId = api.createTask(text, voice, format, speed).getTaskId();
        taskIdConsumer.accept(taskId);
        // 2. 时限内等待结果：成功返回音频；失败抛出；未完成返回 null
        TencentTtsApi.TtsTaskResult result = api.waitTask(taskId, format, WAIT_TIMEOUT, POLL_INTERVAL);
        if (result == null || !result.getStatus().isFinished()) {
            return null;
        }
        if (result.getStatus() == TencentTtsApi.TtsTaskStatus.FAILED) {
            throw new IllegalStateException(StrUtil.blankToDefault(result.getErrorMessage(), "腾讯云语音合成任务失败"));
        }
        return result.getAudio();
    }

}
