package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemSaveReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * OMS 店铺产品项 Service 接口
 *
 * @author 索迈管理员
 */
public interface OmsShopProductItemService {

    /**
     * 创建OMS 店铺产品项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createShopProductItem(@Valid OmsShopProductItemSaveReqVO createReqVO);

    /**
     * 更新OMS 店铺产品项
     *
     * @param updateReqVO 更新信息
     */
    void updateShopProductItem(@Valid OmsShopProductItemSaveReqVO updateReqVO);

    /**
     * 删除OMS 店铺产品项
     *
     * @param id 编号
     */
    void deleteShopProductItem(Long id);

    /**
     * @param ids IDS 产品关联项id集合
     * @Description: 通过ID批量删除店铺产品关联项
     * @return:
     */
    void deleteShopProductItemsByIds(List<Long> ids);


    /**
     * 获得OMS 店铺产品项
     *
     * @param id 编号
     * @return OMS 店铺产品项
     */
    OmsShopProductItemDO getShopProductItem(Long id);

    /**
     * 获得OMS 店铺产品项分页
     *
     * @param pageReqVO 分页查询
     * @return OMS 店铺产品项分页
     */
    PageResult<OmsShopProductItemDO> getShopProductItemPage(OmsShopProductItemPageReqVO pageReqVO);

    /**
     * 获得OMS 店铺产品项列表, 用于 Excel 导出
     *
     * @param shopProductId 店铺产品ID
     * @return OMS 店铺产品项列表
     */
    List<OmsShopProductItemDO> getShopProductItemsByProductId(Long shopProductId);

    /**
     * 按ID集合获得全部Item
     **/
    List<OmsShopProductItemDO> getShopProductItemsByShopProductIds(List<Long> shopProductIds);

    /**
     * @param shopProductCodes 平台sku代码集合
     * @Description: 通过平台sku代码集合获取店铺产品关联项集合
     * @return: @return {@link List }<{@link OmsShopProductItemDO }>
     */
    List<OmsShopProductItemDO> getShopProductItemsByShopProductCodes(List<String> shopProductCodes);
}