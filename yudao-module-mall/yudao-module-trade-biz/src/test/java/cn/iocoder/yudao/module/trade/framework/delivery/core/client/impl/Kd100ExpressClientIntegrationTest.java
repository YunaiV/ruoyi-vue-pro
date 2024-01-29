package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.kd100.Kd100ExpressClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * {@link Kd100ExpressClient} 的集成测试
 *
 * @author jason
 */
@Slf4j
public class Kd100ExpressClientIntegrationTest {

    private Kd100ExpressClient client;

    @BeforeEach
    public void init() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        TradeExpressProperties.Kd100Config config = new TradeExpressProperties.Kd100Config()
                .setKey("pLXUGAwK5305")
                .setCustomer("E77DF18BE109F454A5CD319E44BF5177");
        client = new Kd100ExpressClient(restTemplate, config);
    }

    @Test
    @Disabled("集成测试，暂时忽略")
    public void testGetExpressTrackList() {
        ExpressTrackQueryReqDTO reqDTO = new ExpressTrackQueryReqDTO();
        reqDTO.setExpressCode("STO");
        reqDTO.setLogisticsNo("773220402764314");
        List<ExpressTrackRespDTO> tracks = client.getExpressTrackList(reqDTO);
        System.out.println(JsonUtils.toJsonPrettyString(tracks));
    }

}
