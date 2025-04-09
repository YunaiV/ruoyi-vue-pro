package cn.iocoder.yudao.module.wms.service.inventory.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.product.WmsInventoryProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_PRODUCT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_PRODUCT_NOT_EXISTS;

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

    @Resource
    private ErpProductApi productApi;
    /**
     * @sign : 71250103111E09A9
     */
    @Override
    public WmsInventoryProductDO createInventoryProduct(WmsInventoryProductSaveReqVO createReqVO) {
        if (inventoryProductMapper.getByInventoryIdAndProductId(createReqVO.getInventoryId(), createReqVO.getProductId()) != null) {
            throw exception(INVENTORY_PRODUCT_EXISTS);
        }
        // 插入
        WmsInventoryProductDO inventoryProduct = BeanUtils.toBean(createReqVO, WmsInventoryProductDO.class);
        inventoryProductMapper.insert(inventoryProduct);
        // 返回
        return inventoryProduct;
    }

    /**
     * @sign : 234D5A77BD1FCEB0
     */
    @Override
    public WmsInventoryProductDO updateInventoryProduct(WmsInventoryProductSaveReqVO updateReqVO) {
        // 校验存在
        WmsInventoryProductDO exists = validateInventoryProductExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getInventoryId(), exists.getInventoryId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(INVENTORY_PRODUCT_EXISTS);
        }
        // 更新
        WmsInventoryProductDO inventoryProduct = BeanUtils.toBean(updateReqVO, WmsInventoryProductDO.class);
        inventoryProductMapper.updateById(inventoryProduct);
        // 返回
        return inventoryProduct;
    }

    /**
     * @sign : 1B2D536F7B42E629
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInventoryProduct(Long id) {
        // 校验存在
        WmsInventoryProductDO inventoryProduct = validateInventoryProductExists(id);
        // 唯一索引去重
        inventoryProduct.setInventoryId(inventoryProductMapper.flagUKeyAsLogicDelete(inventoryProduct.getInventoryId()));
        inventoryProduct.setProductId(inventoryProductMapper.flagUKeyAsLogicDelete(inventoryProduct.getProductId()));
        inventoryProductMapper.updateById(inventoryProduct);
        // 删除
        inventoryProductMapper.deleteById(id);
    }

    /**
     * @sign : 34841D62B2D34376
     */
    private WmsInventoryProductDO validateInventoryProductExists(Long id) {
        WmsInventoryProductDO inventoryProduct = inventoryProductMapper.selectById(id);
        if (inventoryProduct == null) {
            throw exception(INVENTORY_PRODUCT_NOT_EXISTS);
        }
        return inventoryProduct;
    }

    @Override
    public WmsInventoryProductDO getInventoryProduct(Long id) {
        return inventoryProductMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryProductDO> getInventoryProductPage(WmsInventoryProductPageReqVO pageReqVO) {
        return inventoryProductMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsInventoryProductDO
     */
    public List<WmsInventoryProductDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return inventoryProductMapper.selectByIds(idList);
    }

    @Override
    public List<WmsInventoryProductDO> selectByInventoryId(Long id) {
        return inventoryProductMapper.selectByInventoryId(id);
    }

    @Override
    public void assembleProduct(List<WmsInventoryProductRespVO> inventoryProductList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(inventoryProductList).map(WmsInventoryProductRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(inventoryProductList).assemble(productVOMap, WmsInventoryProductRespVO::getProductId, WmsInventoryProductRespVO::setProduct);
    }
}
