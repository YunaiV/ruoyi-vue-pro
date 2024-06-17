package cn.iocoder.yudao.framework.ai.core.model.midjourney.api;

import cn.iocoder.yudao.framework.ai.core.model.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Midjourney api
 *
 * @author fansili
 * @time 2024/6/11 15:46
 * @since 1.0
 */
@Slf4j
public class MidjourneyApi {

    private static final String URI_IMAGINE = "/submit/imagine";
    private static final String URI_ACTON = "/submit/action";
    private static final String URI_LIST_BY_CONDITION = "/task/list-by-condition";
    private final WebClient webClient;
    private final MidjourneyConfig midjourneyConfig;

    public MidjourneyApi(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
        this.webClient = WebClient.builder()
                .baseUrl(midjourneyConfig.getUrl())
                .defaultHeaders(ApiUtils.getJsonContentHeaders(midjourneyConfig.getKey()))
                .build();
    }


    /**
     * imagine - 根据提示词提交绘画任务
     *
     * @param imagineReqVO
     * @return
     */
    public SubmitResponse imagine(ImagineRequest imagineReqVO) {
        // 1、发送 post 请求
        String res = post(URI_IMAGINE, imagineReqVO);
        // 2、转换 resp
        return JsonUtils.parseObject(res, SubmitResponse.class);
    }

    /**
     * action - 放大、缩小、U1、U2...
     *
     * @param actionReqVO
     */
    public SubmitResponse action(ActionRequest actionReqVO) {
        // 1、发送 post 请求
        String res = post(URI_ACTON, actionReqVO);
        // 2、转换 resp
        return JsonUtils.parseObject(res, SubmitResponse.class);
    }

    /**
     * 批量查询 task 任务
     *
     * @param taskIds
     * @return
     */
    public List<NotifyRequest> listByCondition(Collection<String> taskIds) {
        // 1、发送 post 请求
        String res = post(URI_LIST_BY_CONDITION, ImmutableMap.of("ids", taskIds));
        // 2、转换 对象
        return JsonUtils.parseArray(res, NotifyRequest.class);
    }

    private String post(String uri, Object body) {
        // 1、发送 post 请求
        return webClient.post()
                .uri(uri)
                .body(Mono.just(JsonUtils.toJsonString(body)), String.class)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .handle((respBody, sink) -> {
                                    log.error("【Midjourney api】调用失败！resp: 【{}】", respBody);
                                    sink.error(new IllegalStateException("【Midjourney api】调用失败！"));
                                }))
                .bodyToMono(String.class)
                .block();
    }

    // ====== record 结构

    /**
     * Midjourney - Imagine 请求
     *
     * @param base64Array 垫图(参考图)base64数组
     * @param notifyHook 通知地址
     * @param prompt 提示词
     * @param state 自定义参数
     */
    public record ImagineRequest(List<String> base64Array,
                                 String notifyHook,
                                 String prompt,
                                 String state) {
    }

    /**
     * Midjourney - Action 请求
     *
     * @param customId   操作按钮id
     * @param taskId     操作按钮id
     * @param notifyHook 通知地址
     */
    public record ActionRequest(String customId,
                                String taskId,
                                String notifyHook
    ) {
    }

    /**
     * Midjourney - Submit 返回
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
     * Midjourney - 通知 request
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
    public record NotifyRequest(String id,
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
    }

    // ====== enums

    /**
     * Midjourney - 模型
     */
    @AllArgsConstructor
    @Getter
    public enum ModelEnum {

        MIDJOURNEY("midjourney", "midjourney"),
        NIJI("niji", "niji"),

        ;

        private String model;
        private String name;

        public static ModelEnum valueOfModel(String model) {
            for (ModelEnum itemEnum : ModelEnum.values()) {
                if (itemEnum.getModel().equals(model)) {
                    return itemEnum;
                }
            }
            throw new IllegalArgumentException("Invalid MessageType value: " + model);
        }
    }

    /**
     * Midjourney - 提交返回的状态码
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
     * Midjourney - action
     */
    @Getter
    @AllArgsConstructor
    public enum TaskActionEnum {
        /**
         * 生成图片.
         */
        IMAGINE,
        /**
         * 选中放大.
         */
        UPSCALE,
        /**
         * 选中其中的一张图，生成四张相似的.
         */
        VARIATION,
        /**
         * 重新执行.
         */
        REROLL,
        /**
         * 图转prompt.
         */
        DESCRIBE,
        /**
         * 多图混合.
         */
        BLEND
    }

    /**
     * Midjourney - 任务状态
     */
    @Getter
    @AllArgsConstructor
    public enum TaskStatusEnum {
        /**
         * 未启动.
         */
        NOT_START(0),
        /**
         * 已提交.
         */
        SUBMITTED(1),
        /**
         * 执行中.
         */
        IN_PROGRESS(3),
        /**
         * 失败.
         */
        FAILURE(4),
        /**
         * 成功.
         */
        SUCCESS(4);

        private final int order;

    }

}
