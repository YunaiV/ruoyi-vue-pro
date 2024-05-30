package cn.iocoder.yudao.framework.ai.core.model.suno.api;

import cn.iocoder.yudao.framework.ai.core.model.suno.SunoConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Suno API
 * <br>
 * 文档地址：https://platform.acedata.cloud/documents/d016ee3f-421b-4b6e-989a-8beba8701701
 *
 * @Author xiaoxin
 * @Date 2024/5/27
 */
@Slf4j
public class SunoApi {

    public static final String DEFAULT_BASE_URL = "https://api.acedata.cloud/suno";
    private final WebClient webClient;

    public SunoApi(SunoConfig config) {
        this.webClient = WebClient.builder()
                .baseUrl(DEFAULT_BASE_URL)
                .defaultHeaders(ApiUtils.getJsonContentHeaders(config.getToken()))
                .build();
    }

    // TODO @芋艿：方法名，要考虑下；
    public SunoResp musicGen(SunoReq sunReq) {
        return this.webClient.post()
                .uri("/audios")
                .body(Mono.just(sunReq), SunoReq.class)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .handle((respBody, sink) -> {
                                    log.error("【Suno】调用失败！resp: 【{}】", respBody);
                                    sink.error(new IllegalStateException("【Suno】调用失败！"));
                                }))
                .bodyToMono(SunoResp.class)
                .block();
    }

    /**
     * 请求数据对象，用于生成音乐音频。
     *
     * @param prompt      用于生成音乐音频的提示
     * @param lyric       用于生成音乐音频的歌词
     * @param custom      指示音乐音频是否为定制，如果为 true，则从歌词生成，否则从提示生成
     * @param title       音乐音频的标题
     * @param style       音乐音频的风格
     * @param callbackUrl 音乐音频生成后回调的 URL
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record SunoReq(
            String prompt,
            String lyric,
            boolean custom,
            String title,
            String style,
            String callbackUrl
    ) {
        public SunoReq(String prompt) {
            this(prompt, null, false, null, null, null);
        }

    }

    /**
     * SunoAPI 响应的数据。
     *
     * @param success 表示请求是否成功
     * @param taskId  任务 ID
     * @param data    音乐数据列表
     */
    public record SunoResp(
            boolean success,
            @JsonProperty("task_id") String taskId,
            List<MusicData> data
    ) {
        /**
         * 单个音乐数据。
         *
         * @param id        音乐数据的 ID
         * @param title     音乐音频的标题
         * @param imageUrl  音乐音频的图片 URL
         * @param lyric     音乐音频的歌词
         * @param audioUrl  音乐音频的 URL
         * @param videoUrl  音乐视频的 URL
         * @param createdAt 音乐音频的创建时间
         * @param model     使用的模型名称
         * @param prompt    生成音乐音频的提示
         * @param style     音乐音频的风格
         */
        public record MusicData(
                String id,
                String title,
                @JsonProperty("image_url") String imageUrl,
                String lyric,
                @JsonProperty("audio_url") String audioUrl,
                @JsonProperty("video_url") String videoUrl,
                @JsonProperty("created_at") String createdAt,
                String model,
                String prompt,
                String style
        ) {
        }
    }
}
