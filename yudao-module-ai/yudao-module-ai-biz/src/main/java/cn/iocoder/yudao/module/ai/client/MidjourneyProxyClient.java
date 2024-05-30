package cn.iocoder.yudao.module.ai.client;

import cn.iocoder.yudao.module.ai.client.vo.MidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneySubmitRespVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

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
