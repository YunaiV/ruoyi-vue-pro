package cn.iocoder.yudao.module.oms.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/4/1$
 */
@Service
public class OmsShopApiImpl implements OmsShopApi {
    @Resource
    private OmsShopService shopService;


//    @Override
//    public Long createShop(OmsShopSaveReqDTO saveReqDTO) {
//        return shopService.createShop(saveReqDTO);
//    }

//    @Override
//    public void updateShop(OmsShopSaveReqDTO updateReqDTO) {
//        shopService.updateShop(updateReqDTO);
//    }

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

    @Override
    public List<OmsShopDTO> getByPlatformCode(String platformCode) {
        if (platformCode == null) {
            return CollectionUtil.empty(List.class);
        }
        return BeanUtils.toBean(shopService.getByPlatformCode(platformCode), OmsShopDTO.class);
    }
}
