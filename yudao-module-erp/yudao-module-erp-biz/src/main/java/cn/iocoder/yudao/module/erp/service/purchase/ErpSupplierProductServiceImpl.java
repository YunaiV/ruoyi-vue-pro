package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierProductMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 供应商产品 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
public class ErpSupplierProductServiceImpl implements ErpSupplierProductService {

    @Resource
    private ErpSupplierProductMapper supplierProductMapper;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;

    @Override
    public Long createSupplierProduct(ErpSupplierProductSaveReqVO createReqVO) {
        validateSupplierProductCodeUnique(createReqVO.getCode());
        // 插入
        ErpSupplierProductDO supplierProduct = BeanUtils.toBean(createReqVO, ErpSupplierProductDO.class);
        supplierProductMapper.insert(supplierProduct);
        // 返回
        return supplierProduct.getId();
    }

    @Override
    public void updateSupplierProduct(ErpSupplierProductSaveReqVO updateReqVO) {
        validateSupplierProductCodeUnique(updateReqVO.getCode());
        // 校验存在
        validateSupplierProductExists(updateReqVO.getId());
        // 更新
        ErpSupplierProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpSupplierProductDO.class);
        supplierProductMapper.updateById(updateObj);
    }

    @Override
    public void deleteSupplierProduct(Long id) {
        // 校验存在
        validateSupplierProductExists(id);
        // 删除
        supplierProductMapper.deleteById(id);
    }

    private void validateSupplierProductExists(Long id) {
        if (supplierProductMapper.selectById(id) == null) {
            throw exception(SUPPLIER_PRODUCT_NOT_EXISTS);
        }
    }

    private void validateSupplierProductCodeUnique(String code) {
        ErpSupplierProductDO supplierProduct = supplierProductMapper.selectByCode(code);
        if (supplierProduct == null) {
            return;
        } else {
            throw exception(SUPPLIER_PRODUCT_CODE_DUPLICATE);
        }
    }

    @Override
    public ErpSupplierProductDO getSupplierProduct(Long id) {
        return supplierProductMapper.selectById(id);
    }

    @Override
    public PageResult<ErpSupplierProductDO> getSupplierProductPage(ErpSupplierProductPageReqVO pageReqVO) {
        return supplierProductMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ErpSupplierProductRespVO> buildSupplierProductVOPageResult(PageResult<ErpSupplierProductDO> pageResult) {
        var newList = buildSupplierProductVOList(pageResult.getList());
        return new PageResult<>(newList, (long) newList.size());
    }

    private List<ErpSupplierProductRespVO> buildSupplierProductVOList(List<ErpSupplierProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
            convertSet(list, ErpSupplierProductDO::getProductId));
        // 1.3 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
            convertSet(list, ErpSupplierProductDO::getSupplierId));
        return BeanUtils.toBean(list, ErpSupplierProductRespVO.class, supplierProduct -> {
            MapUtils.findAndThen(productMap, supplierProduct.getProductId(), product -> supplierProduct.setProductName(product.getName()));
            MapUtils.findAndThen(supplierMap, supplierProduct.getSupplierId(), supplier -> supplierProduct.setSupplierName(supplier.getName()));
//            MapUtils.findAndThen(userMap, Long.parseLong(supplierProduct.getCreator()), user -> supplierProduct.setCreatorName(user.getNickname()));
        });
    }

    @Override
    public List<ErpSupplierProductRespVO> getSupplierProductVOListByStatus(Integer status) {
        //TODO: only return when product is of that status
        List<ErpSupplierProductDO> list = supplierProductMapper.selectList();
        return BeanUtils.toBean(list, ErpSupplierProductRespVO.class);
    }

}