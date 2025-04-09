package cn.iocoder.yudao.module.wms.service.inventory.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.product.WmsInventoryProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库存盘点产品 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryProductServiceImpl implements WmsInventoryProductService {

    @Resource
    private WmsInventoryProductMapper inventoryProductMapper;

    @Override
    public Long createInventoryProduct(WmsInventoryProductSaveReqVO createReqVO) {
        // 插入
        WmsInventoryProductDO inventoryProduct = BeanUtils.toBean(createReqVO, WmsInventoryProductDO.class);
        inventoryProductMapper.insert(inventoryProduct);
        // 返回
        return inventoryProduct.getId();
    }

    @Override
    public void updateInventoryProduct(WmsInventoryProductSaveReqVO updateReqVO) {
        // 校验存在
        validateInventoryProductExists(updateReqVO.getId());
        // 更新
        WmsInventoryProductDO updateObj = BeanUtils.toBean(updateReqVO, WmsInventoryProductDO.class);
        inventoryProductMapper.updateById(updateObj);
    }

    @Override
    public void deleteInventoryProduct(Long id) {
        // 校验存在
        validateInventoryProductExists(id);
        // 删除
        inventoryProductMapper.deleteById(id);
    }

    private void validateInventoryProductExists(Long id) {
        if (inventoryProductMapper.selectById(id) == null) {
            //throw exception(INVENTORY_PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public WmsInventoryProductDO getInventoryProduct(Long id) {
        return inventoryProductMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryProductDO> getInventoryProductPage(WmsInventoryProductPageReqVO pageReqVO) {
        return inventoryProductMapper.selectPage(pageReqVO);
    }

}