package cn.iocoder.yudao.module.product.service.sku;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
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
    @Lazy // 循环依赖，避免报错
    private ProductSpuService productSpuService;
    @Resource
    private ProductPropertyService productPropertyService;
    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @Override
    public void deleteSku(Long id) {
        // 校验存在
        validateSkuExists(id);
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
    public List<ProductSkuDO> getSkuList() {
        return productSkuMapper.selectList();
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
        Set<Long> propertyIds = skus.stream().filter(p -> p.getProperties() != null).flatMap(p -> p.getProperties().stream()) // 遍历多个 Property 属性
                .map(ProductSkuBaseVO.Property::getPropertyId).collect(Collectors.toSet()); // 将每个 Property 转换成对应的 propertyId，最后形成集合
        List<ProductPropertyRespVO> propertyList = productPropertyService.getPropertyList(propertyIds);
        if (propertyList.size() != propertyIds.size()) {
            throw exception(PROPERTY_NOT_EXISTS);
        }

        // 2. 校验，一个 SKU 下，没有重复的规格。校验方式是，遍历每个 SKU ，看看是否有重复的规格 propertyId
        Map<Long, ProductPropertyValueRespVO> propertyValueMap = CollectionUtils.convertMap(productPropertyValueService.getPropertyValueListByPropertyId(new ArrayList<>(propertyIds)), ProductPropertyValueRespVO::getId);
        skus.forEach(sku -> {
            Set<Long> skuPropertyIds = convertSet(sku.getProperties(), propertyItem -> propertyValueMap.get(propertyItem.getValueId()).getPropertyId());
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
            if (!skuAttrValues.add(convertSet(sku.getProperties(), ProductSkuBaseVO.Property::getValueId))) { // 添加失败，说明重复
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
        return productSkuMapper.selectList(ProductSkuDO::getSpuId, spuId);
    }

    @Override
    public List<ProductSkuDO> getSkusBySpuIds(List<Long> spuIds) {
        return productSkuMapper.selectList(ProductSkuDO::getSpuId, spuIds);
    }

    @Override
    public void deleteSkuBySpuId(Long spuId) {
        productSkuMapper.deleteBySpuId(spuId);
    }

    @Override
    public List<ProductSkuDO> getSkusByAlarmStock() {
        return productSkuMapper.selectListByAlarmStock();
    }

    @Override
    @Transactional
    public void updateSkus(Long spuId, List<ProductSkuCreateOrUpdateReqVO> skus) {
        // 查询 SPU 下已经存在的 SKU 的集合
        List<ProductSkuDO> existsSkus = productSkuMapper.selectListBySpuId(spuId);
        // 构建规格与 SKU 的映射关系;
        // TODO @luowenfeng: 可以下 existsSkuMap2; 会简洁一点; 另外, 可以考虑抽一个小方法, 用于 Properties 生成一个串; 这样 177 也可以复用了
        Map<String, Long> existsSkuMap = existsSkus.stream()
                .map(v -> {
                    String collect = v.getProperties() == null? "null": v.getProperties()
                            .stream()
                            .map(m -> String.valueOf(m.getValueId()))
                            .collect(Collectors.joining());
                    return String.join("-", collect, String.valueOf(v.getId()));
                })
                .collect(Collectors.toMap(v -> v.split("-")[0], v -> Long.valueOf(v.split("-")[1])));

        // 拆分三个集合，新插入的、需要更新的、需要删除的
        List<ProductSkuDO> insertSkus = new ArrayList<>();
        List<ProductSkuDO> updateSkus = new ArrayList<>();
        List<Long> deleteSkus = new ArrayList<>();

        List<ProductSkuDO> allUpdateSkus = ProductSkuConvert.INSTANCE.convertSkuDOList(skus);
        allUpdateSkus.forEach(p -> {
            String propertiesKey = p.getProperties() == null? "null": p.getProperties().stream().map(m -> String.valueOf(m.getValueId())).collect(Collectors.joining());
            // 1、找得到的，进行更新
            if (existsSkuMap.containsKey(propertiesKey)) {
                updateSkus.add(p);
                existsSkuMap.remove(propertiesKey);
                return;
            }
            // 2、找不到，进行插入
            p.setSpuId(spuId);
            insertSkus.add(p);
        });
        // 3、多余的，删除
        if(!existsSkuMap.isEmpty()){
            deleteSkus = new ArrayList<>(existsSkuMap.values());
        }

        // 4、执行修改 Sku
        if (!insertSkus.isEmpty()) {
            productSkuMapper.insertBatch(insertSkus);
        }
        if (!updateSkus.isEmpty()) {
            updateSkus.forEach(p -> productSkuMapper.updateById(p));
        }
        if (!deleteSkus.isEmpty()) {
            productSkuMapper.deleteBatchIds(deleteSkus);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO) {
        // 更新 SKU 库存
        updateStockReqDTO.getItems().forEach(item -> {
            if (item.getIncrCount() > 0) {
                productSkuMapper.updateStockIncr(item.getId(), item.getIncrCount());
            } else if (item.getIncrCount() < 0) {
                int updateStockIncr = productSkuMapper.updateStockDecr(item.getId(), item.getIncrCount());
                if (updateStockIncr == 0) {
                    throw exception(SKU_STOCK_NOT_ENOUGH);
                }
            }
        });

        // 更新 SPU 库存
        List<ProductSkuDO> skus = productSkuMapper.selectBatchIds(
                convertSet(updateStockReqDTO.getItems(), ProductSkuUpdateStockReqDTO.Item::getId));
        Map<Long, Integer> spuStockIncrCounts = ProductSkuConvert.INSTANCE.convertSpuStockMap(
                updateStockReqDTO.getItems(), skus);
        productSpuService.updateSpuStock(spuStockIncrCounts);
    }

}
