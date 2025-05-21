package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.kdniao.KdNiaoExpressClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * {@link KdNiaoExpressClient} 的集成测试
 *
 * @author jason
 */
@Slf4j
public class KdNiaoExpressClientIntegrationTest {

    private KdNiaoExpressClient client;

    @BeforeEach
    public void init() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        TradeExpressProperties.KdNiaoConfig config = new TradeExpressProperties.KdNiaoConfig()
                .setApiKey("cb022f1e-48f1-4c4a-a723-9001ac9676b8")
                .setBusinessId("1809751");
        client = new KdNiaoExpressClient(restTemplate, config);
    }

    @Test
    @Disabled("集成测试，暂时忽略")
    public void testGetExpressTrackList() {
        ExpressTrackQueryReqDTO reqDTO = new ExpressTrackQueryReqDTO();
        reqDTO.setExpressCode("STO");
        reqDTO.setLogisticsNo("777168349863987");
        List<ExpressTrackRespDTO> tracks = client.getExpressTrackList(reqDTO);
        System.out.println(JsonUtils.toJsonPrettyString(tracks));
    }

}
