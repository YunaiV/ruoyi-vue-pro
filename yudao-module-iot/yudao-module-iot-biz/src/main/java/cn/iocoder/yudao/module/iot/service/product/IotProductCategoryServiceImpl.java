package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategorySaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductCategoryMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductCategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_CATEGORY_NOT_EXISTS;

/**
 * IoT 产品分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotProductCategoryServiceImpl implements IotProductCategoryService {

    @Resource
    private IotProductCategoryMapper productCategoryMapper;

    @Resource
    private IotProductService productService;

    @Resource
    private IotDeviceService deviceService;

    public Long createProductCategory(IotProductCategorySaveReqVO createReqVO) {
        // 插入
        IotProductCategoryDO productCategory = BeanUtils.toBean(createReqVO, IotProductCategoryDO.class);
        productCategoryMapper.insert(productCategory);
        // 返回
        return productCategory.getId();
    }

    @Override
    public void updateProductCategory(IotProductCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateProductCategoryExists(updateReqVO.getId());
        // 更新
        IotProductCategoryDO updateObj = BeanUtils.toBean(updateReqVO, IotProductCategoryDO.class);
        productCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductCategory(Long id) {
        // 校验存在
        validateProductCategoryExists(id);
        // 删除
        productCategoryMapper.deleteById(id);
    }

    private void validateProductCategoryExists(Long id) {
        if (productCategoryMapper.selectById(id) == null) {
            throw exception(PRODUCT_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public IotProductCategoryDO getProductCategory(Long id) {
        return productCategoryMapper.selectById(id);
    }

    @Override
    public List<IotProductCategoryDO> getProductCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return productCategoryMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotProductCategoryDO> getProductCategoryPage(IotProductCategoryPageReqVO pageReqVO) {
        return productCategoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotProductCategoryDO> getProductCategoryListByStatus(Integer status) {
        return productCategoryMapper.selectListByStatus(status);
    }

    @Override
    public Long getProductCategoryCount(LocalDateTime createTime) {
        return productCategoryMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public List<IotStatisticsRespVO.DataItem> getDeviceCountsOfProductCategory() {
        // 1. 获取所有数据
        List<IotProductCategoryDO> categoryList = productCategoryMapper.selectList();
        List<IotProductDO> productList = productService.getProductList();
        List<IotDeviceDO> deviceList = deviceService.getDeviceList();

        // 2. 统计每个分类下的设备数量
        Map<String, Integer> categoryDeviceCountMap = new HashMap<>();
        
        // 2.1 初始化所有分类的计数为0
        for (IotProductCategoryDO category : categoryList) {
            categoryDeviceCountMap.put(category.getName(), 0);
        }
        
        // 2.2 构建产品ID到分类的映射
        Map<Long, IotProductCategoryDO> productCategoryMap = new HashMap<>();
        for (IotProductDO product : productList) {
            Long categoryId = product.getCategoryId();
            IotProductCategoryDO category = categoryList.stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .findFirst()
                    .orElse(null);
            if (category != null) {
                productCategoryMap.put(product.getId(), category);
            }
        }
        
        // 2.3 统计每个分类下的设备数量
        for (IotDeviceDO device : deviceList) {
            Long productId = device.getProductId();
            IotProductCategoryDO category = productCategoryMap.get(productId);
            if (category != null) {
                String categoryName = category.getName();
                categoryDeviceCountMap.merge(categoryName, 1, Integer::sum);
            }
        }

        // 3. 转换为 DataItem 列表
        return categoryDeviceCountMap.entrySet().stream()
                .map(entry -> {
                    IotStatisticsRespVO.DataItem dataItem = new IotStatisticsRespVO.DataItem();
                    dataItem.setName(entry.getKey());
                    dataItem.setValue(entry.getValue());
                    return dataItem;
                })
                .collect(Collectors.toList());
    }

}