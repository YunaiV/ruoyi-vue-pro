package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.module.erp.controller.admin.product.vo.unit.ErpProductUnitPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.unit.ErpProductUnitSaveReqVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductUnitMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.ErrorCodeConstants.PRODUCT_UNIT_NOT_EXISTS;

/**
 * ERP 产品单位 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpProductUnitServiceImpl implements ErpProductUnitService {

    @Resource
    private ErpProductUnitMapper productUnitMapper;

    @Override
    public Long createProductUnit(ErpProductUnitSaveReqVO createReqVO) {
        // 插入
        ErpProductUnitDO unit = BeanUtils.toBean(createReqVO, ErpProductUnitDO.class);
        productUnitMapper.insert(unit);
        // 返回
        return unit.getId();
    }

    @Override
    public void updateProductUnit(ErpProductUnitSaveReqVO updateReqVO) {
        // 校验存在
        validateProductUnitExists(updateReqVO.getId());
        // 更新
        ErpProductUnitDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductUnitDO.class);
        productUnitMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductUnit(Long id) {
        // 校验存在
        validateProductUnitExists(id);
        // 删除
        productUnitMapper.deleteById(id);
    }

    private void validateProductUnitExists(Long id) {
        if (productUnitMapper.selectById(id) == null) {
            throw exception(PRODUCT_UNIT_NOT_EXISTS);
        }
    }

    @Override
    public ErpProductUnitDO getProductUnit(Long id) {
        return productUnitMapper.selectById(id);
    }

    @Override
    public PageResult<ErpProductUnitDO> getProductUnitPage(ErpProductUnitPageReqVO pageReqVO) {
        return productUnitMapper.selectPage(pageReqVO);
    }

}