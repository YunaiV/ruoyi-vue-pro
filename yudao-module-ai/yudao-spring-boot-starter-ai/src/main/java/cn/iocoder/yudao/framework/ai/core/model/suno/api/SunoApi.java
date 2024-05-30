package cn.iocoder.yudao.framework.ai.core.model.suno.api;

import cn.iocoder.yudao.framework.ai.core.model.suno.SunoConfig;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

// TODO @xiaoxin：类注释
/**
 * @Author xiaoxin
 * @Date 2024/5/27
 */
@Slf4j
public class SunoApi {

    // TODO @xiaoxin：APPLICATION_JSON、TOKEN_PREFIX 看看 spring 有没自带的这 2 个枚举哈。变量越少越好
    public static final String APPLICATION_JSON = "application/json";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String API_URL = "https://api.acedata.cloud/suno/audios";

    private static final int READ_TIMEOUT = 160; // 连接超时时间（秒），音乐生成时间较长，设置为 160s，后续可做callback

    // TODO @xiaoxin：建议使用 webClient 对接。参考 https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/api/OpenAiApi.java
    private final OkHttpClient client;

    // TODO @xiaoxin：sunoConfig => config，简洁一点
    public SunoApi(SunoConfig sunoConfig) {
        this.client = new OkHttpClient().newBuilder().readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request requestWithUserAgent = originalRequest.newBuilder()
                            .header("Authorization", TOKEN_PREFIX + sunoConfig.getToken())
                            .build();
                    return chain.proceed(requestWithUserAgent);
                })
                .build();
    }

    // TODO @芋艿：方法名，要考虑下；
    public SunoResponse musicGen(SunoRequest sunoRequest) {
        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(MediaType.parse(APPLICATION_JSON), JsonUtils.toJsonString(sunoRequest)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("suno调用失败! response: {}", response);
                throw new IllegalStateException("suno调用失败!" + response);
            }
            return JsonUtils.parseObject(response.body().string(), SunoResponse.class);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // TODO @xiaoxin：看看是不是使用 record 特性，简化下；

    /**
     * 请求数据对象，用于生成音乐音频
     */
    @Data
    @Accessors(chain = true)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class SunoRequest {
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
    public static class SunoResponse {
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
