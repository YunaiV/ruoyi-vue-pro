package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import jakarta.validation.Valid;

import java.util.List;

public interface OmsShopService {

    /**
     * 创建店铺
     *
     * @param saveReqVO 创建信息
     * @return 编号
     */
    Long createShop(@Valid OmsShopSaveReqDTO saveReqVO);

    /**
     * 更新店铺
     *
     * @param updateReqVO 更新信息
     */
    void updateShop(@Valid OmsShopSaveReqDTO updateReqVO);

    /**
     * 删除店铺
     *
     * @param id 编号
     */
    void deleteShop(Long id);

    /**
     * @Author: gumaomao
     * @Date: 2025/03/28  根据平台编码获取店铺信息集合
     * @return: @return {@link List }<{@link OmsShopDO }>
     */
    List<OmsShopDO> getByPlatformCode(String platformCode);


    /**
     * @param platformShopCode 平台商店代码
     * @Description: 通过平台商店代码获取
     * @return: @return {@link OmsShopDO }
     */
    OmsShopDTO getShopByPlatformShopCode(String platformShopCode);
    /**
     * @Description: 按平台创建或更新店铺信息
     * @return:
     */
    void createOrUpdateShopByPlatform(List<OmsShopSaveReqDTO> saveReqDTOs);

}
