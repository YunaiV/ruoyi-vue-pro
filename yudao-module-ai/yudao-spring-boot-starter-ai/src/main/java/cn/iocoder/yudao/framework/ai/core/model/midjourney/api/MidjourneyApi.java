package cn.iocoder.yudao.framework.ai.core.model.midjourney.api;

import cn.iocoder.yudao.framework.ai.core.model.midjourney.MidjourneyProperties;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.vo.MidjourneyActionRequest;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.vo.MidjourneyImagineRequest;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.vo.MidjourneyNotifyRequest;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.vo.MidjourneySubmitResponse;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

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
    private final MidjourneyProperties midjourneyProperties;

    public MidjourneyApi(MidjourneyProperties midjourneyProperties) {
        this.midjourneyProperties = midjourneyProperties;
        this.webClient = WebClient.builder()
                .baseUrl(midjourneyProperties.getUrl())
                .defaultHeaders(ApiUtils.getJsonContentHeaders(midjourneyProperties.getKey()))
                .build();
    }


    /**
     * imagine - 根据提示词提交绘画任务
     *
     * @param imagineReqVO
     * @return
     */
    public MidjourneySubmitResponse imagine(MidjourneyImagineRequest imagineReqVO) {
        // 1、发送 post 请求
        String res = post(URI_IMAGINE, imagineReqVO);
        // 2、转换 resp
        return JsonUtils.parseObject(res, MidjourneySubmitResponse.class);
    }

    /**
     * action - 放大、缩小、U1、U2...
     *
     * @param actionReqVO
     */
    public MidjourneySubmitResponse action(MidjourneyActionRequest actionReqVO) {
        // 1、发送 post 请求
        String res = post(URI_ACTON, actionReqVO);
        // 2、转换 resp
        return JsonUtils.parseObject(res, MidjourneySubmitResponse.class);
    }

    /**
     * 批量查询 task 任务
     *
     * @param taskIds
     * @return
     */
    public List<MidjourneyNotifyRequest> listByCondition(Collection<String> taskIds) {
        // 1、发送 post 请求
        String res = post(URI_LIST_BY_CONDITION, ImmutableMap.of("ids", taskIds));
        // 2、转换 对象
        return JsonUtils.parseArray(res, MidjourneyNotifyRequest.class);
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
}
