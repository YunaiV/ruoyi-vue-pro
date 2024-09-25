package cn.iocoder.yudao.module.erp.service.purchase;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierProductMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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

    @Override
    public Long createSupplierProduct(ErpSupplierProductSaveReqVO createReqVO) {
        // 插入
        ErpSupplierProductDO supplierProduct = BeanUtils.toBean(createReqVO, ErpSupplierProductDO.class);
        supplierProductMapper.insert(supplierProduct);
        // 返回
        return supplierProduct.getId();
    }

    @Override
    public void updateSupplierProduct(ErpSupplierProductSaveReqVO updateReqVO) {
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

    @Override
    public ErpSupplierProductDO getSupplierProduct(Long id) {
        return supplierProductMapper.selectById(id);
    }

    @Override
    public PageResult<ErpSupplierProductDO> getSupplierProductPage(ErpSupplierProductPageReqVO pageReqVO) {
        return supplierProductMapper.selectPage(pageReqVO);
    }

}