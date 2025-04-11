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
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.TmsCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.TmsCustomProductSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.TmsCustomProductDO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.product.TmsCustomProductMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.TmsCustomCategoryService;
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
public class TmsCustomProductServiceImpl implements TmsCustomProductService {

    @Resource
    private TmsCustomProductMapper customProductMapper;
    @Autowired
    ErpProductApi erpProductApi;
    @Autowired
    TmsCustomCategoryService tmsCustomCategoryService;

    @Override
    @CacheEvict(value = TMS_CUSTOM_PRODUCT_LIST, allEntries = true)
    public Long createCustomProduct(TmsCustomProductSaveReqVO createReqVO) {
        //校验存在
        //产品存在+分类存在
        validData(createReqVO);
        // 插入
        TmsCustomProductDO customProduct = BeanUtils.toBean(createReqVO, TmsCustomProductDO.class);
        customProductMapper.insert(customProduct);
        // 返回
        this.syncErpCustomRule(vo.getProductId());
        return customProduct.getId();
    }

    private void validData(TmsCustomProductSaveReqVO vo) {
        erpProductApi.validProductList(Collections.singletonList(vo.getProductId()));
        tmsCustomCategoryService.validCustomRuleCategory(Collections.singletonList(vo.getCustomCategoryId()));
    }

    @CacheEvict(value = TMS_CUSTOM_PRODUCT_LIST, allEntries = true)
    @CachePut(value = TMS_CUSTOM_PRODUCT, key = "#updateReqVO.getId()")
    public TmsCustomProductDO updateCustomProduct(TmsCustomProductSaveReqVO updateReqVO) {
        validData(updateReqVO);
        // 校验存在
        validateCustomProductExists(updateReqVO.getId());
        // 更新
        TmsCustomProductDO updateObj = BeanUtils.toBean(updateReqVO, TmsCustomProductDO.class);
        customProductMapper.updateById(updateObj);
        return updateObj;
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
        TmsCustomProductDO customProduct = customProductMapper.selectOne(new LambdaQueryWrapper<TmsCustomProductDO>().eq(TmsCustomProductDO::getProductId, productId));
        return customProduct;
    }

    @Override
    @Cacheable(value = TMS_CUSTOM_PRODUCT_LIST, key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<TmsCustomProductDO> listCustomProductList() {
        return customProductMapper.selectList();
    }
}