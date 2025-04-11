package cn.iocoder.yudao.module.srm.service.purchase.impl;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.SUPPLIER_NOT_ENABLE;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.SUPPLIER_NOT_EXISTS;
import static java.util.Collections.emptyList;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmSupplierMapper;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

/**
 * ERP 供应商 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SrmSupplierServiceImpl implements SrmSupplierService {

    @Resource
    private SrmSupplierMapper supplierMapper;

    @Override
    public Long createSupplier(SrmSupplierSaveReqVO createReqVO) {
        SrmSupplierDO supplier = BeanUtils.toBean(createReqVO, SrmSupplierDO.class);
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    @Override
    public void updateSupplier(SrmSupplierSaveReqVO updateReqVO) {
        // 校验存在
        validateSupplierExists(updateReqVO.getId());
        // 更新
        SrmSupplierDO updateObj = BeanUtils.toBean(updateReqVO, SrmSupplierDO.class);
        supplierMapper.updateById(updateObj);
    }

    @Override
    public void deleteSupplier(Long id) {
        // 校验存在
        validateSupplierExists(id);
        // 删除
        supplierMapper.deleteById(id);
    }

    private void validateSupplierExists(Long id) {
        if(supplierMapper.selectById(id) == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
    }

    @Override
    public SrmSupplierDO getSupplier(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public SrmSupplierDO validateSupplier(Long id) {
        SrmSupplierDO supplier = supplierMapper.selectById(id);
        if(supplier == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
        if(CommonStatusEnum.isDisable(supplier.getStatus())) {
            throw exception(SUPPLIER_NOT_ENABLE, supplier.getName());
        }
        return supplier;
    }

    @Override
    public List<SrmSupplierDO> getSupplierList(Collection<Long> ids) {
        //ids是空集合
        if(CollectionUtils.isEmpty(ids)) {
            return emptyList();
        }
        return supplierMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SrmSupplierDO> getSupplierPage(SrmSupplierPageReqVO pageReqVO) {
        return supplierMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SrmSupplierDO> getSupplierListByStatus(Integer status) {
        return supplierMapper.selectListByStatus(status);
    }

}