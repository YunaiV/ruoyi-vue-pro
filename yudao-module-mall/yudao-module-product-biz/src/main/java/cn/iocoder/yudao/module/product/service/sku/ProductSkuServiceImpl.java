package cn.iocoder.yudao.module.product.service.sku;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品sku Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSkuServiceImpl implements ProductSkuService {

    @Resource
    private ProductSkuMapper ProductSkuMapper;

    @Override
    public Long createSku(ProductSkuCreateReqVO createReqVO) {
        // 插入
        ProductSkuDO sku = ProductSkuConvert.INSTANCE.convert(createReqVO);
        ProductSkuMapper.insert(sku);
        // 返回
        return sku.getId();
    }

    @Override
    public void updateSku(ProductSkuUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSkuExists(updateReqVO.getId());
        // 更新
        ProductSkuDO updateObj = ProductSkuConvert.INSTANCE.convert(updateReqVO);
        ProductSkuMapper.updateById(updateObj);
    }

    @Override
    public void deleteSku(Long id) {
        // 校验存在
        this.validateSkuExists(id);
        // 删除
        ProductSkuMapper.deleteById(id);
    }

    private void validateSkuExists(Long id) {
        if (ProductSkuMapper.selectById(id) == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    public ProductSkuDO getSku(Long id) {
        return ProductSkuMapper.selectById(id);
    }

    @Override
    public List<ProductSkuDO> getSkuList(Collection<Long> ids) {
        return ProductSkuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductSkuDO> getSkuPage(ProductSkuPageReqVO pageReqVO) {
        return ProductSkuMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductSkuDO> getSkuList(ProductSkuExportReqVO exportReqVO) {
        return ProductSkuMapper.selectList(exportReqVO);
    }

    @Override
    public void validatedSkuReq(List<ProductSkuCreateReqVO> skuCreateReqList) {
        // 校验规格属性以及规格值是否存在
        List<Integer> propertyIds = skuCreateReqList.stream().flatMap(p -> p.getProperties().stream()).map(ProductSkuBaseVO.Property::getPropertyId).collect(Collectors.toList());

        // 校验是否有重复的sku组合
    }
}
