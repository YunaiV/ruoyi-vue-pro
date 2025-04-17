package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OmsShopProductApiImpl implements OmsShopProductApi {

    @Resource
    private OmsShopProductService shopProductService;

    @Override
    public void createOrUpdateShopByPlatform(List<OmsShopProductSaveReqDTO> saveReqDTOs) {
        shopProductService.createOrUpdateShopProductByPlatform(saveReqDTOs);
    }

    @Override
    public List<OmsShopProductDTO> getByShopIds(List<Long> shopIds) {
        List<OmsShopProductDO> shopDos = shopProductService.getByShopIds(shopIds);
        return BeanUtils.toBean(shopDos, OmsShopProductDTO.class);
    }
}
