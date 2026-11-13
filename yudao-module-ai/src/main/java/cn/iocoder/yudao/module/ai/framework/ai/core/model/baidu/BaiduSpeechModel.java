package cn.iocoder.yudao.module.ai.framework.ai.core.model.baidu;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 百度智能云的语音合成模型：短文本在线合成（新版接口，API Key 鉴权）
 *
 * 1. POST {baseUrl}/rest/2.0/speech/publiccloudspeech/v1/tts，请求头 Authorization: Bearer API Key；
 *    不做 OAuth 换 token，也不同时传 access_token
 * 2. text 按 GBK 编码必须小于 1024 字节（无法用 GBK 表示的字符直接拒绝，不替换）；请求体不超过 64KB
 * 3. 失败时 HTTP 状态仍为 200，响应为 JSON（code、message、sn）；只有 Content-Type 为对应格式的音频、且文件头一致才算成功
 * 4. 接口没有模型参数：绑定的模型只是配置标识，不下发
 *
 * @see <a href="https://ai.baidu.com/ai-doc/SPEECH/xmw03avls">百度短文本在线合成</a>
 * @see <a href="https://cloud.baidu.com/doc/AI_REFERENCE/s/Hm5us339w">百度 API Key 鉴权</a>
 * @author 芋道源码
 */
@Slf4j
public class BaiduSpeechModel extends AbstractAiSpeechModel {

    public static final String DEFAULT_BASE_URL = "https://aip.baidubce.com";
    private static final String PATH = "/rest/2.0/speech/publiccloudspeech/v1/tts";

    /**
     * text 的 GBK 字节数上限（不含）
     */
    private static final int MAX_TEXT_GBK_BYTES = 1024;
    /**
     * 请求体的最大字节数
     */
    private static final int MAX_BODY_SIZE = 64 * 1024;
    private static final Charset GBK = Charset.forName("GBK");
    private static final List<String> FORMATS = ListUtil.of(AiSpeechUtils.FORMAT_MP3, AiSpeechUtils.FORMAT_WAV);
    private static final BigDecimal MIN_SPEED = new BigDecimal("0.5");
    private static final BigDecimal MAX_SPEED = new BigDecimal("2");
    /**
     * 各格式可接受的 Content-Type（不含参数）
     */
    private static final Map<String, Set<String>> CONTENT_TYPES = ImmutableMap.of(
            AiSpeechUtils.FORMAT_MP3, SetUtils.asSet("audio/mp3", "audio/mpeg", "audio/mpeg3", "audio/x-mpeg"),
            AiSpeechUtils.FORMAT_WAV, SetUtils.asSet("audio/wav", "audio/x-wav", "audio/wave", "audio/vnd.wave"));

    private final String apiKey;
    private final WebClient webClient;
    private final Duration timeout;

    public BaiduSpeechModel(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, AiSpeechUtils.REQUEST_TIMEOUT, AiSpeechUtils.MAX_AUDIO_SIZE);
    }

    public BaiduSpeechModel(String baseUrl, String apiKey, String model, Duration timeout, int maxAudioSize) {
        super(model);
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("百度语音合成的 API Key 不能为空");
        }
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.webClient = AiSpeechUtils.buildWebClient(
                StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/"),
                maxAudioSize, headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(apiKey);
                });
    }

    /**
     * 校验朗读文本：严格按 GBK 编码，小于 1024 字节
     *
     * 注意：120 个 GBK 字节是计费单位，不是输入上限
     */
    @Override
    protected String validateText(String text) {
        if (StrUtil.isBlank(text)) {
            throw new IllegalArgumentException("朗读文本不能为空");
        }
        int length = getGbkLength(text);
        if (length >= MAX_TEXT_GBK_BYTES) {
            throw new IllegalArgumentException(StrUtil.format("朗读文本按 GBK 编码为 {} 字节，需小于 {} 字节",
                    length, MAX_TEXT_GBK_BYTES));
        }
        return text;
    }

    /**
     * 计算 GBK 编码的字节数：无法编码的字符报错，不替换为问号
     */
    static int getGbkLength(String text) {
        CharsetEncoder encoder = GBK.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return encoder.encode(CharBuffer.wrap(text)).remaining();
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException("朗读文本包含 GBK 无法表示的字符（如部分 emoji、生僻字），请修改后重试");
        }
    }

    @Override
    protected SpeechResult doCall(String text, String voice, String format, BigDecimal speed) {
        // 1. 请求：lang、sample_rate 不传，使用服务端默认的 zh、16000
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        body.put("voice", voice);
        body.put("format", format);
        body.put("speed", speed);
        byte[] bodyBytes = JsonUtils.toJsonByte(body);
        if (bodyBytes.length > MAX_BODY_SIZE) {
            throw new IllegalArgumentException("百度语音合成的请求体超过 64KB");
        }
        AiSpeechUtils.HttpResult result = AiSpeechUtils.exchange(webClient.post().uri(PATH).bodyValue(bodyBytes),
                timeout, "百度语音合成");

        // 2. 解析：只有对应格式的音频才算成功；JSON（即使 HTTP 200）都是失败
        MediaType contentType = result.getHeaders().getContentType();
        String type = contentType != null ? (contentType.getType() + "/" + contentType.getSubtype()).toLowerCase() : "";
        if (!result.is2xx() || !CONTENT_TYPES.getOrDefault(format, Collections.emptySet()).contains(type)) {
            throw buildException(result, type);
        }
        return new SpeechResult(result.getBody(), Collections.emptyMap());
    }

    private IllegalStateException buildException(AiSpeechUtils.HttpResult result, String type) {
        JsonNode root = AiSpeechUtils.parseJson(result.getBody());
        if (root != null && root.isObject()) {
            String code = root.path("code").asText("-");
            String sn = root.path("sn").asText("-");
            log.warn("[BaiduSpeech][合成失败，HTTP {}，code {}，sn {}]", result.getStatus(), code, sn);
            return new IllegalStateException(StrUtil.format("百度语音合成失败（HTTP {}，code={}）：{}，sn={}",
                    result.getStatus(), code, AiSpeechUtils.sanitize(root.path("message").asText(""), apiKey), sn));
        }
        log.warn("[BaiduSpeech][合成失败，HTTP {}，Content-Type {}]", result.getStatus(), type);
        return new IllegalStateException(StrUtil.format("百度语音合成失败（HTTP {}）：返回类型({})不是预期的音频",
                result.getStatus(), StrUtil.blankToDefault(type, "-")));
    }

    @Override
    protected int getMaxTextLength() {
        return MAX_TEXT_GBK_BYTES - 1;
    }

    @Override
    public Collection<String> getSupportedFormats() {
        return FORMATS;
    }

    @Override
    public String getDefaultFormat() {
        return AiSpeechUtils.FORMAT_MP3;
    }

    @Override
    public BigDecimal getMinSpeed() {
        return MIN_SPEED;
    }

    @Override
    public BigDecimal getMaxSpeed() {
        return MAX_SPEED;
    }

}
