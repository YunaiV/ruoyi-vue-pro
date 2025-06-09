package cn.iocoder.yudao.module.tms.service.logistic.category.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.TmsCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.TmsCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.TmsCustomProductSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.TmsCustomProductDO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.product.TmsCustomProductMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.TmsCustomCategoryService;
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
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.CUSTOM_PRODUCT_EXISTS;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.CUSTOM_PRODUCT_NOT_EXISTS;
import static cn.iocoder.yudao.module.tms.dal.redis.TmsRedisKeyConstants.TMS_CUSTOM_PRODUCT;
import static cn.iocoder.yudao.module.tms.dal.redis.TmsRedisKeyConstants.TMS_CUSTOM_PRODUCT_LIST;

/**
 * 海关产品分类表 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class TmsCustomProductServiceImpl implements TmsCustomProductService {

    @Resource
    private TmsCustomProductMapper customProductMapper;
    @Autowired
    ErpProductApi erpProductApi;
    @Autowired
    TmsCustomCategoryService tmsCustomCategoryService;
    @Autowired
    TmsCustomRuleApi tmsCustomRuleApi;
    @Resource
    MessageChannel erpCustomRuleChannel;

    @Override
    @CacheEvict(value = TMS_CUSTOM_PRODUCT_LIST, allEntries = true)
    public Long createCustomProduct(TmsCustomProductSaveReqVO vo) {
        //校验当前SKU在数据库中是否已经存在
        validHasProductId(vo.getProductId());
        //产品存在+分类存在
        validData(vo);
        // 插入
        TmsCustomProductDO customProduct = BeanUtils.toBean(vo, TmsCustomProductDO.class);
        customProductMapper.insert(customProduct);
        // 返回
        this.syncErpCustomRule(vo.getProductId());
        return customProduct.getId();
    }

    /**
     * 校验产品id是否已经存在
     *
     * @param productId productId
     */
    private void validHasProductId(Long productId) {
        if (customProductMapper.getCustomProductByProductId(productId) != null) {
            throw exception(CUSTOM_PRODUCT_EXISTS);
        }
    }

    /**
     * 校验产品+规则id 是否存在
     *
     * @param vo 创建或修改的VO
     */
    private void validData(TmsCustomProductSaveReqVO vo) {
        erpProductApi.validProductList(Collections.singletonList(vo.getProductId()));
        tmsCustomCategoryService.validCustomRuleCategory(Collections.singletonList(vo.getCustomCategoryId()));
    }

    @CacheEvict(value = TMS_CUSTOM_PRODUCT_LIST, allEntries = true)
    @CachePut(value = TMS_CUSTOM_PRODUCT, key = "#vo.getId()")
    public TmsCustomProductDO updateCustomProduct(TmsCustomProductSaveReqVO vo) {
        validData(vo);
        // 校验存在
        TmsCustomProductDO customProductDO = validateCustomProductExists(vo.getId());
        // 校验变更的产品id是否存在
        if (!customProductDO.getProductId().equals(vo.getProductId())) {
            validHasProductId(vo.getProductId());
        }
        // 更新
        TmsCustomProductDO updateObj = BeanUtils.toBean(vo, TmsCustomProductDO.class);
        customProductMapper.updateById(updateObj);
        this.syncErpCustomRule(vo.getProductId());
        return updateObj;
    }

    //同步海关规则方法
    private void syncErpCustomRule(Long productId) {
        //更新产品时->覆盖n个海关规则
        //找到产品id对应的所有海关规则DTO(含海关信息+海关分类)，如果没有海关分类信息，那么就不更新海关规则
        List<TmsCustomRuleDTO> dtos = new ArrayList<>();
        // 从产品ID获取海关规则 + 从分类ID获取海关规则
        Optional.ofNullable(tmsCustomRuleApi.listCustomRuleDTOsByProductId(productId)).ifPresent(dtos::addAll);
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

    private TmsCustomProductDO validateCustomProductExists(Long id) {
        TmsCustomProductDO tmsCustomProductDO = customProductMapper.selectById(id);
        if (tmsCustomProductDO == null) {
            throw exception(CUSTOM_PRODUCT_NOT_EXISTS);
        }
        return tmsCustomProductDO;
    }

    @Override
    @Cacheable(value = TMS_CUSTOM_PRODUCT, key = "#id", unless = "#result == null")
    public TmsCustomProductDO getCustomProduct(Long id) {
        return customProductMapper.selectById(id);
    }

    @Override
    @Cacheable(value = TMS_CUSTOM_PRODUCT_LIST, key = "#pageReqVO", unless = "#result == null")
    public PageResult<TmsCustomProductDO> getCustomProductPage(TmsCustomProductPageReqVO pageReqVO) {
        //打印URL
        return customProductMapper.selectPage(pageReqVO);
    }

    @Override
    public TmsCustomProductDO getCustomProductByProductId(Long productId) {
        //根据产品id查询海关产品分类表
        return customProductMapper.getCustomProductByProductId(productId);
    }

    @Override
    @Cacheable(value = TMS_CUSTOM_PRODUCT_LIST, key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<TmsCustomProductDO> listCustomProductList() {
        return customProductMapper.selectList();
    }
}