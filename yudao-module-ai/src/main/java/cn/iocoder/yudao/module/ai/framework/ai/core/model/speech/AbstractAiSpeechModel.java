package cn.iocoder.yudao.module.ai.framework.ai.core.model.speech;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.springframework.ai.audio.tts.Speech;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.audio.tts.TextToSpeechResponseMetadata;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 同步语音合成模型的基类：一次调用返回完整音频
 *
 * 基于 Spring AI 的 {@link TextToSpeechModel} 契约：{@link TextToSpeechOptions} 的 model、voice、format、speed
 * 正好覆盖产品参数。模型在创建时绑定，请求不能切换；不支持的参数直接拒绝；校验全部在付费调用前完成。
 * 产品只做非流式播放，所以 {@link #stream(TextToSpeechPrompt)} 明确不支持。
 *
 * 响应的 {@link TextToSpeechResponseMetadata}：{@link #METADATA_FORMAT} 为音频格式；
 * 其它键为服务商的请求编号等排查信息，只在服务商实际返回时写入，不能作为可轮询的任务编号
 *
 * @author 芋道源码
 */
public abstract class AbstractAiSpeechModel implements TextToSpeechModel, AiSpeechModel {

    /**
     * 元数据：音频格式
     */
    public static final String METADATA_FORMAT = "format";

    /**
     * 绑定的模型标识
     */
    protected final String model;

    protected AbstractAiSpeechModel(String model) {
        if (StrUtil.isBlank(model)) {
            throw new IllegalArgumentException("语音模型不能为空");
        }
        this.model = model;
    }

    @Override
    public TextToSpeechResponse call(TextToSpeechPrompt prompt) {
        if (prompt == null || prompt.getInstructions() == null) {
            throw new IllegalArgumentException("语音合成请求不能为空");
        }
        // 1. 校验参数：全部在付费调用之前
        TextToSpeechOptions options = prompt.getOptions();
        String text = validateText(prompt.getInstructions().getText());
        String voice = null;
        String format = null;
        Double speed = null;
        if (options != null) {
            AiSpeechUtils.validateModel(model, options.getModel());
            voice = options.getVoice();
            format = options.getFormat();
            speed = options.getSpeed();
        }
        voice = validateVoice(voice);
        format = AiSpeechUtils.validateFormat(format, getDefaultFormat(), getSupportedFormats());
        BigDecimal speedValue = AiSpeechUtils.validateSpeed(speed, getMinSpeed(), getMaxSpeed());

        // 2. 调用服务商，并校验音频
        SpeechResult result = doCall(text, voice, format, speedValue);
        byte[] audio = AiSpeechUtils.validateAudio(result.getAudio(), format);

        // 3. 返回
        TextToSpeechResponseMetadata metadata = new TextToSpeechResponseMetadata();
        metadata.put(METADATA_FORMAT, format);
        result.getMetadata().forEach((key, value) -> {
            if (StrUtil.isNotBlank(value)) {
                metadata.put(key, value);
            }
        });
        return new TextToSpeechResponse(Collections.singletonList(new Speech(audio)), metadata);
    }

    @Override
    public Flux<TextToSpeechResponse> stream(TextToSpeechPrompt prompt) {
        return Flux.error(new UnsupportedOperationException("语音合成暂不支持流式输出"));
    }

    @Override
    public TextToSpeechOptions getDefaultOptions() {
        return TextToSpeechOptions.builder().model(model).format(getDefaultFormat()).speed(1.0).build();
    }

    public String getModel() {
        return model;
    }

    /**
     * 校验合成参数：不发请求；{@link #call(TextToSpeechPrompt)} 时同样校验
     */
    @Override
    public void validateOptions(String text, String voice, String format, Double speed) {
        validateText(text);
        validateVoice(voice);
        AiSpeechUtils.validateFormat(format, getDefaultFormat(), getSupportedFormats());
        AiSpeechUtils.validateSpeed(speed, getMinSpeed(), getMaxSpeed());
    }

    /**
     * 合成：一次请求拿到完整音频，不回调任务编号
     */
    @Override
    public byte[] synthesize(String text, String voice, String format, Double speed, Consumer<String> taskIdConsumer) {
        return call(new TextToSpeechPrompt(text, TextToSpeechOptions.builder()
                .voice(voice).format(format).speed(speed).build())).getResult().getOutput();
    }

    /**
     * 校验朗读文本，默认按字符数（Unicode 码点）
     */
    protected String validateText(String text) {
        return AiSpeechUtils.validateText(text, getMaxTextLength());
    }

    /**
     * 校验音色，默认只要求非空
     */
    protected String validateVoice(String voice) {
        return AiSpeechUtils.validateVoice(voice);
    }

    /**
     * @return 朗读文本的最大字符数
     */
    protected abstract int getMaxTextLength();

    /**
     * @return 支持的音频格式
     */
    public abstract Collection<String> getSupportedFormats();

    /**
     * @return 默认音频格式
     */
    public abstract String getDefaultFormat();

    /**
     * @return 最小语速倍数
     */
    public abstract BigDecimal getMinSpeed();

    /**
     * @return 最大语速倍数
     */
    public abstract BigDecimal getMaxSpeed();

    /**
     * 调用服务商合成音频：参数已校验；只调用一次，不自动重试
     *
     * @param text 朗读文本
     * @param voice 音色
     * @param format 音频格式
     * @param speed 语速倍数，最多两位小数
     * @return 音频与排查信息
     */
    protected abstract SpeechResult doCall(String text, String voice, String format, BigDecimal speed);

    /**
     * 服务商返回的结果
     */
    @Getter
    protected static class SpeechResult {

        /**
         * 音频
         */
        private final byte[] audio;
        /**
         * 排查信息（请求编号等），值为空时不写入
         */
        private final Map<String, String> metadata;

        public SpeechResult(byte[] audio, Map<String, String> metadata) {
            this.audio = audio;
            this.metadata = metadata == null ? Collections.<String, String>emptyMap() : metadata;
        }

        @Override
        public String toString() {
            return "SpeechResult{audioLength=" + (audio == null ? 0 : audio.length) + "}";
        }

    }

}
