package cn.iocoder.yudao.module.product.service.sku;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyAndValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品 SKU Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSkuServiceImpl implements ProductSkuService {

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Resource
    private ProductPropertyService productPropertyService;

    @Override
    public void deleteSku(Long id) {
        // 校验存在
        this.validateSkuExists(id);
        // 删除
        productSkuMapper.deleteById(id);
    }

    private void validateSkuExists(Long id) {
        if (productSkuMapper.selectById(id) == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    public ProductSkuDO getSku(Long id) {
        return productSkuMapper.selectById(id);
    }

    @Override
    public List<ProductSkuDO> getSkuList(Collection<Long> ids) {
        return productSkuMapper.selectBatchIds(ids);
    }

    @Override
    public void validateSkus(List<ProductSkuCreateOrUpdateReqVO> skus, Integer specType) {
        // 非多规格，不需要校验
        if (ObjectUtil.notEqual(specType, ProductSpuSpecTypeEnum.DISABLE.getType())) {
            return;
        }

        // 1、校验规格属性存在
        // TODO @Luowenfeng：stream 的写法；不用改哈，就是说下可以酱紫写；
        Set<Long> propertyIds = skus.stream().filter(p -> p.getProperties() != null).flatMap(p -> p.getProperties().stream()) // 遍历多个 Property 属性
                .map(ProductSkuBaseVO.Property::getPropertyId).collect(Collectors.toSet()); // 将每个 Property 转换成对应的 propertyId，最后形成集合
        List<ProductPropertyAndValueRespVO> propertyAndValueList = productPropertyService.getPropertyAndValueList(propertyIds);
        if (propertyAndValueList.size() == propertyIds.size()) {
            throw exception(PROPERTY_NOT_EXISTS);
        }

        // 2. 校验，一个 SKU 下，没有重复的规格。校验方式是，遍历每个 SKU ，看看是否有重复的规格 propertyId
        Map<Long, ProductPropertyValueRespVO> propertyValueMap = propertyAndValueList.stream().filter(p -> p.getValues() != null).flatMap(p -> p.getValues().stream())
                .collect(Collectors.toMap(ProductPropertyValueRespVO::getId, value -> value)); // KEY：规格属性值的编号
        skus.forEach(sku -> {
            Set<Long> skuPropertyIds = CollectionUtils.convertSet(sku.getProperties(), propertyItem -> propertyValueMap.get(propertyItem.getValueId()).getPropertyId());
            if (skuPropertyIds.size() != sku.getProperties().size()) {
                throw exception(SKU_PROPERTIES_DUPLICATED);
            }
        });

        // 3. 再校验，每个 Sku 的规格值的数量，是一致的。
        int attrValueIdsSize = skus.get(0).getProperties().size();
        for (int i = 1; i < skus.size(); i++) {
            if (attrValueIdsSize != skus.get(i).getProperties().size()) {
                throw exception(ErrorCodeConstants.SPU_ATTR_NUMBERS_MUST_BE_EQUALS);
            }
        }

        // 4. 最后校验，每个 Sku 之间不是重复的
        Set<Set<Long>> skuAttrValues = new HashSet<>(); // 每个元素，都是一个 Sku 的 attrValueId 集合。这样，通过最外层的 Set ，判断是否有重复的.
        for (ProductSkuCreateOrUpdateReqVO sku : skus) {
            // TODO @Luowenfeng：可以使用 CollectionUtils.convertSet()，简化下面的 stream 操作
            if (!skuAttrValues.add(sku.getProperties().stream().map(ProductSkuBaseVO.Property::getValueId).collect(Collectors.toSet()))) { // 添加失败，说明重复
                throw exception(ErrorCodeConstants.SPU_SKU_NOT_DUPLICATE);
            }
        }
    }

    @Override
    public void createSkus(Long spuId, List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList) {
        // 批量插入 SKU
        List<ProductSkuDO> skuDOList = ProductSkuConvert.INSTANCE.convertSkuDOList(skuCreateReqList);
        skuDOList.forEach(v -> v.setSpuId(spuId));
        productSkuMapper.insertBatch(skuDOList);
    }

    @Override
    public List<ProductSkuDO> getSkusBySpuId(Long spuId) {
        return productSkuMapper.selectListBySpuIds(Collections.singletonList(spuId));
    }

    @Override
    public List<ProductSkuDO> getSkusBySpuIds(List<Long> spuIds) {
        return productSkuMapper.selectListBySpuIds(spuIds);
    }

    @Override
    public void deleteSkuBySpuId(Long spuId) {
        productSkuMapper.deleteBySpuId(spuId);
    }

    @Override
    @Transactional
    public void updateProductSkus(Long spuId, List<ProductSkuCreateOrUpdateReqVO> skus) {
        // 查询 SPU 下已经存在的 SKU 的集合
        List<ProductSkuDO> existsSkus = productSkuMapper.selectListBySpuId(spuId);
        Map<Long, ProductSkuDO> existsSkuMap = CollectionUtils.convertMap(existsSkus, ProductSkuDO::getId);

        // 拆分三个集合，新插入的、需要更新的、需要删除的
        List<ProductSkuDO> insertSkus = new ArrayList<>();
        List<ProductSkuDO> updateSkus = new ArrayList<>(); // TODO Luowenfeng：使用 Long 即可
        List<ProductSkuDO> deleteSkus = new ArrayList<>();

        // TODO @Luowenfeng：是不是基于规格匹配会比较好。可以参考下 onemall 的 ProductSpuServiceImpl 的 updateProductSpu 逻辑
        List<ProductSkuDO> allUpdateSkus = ProductSkuConvert.INSTANCE.convertSkuDOList(skus);
        allUpdateSkus.forEach(p -> {
            if (p.getId() != null) {
                if (existsSkuMap.containsKey(p.getId())) {
                    updateSkus.add(p);
                    return;
                }
                deleteSkus.add(p);
                return;
            }
            p.setSpuId(spuId);
            insertSkus.add(p);
        });

        if (CollectionUtil.isNotEmpty(insertSkus)) {
            productSkuMapper.insertBatch(insertSkus);
        }
        if (updateSkus.size() > 0) {
            updateSkus.forEach(p -> productSkuMapper.updateById(p));
        }
        if (deleteSkus.size() > 0) {
            productSkuMapper.deleteBatchIds(deleteSkus.stream().map(ProductSkuDO::getId).collect(Collectors.toList()));
        }
    }
}
