package cn.iocoder.yudao.module.oms.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemSaveReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductItemDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopProductItemMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopProductItemService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SHOP_PRODUCT_ITEM_NOT_EXISTS;

/**
 * OMS 店铺产品项 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
public class OmsShopProductItemServiceImpl implements OmsShopProductItemService {

    @Resource
    private OmsShopProductItemMapper shopProductItemMapper;

    @Override
    public Long createShopProductItem(OmsShopProductItemSaveReqVO createReqVO) {
        // 插入
        OmsShopProductItemDO shopProductItem = BeanUtils.toBean(createReqVO, OmsShopProductItemDO.class);
        shopProductItemMapper.insert(shopProductItem);
        // 返回
        return shopProductItem.getId();
    }

    @Override
    public void updateShopProductItem(OmsShopProductItemSaveReqVO updateReqVO) {
        // 校验存在
        validateShopProductItemExists(updateReqVO.getId());
        // 更新
        OmsShopProductItemDO updateObj = BeanUtils.toBean(updateReqVO, OmsShopProductItemDO.class);
        shopProductItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteShopProductItem(Long id) {
        // 校验存在
        validateShopProductItemExists(id);
        // 删除
        shopProductItemMapper.deleteById(id);
    }

    @Override
    public void deleteShopProductItemsByIds(List<Long> ids) {
        shopProductItemMapper.deleteByIds(ids);
    }

    private void validateShopProductItemExists(Long id) {
        if (shopProductItemMapper.selectById(id) == null) {
            throw exception(OMS_SHOP_PRODUCT_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public OmsShopProductItemDO getShopProductItem(Long id) {
        return shopProductItemMapper.selectById(id);
    }

    @Override
    public PageResult<OmsShopProductItemDO> getShopProductItemPage(OmsShopProductItemPageReqVO pageReqVO) {
        return shopProductItemMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OmsShopProductItemDO> getShopProductItemsByProductId(Long shopProductId) {
        return shopProductItemMapper.getShopProductItemsByProductId(shopProductId);
    }

    @Override
    public List<OmsShopProductItemDO> getShopProductItemsByProductIds(List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return List.of();
        }
        return shopProductItemMapper.selectList(OmsShopProductItemDO::getShopProductId, productIds);
    }

}