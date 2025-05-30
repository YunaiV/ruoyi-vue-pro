package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.cdiscount.model.resp.CdiscountSellerRespVO;
import com.somle.cdiscount.service.CdiscountClient;
import com.somle.cdiscount.service.CdiscountService;
import com.somle.esb.converter.oms.CdiscountToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CdiscountShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    private CdiscountService cdiscountService;
    @Resource
    private CdiscountToOmsConverter cdiscountToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        List<OmsShopSaveReqDTO> shops = new ArrayList<>();
        for (CdiscountClient client : cdiscountService.clients) {
            try {
                CdiscountSellerRespVO cdiscountSellerRespVO = client.getSeller();
                List<OmsShopSaveReqDTO> omsShopSaveReqDTO = cdiscountToOmsConverter.toShops(List.of(cdiscountSellerRespVO));
                shops.addAll(omsShopSaveReqDTO);
            } catch (Exception e) {
                log.error("获取店铺信息失败", e);
            }
        }
        return shops;
    }
}
