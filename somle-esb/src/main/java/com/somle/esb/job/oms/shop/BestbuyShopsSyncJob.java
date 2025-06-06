package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.bestbuy.resp.BestbuyShopRespVO;
import com.somle.bestbuy.service.BestbuyService;
import com.somle.esb.converter.oms.BestbuyToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BestbuyShopsSyncJob extends BaseShopsSyncJob {
    @Resource
    BestbuyService bestbuyService;
    @Resource
    BestbuyToOmsConverter bestbuyToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {

        List<BestbuyShopRespVO> bestbuyShopRespVOs = bestbuyService.bestbuyClients.stream().map(client -> {
            BestbuyShopRespVO bestbuyShopRespVO = client.getShopInformation();
            return bestbuyShopRespVO;
        }).toList();
        return bestbuyToOmsConverter.toShops(bestbuyShopRespVOs);
    }
}
