package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.tvstand.ErpProductTvStandServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * ERP 产品 Service 实现类
 *
 * @author 芋道源码
 */

@Service
public class ErpProductServiceDelegator implements ErpProductService {

    @Resource
    ApplicationContext applicationContext;

    // 分类Id映射ProductService的实现类
    private static final Map<Long, Class<?>> SERVICE_MAP = Map.of(
        87L, ErpProductTvStandServiceImpl.class
    );

    private ErpProductService getDefaultService() {
        return getService(null);
    }

    private ErpProductService getService(Long categoryId) {
        Class<?> serviceClass = SERVICE_MAP.get(categoryId);
        if (serviceClass == null){
            serviceClass = ErpProductServiceImpl.class;
        }
        Object serviceImpl = applicationContext.getBean(serviceClass);
        return (ErpProductService) serviceImpl;
    }

    @Override
    public Long createProduct(ErpProductSaveReqVO createReqVO) {
        ErpProductService service = getService(createReqVO.getCategoryId());
        return service.createProduct(createReqVO);
    }

    @Override
    public void updateProduct(ErpProductSaveReqVO updateReqVO) {
        ErpProductService service = getService(updateReqVO.getCategoryId());
        service.updateProduct(updateReqVO);
    }

    @Override
    public void deleteProduct(Long id) {
        ErpProductService service = getDefaultService();
        service.deleteProduct(id);
    }

    @Override
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        ErpProductService service = getDefaultService();
        return service.validProductList(ids);
    }

    @Override
    public ErpProductRespVO getProduct(Long id) {
        ErpProductService service = getDefaultService();
        return service.getProduct(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status) {
        ErpProductService service = getDefaultService();
        return service.getProductVOListByStatus(status);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        ErpProductService service = getDefaultService();
        return service.getProductVOList(ids);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        ErpProductService service = getService(pageReqVO.getCategoryId());
        return service.getProductVOPage(pageReqVO);
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        ErpProductService service = getService(categoryId);
        return service.getProductCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        ErpProductService service = getDefaultService();
        return service.getProductCountByUnitId(unitId);
    }
}