package cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import com.alibaba.cloud.ai.dashscope.api.tts.DashScopeQwenTTSApi;
import com.alibaba.cloud.ai.dashscope.audio.tts.DashScopeAudioSpeechOptions;
import com.alibaba.cloud.ai.dashscope.audio.tts.DashScopeTTSApiSpec.DashScopeAudioTTSResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 通义千问（阿里云百炼）的语音合成模型：Qwen-TTS 非流式 HTTP
 *
 * 1. 使用 spring-ai-alibaba-dashscope 的 {@link DashScopeQwenTTSApi} 构建请求、解析响应；
 *    传输层沿用 {@link AiSpeechUtils#buildHttpClient()}：不自动重发、不跟随重定向，单次请求有超时
 * 2. 响应 output.audio.url 为 24 小时有效的临时地址，这里有界下载为字节，不保存临时地址。
 *    不使用 DashScopeAudioSpeechModel：只返回地址时，它给出的是空音频
 * 3. 只放行接口文档中的 qwen3-tts-flash；CosyVoice、Qwen-Audio 等其它系列协议不同，不隐式放行
 * 4. 输出固定为 wav；接口没有语速参数，语速只能为 1
 *
 * @see <a href="https://help.aliyun.com/zh/model-studio/qwen-tts-api">Qwen-TTS API</a>
 * @author 芋道源码
 */
@Slf4j
public class TongYiSpeechModel extends AbstractAiSpeechModel {

    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com";

    /**
     * 支持的模型
     */
    public static final Set<String> MODELS = SetUtils.asSet("qwen3-tts-flash");
    /**
     * 朗读文本的最大字符数
     */
    private static final int MAX_TEXT_LENGTH = 600;
    private static final List<String> FORMATS = ListUtil.of(AiSpeechUtils.FORMAT_WAV);

    public static final String METADATA_REQUEST_ID = "requestId";

    private final DashScopeQwenTTSApi api;
    private final Duration timeout;
    private final int maxAudioSize;

    public TongYiSpeechModel(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, AiSpeechUtils.REQUEST_TIMEOUT, AiSpeechUtils.MAX_AUDIO_SIZE);
    }

    public TongYiSpeechModel(String baseUrl, String apiKey, String model, Duration timeout, int maxAudioSize) {
        super(model);
        if (!MODELS.contains(model)) {
            throw new IllegalArgumentException(StrUtil.format("通义语音合成不支持模型({})，可选：{}", model, MODELS));
        }
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("通义语音合成的 API Key 不能为空");
        }
        this.timeout = timeout;
        this.maxAudioSize = maxAudioSize;
        ReactorClientHttpRequestFactory requestFactory = new ReactorClientHttpRequestFactory(AiSpeechUtils.buildHttpClient());
        requestFactory.setReadTimeout(timeout);
        requestFactory.setExchangeTimeout(timeout);
        this.api = DashScopeQwenTTSApi.builder()
                .baseUrl(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL))
                .apiKey(new SimpleApiKey(apiKey))
                .restClientBuilder(RestClient.builder().requestFactory(requestFactory))
                .webClientBuilder(WebClient.builder()) // 只用于流式合成，这里不使用
                .responseErrorHandler(new ErrorHandler(apiKey))
                .build();
    }

    @Override
    protected SpeechResult doCall(String text, String voice, String format, BigDecimal speed) {
        // 1. 合成：language_type 不传，使用服务端默认的 Auto；非 2xx 由 ErrorHandler 转为异常
        DashScopeAudioTTSResponse response;
        try {
            response = api.call(text, DashScopeAudioSpeechOptions.builder().model(model).voice(voice).build());
        } catch (RestClientException ex) {
            // 网络异常、超时、响应无法解析：付费请求可能已被处理，不自动重试
            throw new IllegalStateException(StrUtil.format("通义语音合成失败：{}，结果未知，请勿自动重试",
                    ExceptionUtil.getRootCause(ex).getClass().getSimpleName()));
        }

        // 2. 校验：必须返回音频地址
        String requestId = response != null ? response.getRequestId() : null;
        String url = response != null && response.getOutput() != null && response.getOutput().audio() != null
                ? response.getOutput().audio().url() : null;
        if (StrUtil.isBlank(url)) {
            throw new IllegalStateException(StrUtil.format("通义语音合成未返回音频地址，request_id={}",
                    StrUtil.blankToDefault(requestId, "-")));
        }

        // 3. 下载临时地址：不携带 API Key
        byte[] audio = AiSpeechUtils.download(url, format, timeout, maxAudioSize);
        return new SpeechResult(audio, Collections.singletonMap(METADATA_REQUEST_ID, requestId));
    }

    @Override
    protected int getMaxTextLength() {
        return MAX_TEXT_LENGTH;
    }

    @Override
    public Collection<String> getSupportedFormats() {
        return FORMATS;
    }

    @Override
    public String getDefaultFormat() {
        return AiSpeechUtils.FORMAT_WAV;
    }

    @Override
    public BigDecimal getMinSpeed() {
        return BigDecimal.ONE;
    }

    @Override
    public BigDecimal getMaxSpeed() {
        return BigDecimal.ONE;
    }

    /**
     * 错误响应的处理：非 2xx（含未跟随的重定向）都是失败；只取 code、message、request_id，脱敏后抛出，不输出原始响应
     */
    private static class ErrorHandler implements ResponseErrorHandler {

        private final String apiKey;

        private ErrorHandler(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return !response.getStatusCode().is2xxSuccessful();
        }

        @Override
        public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
            int status = response.getStatusCode().value();
            JsonNode root = AiSpeechUtils.parseJson(IoUtil.readBytes(response.getBody(), AiSpeechUtils.MAX_JSON_SIZE));
            String requestId = root != null ? StrUtil.emptyToNull(root.path("request_id").asText(null)) : null;
            String code = root != null ? StrUtil.emptyToNull(root.path("code").asText(null)) : null;
            // 非 JSON 的错误响应（如网关页面）不放进异常信息
            String message = root != null ? root.path("message").asText("") : "响应不是 JSON";
            log.warn("[TongYiSpeech][合成失败，HTTP {}，code {}，requestId {}]", status, code, requestId);
            throw new IllegalStateException(StrUtil.format("通义语音合成失败（HTTP {}，{}）：{}，request_id={}",
                    status, StrUtil.blankToDefault(code, "-"), AiSpeechUtils.sanitize(message, apiKey),
                    StrUtil.blankToDefault(requestId, "-")));
        }

        @Override
        public String toString() {
            return "ErrorHandler";
        }

    }

}
