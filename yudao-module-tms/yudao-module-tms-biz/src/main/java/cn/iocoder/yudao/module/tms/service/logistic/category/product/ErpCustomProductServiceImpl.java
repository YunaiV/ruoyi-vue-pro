package cn.iocoder.yudao.module.tms.service.logistic.category.product;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.ErpCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.ErpCustomProductDO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.product.ErpCustomProductMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.ErpCustomCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_PRODUCT_NOT_EXISTS;
import static cn.iocoder.yudao.module.tms.dal.redis.TmsRedisKeyConstants.TMS_CUSTOM_PRODUCT;
import static cn.iocoder.yudao.module.tms.dal.redis.TmsRedisKeyConstants.TMS_CUSTOM_PRODUCT_LIST;

/**
 * 海关产品分类表 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class ErpCustomProductServiceImpl implements ErpCustomProductService {

    @Resource
    private ErpCustomProductMapper customProductMapper;
    @Autowired
    ErpProductApi erpProductApi;
    @Autowired
    ErpCustomCategoryService erpCustomCategoryService;
    @Autowired
    ErpCustomRuleApi erpCustomRuleApi;
    @Resource
    MessageChannel erpCustomRuleChannel;

    @Override
    @CacheEvict(value = TMS_CUSTOM_PRODUCT_LIST, allEntries = true)
    public Long createCustomProduct(ErpCustomProductSaveReqVO vo) {
        //校验存在
        //产品存在+分类存在
        validData(vo);
        // 插入
        ErpCustomProductDO customProduct = BeanUtils.toBean(vo, ErpCustomProductDO.class);
        customProductMapper.insert(customProduct);
        // 返回
        this.syncErpCustomRule(vo.getProductId());
        return customProduct.getId();
    }

    private void validData(ErpCustomProductSaveReqVO vo) {
        erpProductApi.validProductList(Collections.singletonList(vo.getProductId()));
        erpCustomCategoryService.validCustomRuleCategory(Collections.singletonList(vo.getCustomCategoryId()));
    }

    @CacheEvict(value = TMS_CUSTOM_PRODUCT_LIST, allEntries = true)
    @CachePut(value = TMS_CUSTOM_PRODUCT, key = "#vo.getId()")
    public ErpCustomProductDO updateCustomProduct(ErpCustomProductSaveReqVO vo) {
        validData(vo);
        // 校验存在
        validateCustomProductExists(vo.getId());
        // 更新
        ErpCustomProductDO updateObj = BeanUtils.toBean(vo, ErpCustomProductDO.class);
        customProductMapper.updateById(updateObj);
        this.syncErpCustomRule(vo.getProductId());
        return updateObj;
    }

    //同步海关规则方法
    private void syncErpCustomRule(Long productId) {
        //更新产品时->覆盖n个海关规则
        //找到产品id对应的所有海关规则DTO(含海关信息+海关分类)，如果没有海关分类信息，那么就不更新海关规则
        List<ErpCustomRuleDTO> dtos = new ArrayList<>();
        // 从产品ID获取海关规则 + 从分类ID获取海关规则
        Optional.ofNullable(erpCustomRuleApi.listDTOsByProductId(productId))
            .ifPresent(dtos::addAll);
        if (!dtos.isEmpty()) {
            erpCustomRuleChannel.send(MessageBuilder.withPayload(dtos.stream().distinct().toList()).build());
        }
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = TMS_CUSTOM_PRODUCT, key = "#id"),
        @CacheEvict(value = TMS_CUSTOM_PRODUCT_LIST, allEntries = true)
    })
    public void deleteCustomProduct(Long id) {
        // 校验存在
        validateCustomProductExists(id);
        // 删除
        customProductMapper.deleteById(id);
    }

    private void validateCustomProductExists(Long id) {
        if (customProductMapper.selectById(id) == null) {
            throw exception(CUSTOM_PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    @Cacheable(value = TMS_CUSTOM_PRODUCT, key = "#id", unless = "#result == null")
    public ErpCustomProductDO getCustomProduct(Long id) {
        return customProductMapper.selectById(id);
    }

    @Override
    @Cacheable(value = TMS_CUSTOM_PRODUCT_LIST, key = "#pageReqVO", unless = "#result == null")
    public PageResult<ErpCustomProductDO> getCustomProductPage(ErpCustomProductPageReqVO pageReqVO) {
        //打印URL
        return customProductMapper.selectPage(pageReqVO);
    }

    @Override
    public ErpCustomProductDO getCustomProductByProductId(Long productId) {
        //根据产品id查询海关产品分类表
        ErpCustomProductDO customProduct = customProductMapper.selectOne(new LambdaQueryWrapper<ErpCustomProductDO>().eq(ErpCustomProductDO::getProductId, productId));
        return customProduct;
    }

    @Override
    @Cacheable(value = TMS_CUSTOM_PRODUCT_LIST, key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<ErpCustomProductDO> listCustomProductList() {
        return customProductMapper.selectList();
    }
}