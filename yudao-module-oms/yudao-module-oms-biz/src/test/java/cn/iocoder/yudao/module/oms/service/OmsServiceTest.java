package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopMapper;
import cn.iocoder.yudao.module.oms.service.impl.OmsShopServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

//@Disabled
@Slf4j
@Import(OmsShopServiceImpl.class)
public class OmsServiceTest extends BaseDbUnitTest {
    @Resource
    private OmsShopServiceImpl omsShopService;

    @Resource
    private OmsShopMapper omsShopMapper;


    @Test
    void getApiString() {
        List<OmsShopSaveReqDTO> omsShopSaveReqDTOS = RandomUtils.randomPojoList(OmsShopSaveReqDTO.class, 10);
        omsShopService.createOrUpdateShopByPlatform(omsShopSaveReqDTOS);
        System.out.println(omsShopSaveReqDTOS);
    }
}
