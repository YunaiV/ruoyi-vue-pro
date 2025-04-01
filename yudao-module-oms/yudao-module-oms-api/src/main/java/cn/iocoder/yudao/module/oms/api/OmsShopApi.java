package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import jakarta.validation.Valid;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/4/1$
 */
public interface OmsShopApi {
    /**
     * 创建店铺
     *
     * @param saveReqDTO 创建信息
     * @return 编号
     */
    Long createShop(@Valid OmsShopSaveReqDTO saveReqDTO);

    /**
     * 更新店铺
     *
     * @param updateReqDTO 更新信息
     */
    void updateShop(@Valid OmsShopSaveReqDTO updateReqDTO);

    /**
     * 删除店铺
     *
     * @param id 编号
     */
    void deleteShop(Long id);
}
