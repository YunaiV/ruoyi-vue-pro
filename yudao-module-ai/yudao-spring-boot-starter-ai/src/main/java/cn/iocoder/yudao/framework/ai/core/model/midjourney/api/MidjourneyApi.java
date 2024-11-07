package cn.iocoder.yudao.framework.ai.core.model.midjourney.api;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Midjourney API
 *
 * @author fansili
 * @since 1.0
 */
@Slf4j
public class MidjourneyApi {

    private final Predicate<HttpStatusCode> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

    private final Function<Object, Function<ClientResponse, Mono<? extends Throwable>>> EXCEPTION_FUNCTION =
            reqParam -> response -> response.bodyToMono(String.class).handle((responseBody, sink) -> {
                HttpRequest request = response.request();
                log.error("[midjourney-api] 调用失败！请求方式:[{}]，请求地址:[{}]，请求参数:[{}]，响应数据: [{}]",
                        request.getMethod(), request.getURI(), reqParam, responseBody);
                sink.error(new IllegalStateException("[midjourney-api] 调用失败！"));
            });

    private final WebClient webClient;

    /**
     * 回调地址
     */
    private final String notifyUrl;

    public MidjourneyApi(String baseUrl, String apiKey, String notifyUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(ApiUtils.getJsonContentHeaders(apiKey))
                .build();
        this.notifyUrl = notifyUrl;
    }

    /**
     * imagine - 根据提示词提交绘画任务
     *
     * @param request 请求
     * @return 提交结果
     */
    public SubmitResponse imagine(ImagineRequest request) {
        if (StrUtil.isEmpty(request.getNotifyHook())) {
            request.setNotifyHook(notifyUrl);
        }
        String response = post("/submit/imagine", request);
        return JsonUtils.parseObject(response, SubmitResponse.class);
    }

    /**
     * action - 放大、缩小、U1、U2...
     *
     * @param request 请求
     * @return 提交结果
     */
    public SubmitResponse action(ActionRequest request) {
        if (StrUtil.isEmpty(request.getNotifyHook())) {
            request.setNotifyHook(notifyUrl);
        }
        String response = post("/submit/action", request);
        return JsonUtils.parseObject(response, SubmitResponse.class);
    }

    /**
     * 批量查询 task 任务
     *
     * @param ids 任务编号数组
     * @return task 任务
     */
    public List<Notify> getTaskList(Collection<String> ids) {
        String res = post("/task/list-by-condition", ImmutableMap.of("ids", ids));
        return JsonUtils.parseArray(res, Notify.class);
    }

    private String post(String uri, Object body) {
        return webClient.post()
                .uri(uri)
                .body(Mono.just(JsonUtils.toJsonString(body)), String.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(body))
                .bodyToMono(String.class)
                .block();
    }

    // ========== record 结构 ==========

    /**
     * Imagine 请求（生成图片）
     */
    @Data
    public static final class ImagineRequest {

        /**
         * 垫图(参考图) base64 数组
         */
        private List<String> base64Array;
        /**
         * 提示词
         */
        private String prompt;
        /**
         * 通知地址
         */
        private String notifyHook;
        /**
         * 自定义参数
         */
        private String state;

        public ImagineRequest(List<String> base64Array, String prompt, String notifyHook, String state) {
            this.base64Array = base64Array;
            this.prompt = prompt;
            this.notifyHook = notifyHook;
            this.state = state;
        }

        public static String buildState(Integer width, Integer height, String version, String model) {
            StringBuilder params = new StringBuilder();
            //  --ar 来设置尺寸
            params.append(String.format(" --ar %s:%s ", width, height));
            // --niji 模型
            if (ModelEnum.NIJI.getModel().equals(model)) {
                params.append(String.format(" --niji %s ", version));
            } else {
                params.append(String.format(" --v %s ", version));
            }
            return params.toString();
        }

    }

    /**
     * Action 请求
     */
    @Data
    public static final class ActionRequest {

        private String customId;
        private String taskId;
        private String notifyHook;

        public ActionRequest(String taskId, String customId, String notifyHook) {
            this.customId = customId;
            this.taskId = taskId;
            this.notifyHook = notifyHook;
        }

    }

    /**
     * Submit 统一返回
     *
     * @param code 状态码: 1(提交成功), 21(已存在), 22(排队中), other(错误)
     * @param description 描述
     * @param properties 扩展字段
     * @param result 任务ID
     */
    public record SubmitResponse(String code,
                                 String description,
                                 Map<String, Object> properties,
                                 String result) {
    }

    /**
     * 通知 request
     *
     * @param id job id
     * @param action 任务类型 {@link TaskActionEnum}
     * @param status 任务状态 {@link TaskStatusEnum}
     * @param prompt 提示词
     * @param promptEn 提示词-英文
     * @param description 任务描述
     * @param state 自定义参数
     * @param submitTime 提交时间
     * @param startTime 开始执行时间
     * @param finishTime 结束时间
     * @param imageUrl 图片url
     * @param progress 任务进度
     * @param failReason 失败原因
     * @param buttons 任务完成后的可执行按钮
     */
    public record Notify(String id,
                         String action,
                         String status,

                         String prompt,
                         String promptEn,

                         String description,
                         String state,

                         Long submitTime,
                         Long startTime,
                         Long finishTime,

                         String imageUrl,
                         String progress,
                         String failReason,
                         List<Button> buttons) {

    }

    /**
     * button
     *
     * @param customId MJ::JOB::upsample::1::85a4b4c1-8835-46c5-a15c-aea34fad1862 动作标识
     * @param emoji 图标 emoji
     * @param label Make Variations 文本
     * @param type 类型，系统内部使用
     * @param style 样式: 2（Primary）、3（Green）
     */
    public record Button(String customId,
                         String emoji,
                         String label,
                         String type,
                         String style) {
    }

    // ============ enums ============

    /**
     * 模型枚举
     */
    @AllArgsConstructor
    @Getter
    public enum ModelEnum {

        MIDJOURNEY("midjourney", "midjourney"),
        NIJI("niji", "niji"),
        ;

        private final String model;
        private final String name;

    }

    /**
     * 提交返回的状态码的枚举
     */
    @Getter
    @AllArgsConstructor
    public enum SubmitCodeEnum {

        SUBMIT_SUCCESS("1", "提交成功"),
        ALREADY_EXISTS("21", "已存在"),
        QUEUING("22", "排队中"),
        ;

        public static final List<String> SUCCESS_CODES = Lists.newArrayList(
                SUBMIT_SUCCESS.code,
                ALREADY_EXISTS.code,
                QUEUING.code
        );

        private final String code;
        private final String name;

    }

    /**
     * Action 枚举
     */
    @Getter
    @AllArgsConstructor
    public enum TaskActionEnum {

        /**
         * 生成图片
         */
        IMAGINE,
        /**
         * 选中放大
         */
        UPSCALE,
        /**
         * 选中其中的一张图，生成四张相似的
         */
        VARIATION,
        /**
         * 重新执行
         */
        REROLL,
        /**
         * 图转 prompt
         */
        DESCRIBE,
        /**
         * 多图混合
         */
        BLEND

    }

    /**
     * 任务状态枚举
     */
    @Getter
    @AllArgsConstructor
    public enum TaskStatusEnum {

        /**
         * 未启动
         */
        NOT_START(0),
        /**
         * 已提交
         */
        SUBMITTED(1),
        /**
         * 执行中
         */
        IN_PROGRESS(3),
        /**
         * 失败
         */
        FAILURE(4),
        /**
         * 成功
         */
        SUCCESS(4);

        private final int order;

    }

}
