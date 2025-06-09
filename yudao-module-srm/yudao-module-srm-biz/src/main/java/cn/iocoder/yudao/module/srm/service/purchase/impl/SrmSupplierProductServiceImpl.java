package cn.iocoder.yudao.module.srm.service.purchase.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierProductDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmSupplierProductMapper;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierProductService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErpErrorCodeConstants.PRODUCT_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErpErrorCodeConstants.PRODUCT_UNIT_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.SUPPLIER_PRODUCT_NOT_EXISTS;

/**
 * ERP 供应商产品 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
@RequiredArgsConstructor
public class SrmSupplierProductServiceImpl implements SrmSupplierProductService {

    @Resource
    MessageChannel erpCustomRuleChannel;
    private final SrmSupplierProductMapper supplierProductMapper;
    //    private final ErpProductService productService;
    private final SrmSupplierService supplierService;

    @Override
    public Long createSupplierProduct(SrmSupplierProductSaveReqVO createReqVO) {
        validateSupplierProductCodeUnique(null, createReqVO.getCode());
        // 插入
        SrmSupplierProductDO supplierProduct = BeanUtils.toBean(createReqVO, SrmSupplierProductDO.class);
        supplierProductMapper.insert(supplierProduct);
        // 返回
        return supplierProduct.getId();
    }

    @Override
    public void updateSupplierProduct(SrmSupplierProductSaveReqVO updateReqVO) {
        Long id = updateReqVO.getId();
        validateSupplierProductCodeUnique(id, updateReqVO.getCode());
        // 校验存在
        validateSupplierProductExists(id);
        // 更新
        SrmSupplierProductDO updateObj = BeanUtils.toBean(updateReqVO, SrmSupplierProductDO.class);
        ThrowUtil.ifSqlThrow(supplierProductMapper.updateById(updateObj), DB_UPDATE_ERROR);
        //同步数据-暂停
        //        var dtos = customRuleMapper.selectProductAllInfoListBySupplierId(id);
        //        erpCustomRuleChannel.send(MessageBuilder.withPayload(dtos).build());
    }

    @Override
    public void deleteSupplierProduct(Long id) {
        // 校验存在
        validateSupplierProductExists(id);
        // 删除
        supplierProductMapper.deleteById(id);
    }

    private void validateSupplierProductExists(Long id) {
        if(supplierProductMapper.selectById(id) == null) {
            throw exception(SUPPLIER_PRODUCT_NOT_EXISTS);
        }
    }

    private void validateSupplierProductCodeUnique(Long id, String code) {
        SrmSupplierProductDO supplierProduct = supplierProductMapper.selectByCode(code);
        if(supplierProduct == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if(id == null) {
            throw exception(PRODUCT_CODE_DUPLICATE);
        }
        if(!supplierProduct.getId().equals(id)) {
            throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
        }
    }

    @Override
    public SrmSupplierProductDO getSupplierProduct(Long id) {
        return supplierProductMapper.selectById(id);
    }

    @Override
    public PageResult<SrmSupplierProductDO> getSupplierProductPage(SrmSupplierProductPageReqVO pageReqVO) {
        return supplierProductMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<SrmSupplierProductRespVO> buildSupplierProductVOPageResult(PageResult<SrmSupplierProductDO> pageResult) {
        var newList = buildSupplierProductVOList(pageResult.getList());
        return new PageResult<>(newList, pageResult.getTotal());
    }

    private List<SrmSupplierProductRespVO> buildSupplierProductVOList(List<SrmSupplierProductDO> list) {
        if(CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.2 产品信息
        //        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
        //            convertSet(list, SrmSupplierProductDO::getProductId));
        // 1.3 供应商信息
        Map<Long, SrmSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(list, SrmSupplierProductDO::getSupplierId));
        return BeanUtils.toBean(list, SrmSupplierProductRespVO.class, supplierProduct -> {
            //            MapUtils.findAndThen(productMap, supplierProduct.getProductId(), product -> supplierProduct.setProductName(product.getName()));
            MapUtils.findAndThen(supplierMap, supplierProduct.getSupplierId(), supplier -> supplierProduct.setSupplierName(supplier.getName()));
            //            MapUtils.findAndThen(userMap, Long.parseLong(supplierProduct.getCreator()), user -> supplierProduct.setCreatorName(user.getNickname()));
        });
    }

    @Override
    public List<SrmSupplierProductRespVO> getSupplierProductVOListByStatus(Integer status) {
        //TODO: only return when product is of that status
        List<SrmSupplierProductDO> list = supplierProductMapper.selectList();
        return BeanUtils.toBean(list, SrmSupplierProductRespVO.class);
    }

}