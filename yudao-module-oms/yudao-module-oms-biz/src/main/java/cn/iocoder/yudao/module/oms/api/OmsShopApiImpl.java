package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.*;


/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/4/1$
 */
@Service
public class OmsShopApiImpl implements OmsShopApi {
    @Resource
    private OmsShopService shopService;


    @Override
    public Long createShop(OmsShopSaveReqDTO saveReqDTO) {
        return shopService.createShop(saveReqDTO);
    }

    @Override
    public void updateShop(OmsShopSaveReqDTO updateReqDTO) {
        shopService.updateShop(updateReqDTO);
    }

    @Override
    public void deleteShop(Long id) {
        shopService.deleteShop(id);
    }

    @Override
    public void createOrUpdateShopByPlatform(List<OmsShopSaveReqDTO> saveReqDTOs) {
       shopService.createOrUpdateShopByPlatform(saveReqDTOs);
    }

    @Override
    public OmsShopDTO getShopByPlatformShopCode(String platformShopCode) {
        return shopService.getShopByPlatformShopCode(platformShopCode);
    }
}
