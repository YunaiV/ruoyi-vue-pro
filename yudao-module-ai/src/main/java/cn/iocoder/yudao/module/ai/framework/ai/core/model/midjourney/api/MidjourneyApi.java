package cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
            reqParam -> response -> response.bodyToMono(String.class).defaultIfEmpty("").handle((responseBody, sink) -> {
                HttpRequest request = response.request();
                log.error("[midjourney-api] 调用失败！请求方式:[{}]，请求地址:[{}]，请求参数:[{}]，响应数据: [{}]",
                        request.getMethod(), request.getURI(), reqParam, responseBody);
                // 中转没有该接口（例如说不支持融图、换脸、局部重绘），转为明确的错误
                if (response.statusCode().value() == HttpStatus.NOT_FOUND.value()) {
                    sink.error(new IllegalStateException("[midjourney-api] 中转不支持该接口：" + request.getURI().getPath()));
                    return;
                }
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
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setBearerAuth(apiKey);
                    // 兼容 novicezk/midjourney-proxy：开启 mj.api-secret 后，通过 mj-api-secret 请求头鉴权
                    if (StrUtil.isNotEmpty(apiKey)) {
                        httpHeaders.set("mj-api-secret", apiKey);
                    }
                })
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
     * blend - 融图：2-5 张图片
     *
     * @param request 请求
     * @return 提交结果
     */
    public SubmitResponse blend(BlendRequest request) {
        if (StrUtil.isEmpty(request.notifyHook())) {
            request = new BlendRequest(request.base64Array(), request.dimensions(), notifyUrl, request.state());
        }
        String response = post("/submit/blend", request);
        return JsonUtils.parseObject(response, SubmitResponse.class);
    }

    /**
     * swapFace - 换脸：把来源图的脸换到目标图上
     *
     * @param request 请求
     * @return 提交结果
     */
    public SubmitResponse swapFace(SwapFaceRequest request) {
        if (StrUtil.isEmpty(request.notifyHook())) {
            request = new SwapFaceRequest(request.sourceBase64(), request.targetBase64(), notifyUrl, request.state());
        }
        String response = post("/insight-face/swap", request);
        return JsonUtils.parseObject(response, SubmitResponse.class);
    }

    /**
     * modal - 提交弹窗内容，例如说局部重绘的蒙版与提示词
     *
     * @param request 请求
     * @return 提交结果
     */
    public SubmitResponse modal(ModalRequest request) {
        String response = post("/submit/modal", request);
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
     * Blend 请求（融图）
     *
     * @param base64Array 图片 base64 数组，2-5 张
     * @param dimensions 尺寸：PORTRAIT(2:3)、SQUARE(1:1)、LANDSCAPE(3:2)
     * @param notifyHook 通知地址
     * @param state 自定义参数
     */
    public record BlendRequest(List<String> base64Array,
                               String dimensions,
                               String notifyHook,
                               String state) {
    }

    /**
     * SwapFace 请求（换脸）
     *
     * @param sourceBase64 来源人脸图片 base64
     * @param targetBase64 目标图片 base64
     * @param notifyHook 通知地址
     * @param state 自定义参数
     */
    public record SwapFaceRequest(String sourceBase64,
                                  String targetBase64,
                                  String notifyHook,
                                  String state) {
    }

    /**
     * Modal 请求（局部重绘等弹窗的提交）
     *
     * @param taskId 任务编号
     * @param prompt 提示词
     * @param maskBase64 蒙版 base64：黑底，白色为重绘区域
     */
    public record ModalRequest(String taskId,
                               String prompt,
                               String maskBase64) {
    }

    /**
     * Submit 统一返回
     *
     * @param code 状态码: 1(提交成功), 21(已存在 / 窗口等待), 22(排队中), other(错误)
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
        BLEND,
        /**
         * 换脸
         */
        SWAP_FACE

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
         * 窗口等待（例如说：局部重绘等需要弹窗确认的操作）
         */
        MODAL(2),
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
        SUCCESS(4),
        /**
         * 已取消
         */
        CANCEL(4);

        private final int order;

    }

    /**
     * 局部重绘（Modal）的中转流程
     */
    public enum ModalProtocolEnum {

        /**
         * 先执行局部重绘（Inpaint）按钮，再用返回的任务编号提交蒙版
         */
        ACTION_THEN_MODAL,
        /**
         * 直接用来源任务编号提交蒙版
         */
        DIRECT_MODAL

    }

}
