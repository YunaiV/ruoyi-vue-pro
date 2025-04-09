package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopSaveReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OmsShopService {

    /**
     * 创建店铺
     *
     * @param saveReqVO 创建信息
     * @return 店铺id
     */
    Long createShop(@Valid OmsShopSaveReqVO saveReqVO);

    /**
     * 更新店铺
     *
     * @param updateReqVO 更新信息
     */
    void updateShop(@Valid OmsShopSaveReqVO updateReqVO);

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

    /**
     * 按条件查询店铺，不分页
     **/
    List<OmsShopRespVO> getShopList(@Valid OmsShopPageReqVO pageReqVO);


    /**
     * 获得OMS 店铺分页
     *
     * @param pageReqVO 分页查询
     * @return OMS 店铺分页
     */
    PageResult<OmsShopRespVO> getShopPage(OmsShopPageReqVO pageReqVO);

    /**
     * 通过主键id获得OMS店铺
     */
    OmsShopRespVO getShopById(Long id);

    /**
     * @param id   shop的主键
     * @param name 店铺名称
     * @param code 店铺编码
     * @Description: 更新店铺的名称和编码
     */
    Boolean updateShopNameAndCode(Long id, String name, String code);

    /**
     * 按ShopId集合获取全部店铺
     **/
    Map<Long, OmsShopRespVO> getShopMapByIds(Set<Long> shopIds);

}
