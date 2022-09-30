package cn.iocoder.yudao.module.product.service.property;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.*;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.convert.property.ProductPropertyConvert;
import cn.iocoder.yudao.module.product.convert.propertyvalue.ProductPropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyMapper;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyValueMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_NOT_EXISTS;

/**
 * 规格名称 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductPropertyServiceImpl implements ProductPropertyService {

    @Resource
    private ProductPropertyMapper productPropertyMapper;

    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProperty(ProductPropertyCreateReqVO createReqVO) {
        // TODO @luowenfeng: 插入和更新的时候, 要校验 name 的唯一性;
        // 插入
        ProductPropertyDO property = ProductPropertyConvert.INSTANCE.convert(createReqVO);
        productPropertyMapper.insert(property);
        // 返回
        return property.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProperty(ProductPropertyUpdateReqVO updateReqVO) {
        // 校验存在
        this.validatePropertyExists(updateReqVO.getId());
        // 更新
        ProductPropertyDO updateObj = ProductPropertyConvert.INSTANCE.convert(updateReqVO);
        productPropertyMapper.updateById(updateObj);
    }

    @Override
    public void deleteProperty(Long id) {
        // 校验存在
        this.validatePropertyExists(id);
        // 删除
        productPropertyMapper.deleteById(id);
        //同步删除属性值
        productPropertyValueMapper.deletePropertyValueByPropertyId(id);
    }

    private void validatePropertyExists(Long id) {
        if (productPropertyMapper.selectById(id) == null) {
            throw exception(PROPERTY_NOT_EXISTS);
        }
    }

    @Override
    public ProductPropertyDO getProperty(Long id) {
        return productPropertyMapper.selectById(id);
    }

    @Override
    public List<ProductPropertyRespVO> getPropertyList(ProductPropertyListReqVO listReqVO) {
        return ProductPropertyConvert.INSTANCE.convertList(productPropertyMapper.selectList(new LambdaQueryWrapperX<ProductPropertyDO>()
                .likeIfPresent(ProductPropertyDO::getName, listReqVO.getName())
                .eqIfPresent(ProductPropertyDO::getStatus, listReqVO.getStatus())));
    }

    @Override
    public PageResult<ProductPropertyRespVO> getPropertyPage(ProductPropertyPageReqVO pageReqVO) {
        //获取属性列表
        PageResult<ProductPropertyDO> pageResult = productPropertyMapper.selectPage(pageReqVO);
        PageResult<ProductPropertyRespVO> propertyRespVOPageResult = ProductPropertyConvert.INSTANCE.convertPage(pageResult);
        // TODO @luofengwen: 下面的代码, 如果不要,可以删除哈; git 可以拿到记录的
//        List<Long> propertyIds = propertyRespVOPageResult.getList().stream().map(ProductPropertyAndValueRespVO::getId).collect(Collectors.toList());
//
//        //获取属性值列表
//        List<ProductPropertyValueDO> productPropertyValueDOList = productPropertyValueMapper.getPropertyValueListByPropertyId(propertyIds);
//        List<ProductPropertyValueRespVO> propertyValueRespVOList = ProductPropertyValueConvert.INSTANCE.convertList(productPropertyValueDOList);
//        //组装一对多
//        propertyRespVOPageResult.getList().forEach(x->{
//            Long propertyId = x.getId();
//            List<ProductPropertyValueRespVO> valueDOList = propertyValueRespVOList.stream().filter(v -> v.getPropertyId().equals(propertyId)).collect(Collectors.toList());
//            x.setValues(valueDOList);
//        });
        return propertyRespVOPageResult;
    }

    private List<ProductPropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds) {
        return productPropertyValueMapper.getPropertyValueListByPropertyId(propertyIds);
    }

    @Override
    public ProductPropertyRespVO getPropertyResp(Long id) {
        ProductPropertyDO property = getProperty(id);
        return ProductPropertyConvert.INSTANCE.convert(property);
    }

    @Override
    public List<ProductPropertyRespVO> getPropertyList(Collection<Long> ids) {
        return ProductPropertyConvert.INSTANCE.convertList(productPropertyMapper.selectBatchIds(ids));
    }

    @Override
    public List<ProductPropertyAndValueRespVO> getPropertyAndValueList(ProductPropertyListReqVO listReqVO) {
        List<ProductPropertyRespVO> propertyList = getPropertyList(listReqVO);

        // 查询属性值
        List<ProductPropertyValueDO> valueDOList = productPropertyValueMapper.getPropertyValueListByPropertyId(CollectionUtils.convertList(propertyList, ProductPropertyRespVO::getId));
//        CollectionUtils.convertMultiMap() // TODO @luofengwen: 可以使用这个方法哈
        Map<Long, List<ProductPropertyValueRespVO>> valueDOMap = valueDOList.stream()
                .map(ProductPropertyValueConvert.INSTANCE::convert)
                .collect(Collectors.groupingBy(ProductPropertyValueRespVO::getPropertyId));
        // 组装 TODO @luowenfeng: CollectionUtils 转换哈;
        return propertyList.stream().map(m -> {
            // TODO @luowenfeng: 使用 mapstruct convert 哈
            ProductPropertyAndValueRespVO productPropertyAndValueRespVO = BeanUtil.copyProperties(m, ProductPropertyAndValueRespVO.class);
            productPropertyAndValueRespVO.setValues(valueDOMap.get(m.getId()));
            return productPropertyAndValueRespVO;
        }).collect(Collectors.toList());
    }
}
