package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface OmsShopProductApi {

    /**
     * @Description: 按平台创建或更新店铺产品信息
     * @return:
     */
    void createOrUpdateShopByPlatform(@Valid List<OmsShopProductSaveReqDTO> saveReqDTOs);
}
