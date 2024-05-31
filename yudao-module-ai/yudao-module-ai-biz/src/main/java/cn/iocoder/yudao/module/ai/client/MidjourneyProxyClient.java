package cn.iocoder.yudao.module.ai.client;

import cn.iocoder.yudao.module.ai.client.vo.MidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneySubmitRespVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

// TODO @fan：这个写到 starter-ai 里哈。搞个 MidjourneyApi，参考 https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/api/OpenAiApi.java 的风格写哈
/**
 * Midjourney Proxy 客户端
 *
 * @author fansili
 * @time 2024/5/30 13:58
 * @since 1.0
 */
@Component
public class MidjourneyProxyClient {

    private static final String URI_IMAGINE = "/submit/imagine";

    @Value("${ai.midjourney-proxy.url:http://127.0.0.1:8080/mj}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * imagine - 根据提示词提交绘画任务
     *
     * @param imagineReqVO
     * @return
     */
    public MidjourneySubmitRespVO imagine(@Validated @NotNull MidjourneyImagineReqVO imagineReqVO) {
        return restTemplate.postForObject(url.concat(URI_IMAGINE), imagineReqVO, MidjourneySubmitRespVO.class);
    }

}
