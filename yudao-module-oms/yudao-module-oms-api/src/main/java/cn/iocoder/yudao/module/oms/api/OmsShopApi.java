package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import jakarta.validation.Valid;

import java.util.List;

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
//    Long createShop(@Valid OmsShopSaveReqDTO saveReqDTO);

    /**
     * 更新店铺
     *
     * @param updateReqDTO 更新信息
     */
//    void updateShop(@Valid OmsShopSaveReqDTO updateReqDTO);

    /**
     * 删除店铺
     *
     * @param id 编号
     */
    void deleteShop(Long id);

    /**
     * @Description: 按平台创建或更新店铺信息
     * @return:
     */
    void createOrUpdateShopByPlatform(@Valid List<OmsShopSaveReqDTO> saveReqDTOs);

    /**
     * @param platformShopCode 平台店铺代码
     * @Description: 按平台店铺代码获取店铺信息
     * @return: @return {@link OmsShopDTO }
     */
    OmsShopDTO getShopByPlatformShopCode(String platformShopCode);

    /**
     * @Author: gumaomao
     * @Date: 2025/03/28  根据平台编码获取店铺信息集合
     */
    List<OmsShopDTO> getByPlatformCode(String platformCode);
}
