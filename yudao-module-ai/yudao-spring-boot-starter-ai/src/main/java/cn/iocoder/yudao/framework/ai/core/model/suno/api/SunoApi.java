package cn.iocoder.yudao.framework.ai.core.model.suno.api;

import cn.iocoder.yudao.framework.ai.core.model.suno.SunoConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
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

    // TODO @xiaoxin：看看是不是使用 record 特性，简化下；

    /**
     * 请求数据对象，用于生成音乐音频
     */
    @Data
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class SunoReq {
        /**
         * 用于生成音乐音频的提示
         */
        private String prompt;

        /**
         * 用于生成音乐音频的歌词
         */
        private String lyric;

        /**
         * 指示音乐音频是否为定制，如果为 true，则从歌词生成，否则从提示生成
         */
        private boolean custom;

        /**
         * 音乐音频的标题
         */
        private String title;

        /**
         * 音乐音频的风格
         */
        private String style;

        /**
         * 音乐音频生成后回调的 URL
         */
        private String callbackUrl;
    }

    // TODO @xiaoxin：看看是不是使用 record 特性，简化下；

    /**
     * SunoAPI 响应的数据
     */
    @Data
    public static class SunoResp {
        /**
         * 表示请求是否成功
         */
        private boolean success;

        /**
         * 任务 ID
         */
        @JsonProperty("task_id")
        private String taskId;

        /**
         * 音乐数据列表
         */
        private List<MusicData> data;

        /**
         * 表示单个音乐数据的类
         */
        @Data
        static class MusicData {
            /**
             * 音乐数据的 ID
             */
            private String id;

            /**
             * 音乐音频的标题
             */
            private String title;

            /**
             * 音乐音频的图片 URL
             */
            @JsonProperty("image_url")
            private String imageUrl;

            /**
             * 音乐音频的歌词
             */
            private String lyric;

            /**
             * 音乐音频的 URL
             */
            @JsonProperty("audio_url")
            private String audioUrl;

            /**
             * 音乐视频的 URL
             */
            @JsonProperty("video_url")
            private String videoUrl;

            /**
             * 音乐音频的创建时间
             */
            @JsonProperty("created_at")
            private String createdAt;

            /**
             * 使用的模型名称
             */
            private String model;

            /**
             * 生成音乐音频的提示
             */
            private String prompt;

            /**
             * 音乐音频的风格
             */
            private String style;
        }
    }


}
