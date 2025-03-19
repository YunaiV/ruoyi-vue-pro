package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierProductMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 供应商产品 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
@RequiredArgsConstructor
public class ErpSupplierProductServiceImpl implements ErpSupplierProductService {
    @Resource
    MessageChannel erpCustomRuleChannel;
    private final ErpSupplierProductMapper supplierProductMapper;
    private final ErpProductService productService;
    private final ErpSupplierService supplierService;

    @Override
    public Long createSupplierProduct(ErpSupplierProductSaveReqVO createReqVO) {
        validateSupplierProductCodeUnique(null,createReqVO.getCode());
        // 插入
        ErpSupplierProductDO supplierProduct = BeanUtils.toBean(createReqVO, ErpSupplierProductDO.class);
        supplierProductMapper.insert(supplierProduct);
        // 返回
        return supplierProduct.getId();
    }

    @Override
    public void updateSupplierProduct(ErpSupplierProductSaveReqVO updateReqVO) {
        Long id = updateReqVO.getId();
        validateSupplierProductCodeUnique(id,updateReqVO.getCode());
        // 校验存在
        validateSupplierProductExists(id);
        // 更新
        ErpSupplierProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpSupplierProductDO.class);
        ThrowUtil.ifSqlThrow(supplierProductMapper.updateById(updateObj),DB_UPDATE_ERROR);
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
        if (supplierProductMapper.selectById(id) == null) {
            throw exception(SUPPLIER_PRODUCT_NOT_EXISTS);
        }
    }

    private void validateSupplierProductCodeUnique(Long id,String code) {
        ErpSupplierProductDO supplierProduct = supplierProductMapper.selectByCode(code);
        if (supplierProduct == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null){
            throw exception(PRODUCT_CODE_DUPLICATE);
        }
        if (!supplierProduct.getId().equals(id)) {
            throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
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
        return new PageResult<>(newList, pageResult.getTotal());
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