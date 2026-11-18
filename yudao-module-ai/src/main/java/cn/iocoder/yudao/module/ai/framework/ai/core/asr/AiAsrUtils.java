package cn.iocoder.yudao.module.ai.framework.ai.core.asr;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * AI 语音识别的工具类：音频读取、校验
 *
 * HTTP 请求、错误信息脱敏等通用能力，复用 {@link AiSpeechUtils}
 *
 * @author 芋道源码
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AiAsrUtils {

    /**
     * 读取语音识别的音频：不能为空
     */
    public static byte[] readAudio(Resource resource) {
        Assert.notNull(resource, "音频不能为空");
        byte[] audio;
        try {
            audio = resource.getContentAsByteArray();
        } catch (IOException ex) {
            throw new IllegalArgumentException("音频读取失败", ex);
        }
        Assert.isTrue(ArrayUtil.isNotEmpty(audio), "音频不能为空");
        return audio;
    }

    /**
     * 校验语音识别的音频：必须是 WAV（前端统一转为 16kHz 单声道 16 位 PCM WAV），且不超过大小上限
     */
    public static byte[] validateWavAudio(byte[] audio, int maxSize) {
        Assert.isTrue(AiSpeechUtils.isAudioFormat(audio, AiSpeechUtils.FORMAT_WAV), "音频必须是 WAV 格式");
        Assert.isTrue(audio.length <= maxSize, "音频大小({} 字节)超过上限({} 字节)", audio.length, maxSize);
        return audio;
    }

}
