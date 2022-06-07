package cn.iocoder.yudao.module.product.service.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.ProductPropertyValueCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.convert.property.ProductPropertyConvert;
import cn.iocoder.yudao.module.product.convert.propertyvalue.ProductPropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyMapper;
import cn.iocoder.yudao.module.product.dal.mysql.propertyvalue.ProductPropertyValueMapper;
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
        // 插入
        ProductPropertyDO property = ProductPropertyConvert.INSTANCE.convert(createReqVO);
        productPropertyMapper.insert(property);

        //插入属性值
        List<ProductPropertyValueCreateReqVO> propertyValueList = createReqVO.getPropertyValueList();
        List<ProductPropertyValueDO> productPropertyValueDOList = ProductPropertyValueConvert.INSTANCE.convertList03(propertyValueList);
        productPropertyValueDOList.stream().forEach(x-> x.setPropertyId(property.getId()));
        productPropertyValueMapper.insertBatch(productPropertyValueDOList);
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
        //更新属性值，先删后加
        productPropertyValueMapper.deletePropertyValueByPropertyId(updateReqVO.getId());
        List<ProductPropertyValueCreateReqVO> propertyValueList = updateReqVO.getPropertyValueList();
        List<ProductPropertyValueDO> productPropertyValueDOList = ProductPropertyValueConvert.INSTANCE.convertList03(propertyValueList);
        productPropertyValueDOList.stream().forEach(x-> x.setPropertyId(updateReqVO.getId()));
        productPropertyValueMapper.insertBatch(productPropertyValueDOList);
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
    public List<ProductPropertyDO> getPropertyList(Collection<Long> ids) {
        return productPropertyMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductPropertyDO> getPropertyPage(ProductPropertyPageReqVO pageReqVO) {
        return productPropertyMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductPropertyDO> getPropertyList(ProductPropertyExportReqVO exportReqVO) {
        return productPropertyMapper.selectList(exportReqVO);
    }

    @Override
    public PageResult<ProductPropertyRespVO> getPropertyListPage(ProductPropertyPageReqVO pageReqVO) {
        //获取属性列表
        PageResult<ProductPropertyDO> pageResult = productPropertyMapper.selectPage(pageReqVO);
        PageResult<ProductPropertyRespVO> propertyRespVOPageResult = ProductPropertyConvert.INSTANCE.convertPage(pageResult);
        List<Long> propertyIds = propertyRespVOPageResult.getList().stream().map(ProductPropertyRespVO::getId).collect(Collectors.toList());

        //获取属性值列表
        List<ProductPropertyValueDO> productPropertyValueDOList = productPropertyValueMapper.getPropertyValueListByPropertyId(propertyIds);
        List<ProductPropertyValueRespVO> propertyValueRespVOList = ProductPropertyValueConvert.INSTANCE.convertList(productPropertyValueDOList);
        //组装一对多
        propertyRespVOPageResult.getList().forEach(x->{
            Long propertyId = x.getId();
            List<ProductPropertyValueRespVO> valueDOList = propertyValueRespVOList.stream().filter(v -> v.getPropertyId().equals(propertyId)).collect(Collectors.toList());
            x.setPropertyValueList(valueDOList);
        });
        return propertyRespVOPageResult;
    }

    private List<ProductPropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds) {
        return productPropertyValueMapper.getPropertyValueListByPropertyId(propertyIds);
    }

    @Override
    public ProductPropertyRespVO getPropertyResp(Long id) {
        //查询规格
        ProductPropertyDO property = getProperty(id);
        ProductPropertyRespVO propertyRespVO = ProductPropertyConvert.INSTANCE.convert(property);
        //查询属性值
        List<ProductPropertyValueDO> valueDOList = productPropertyValueMapper.getPropertyValueListByPropertyId(Arrays.asList(id));
        List<ProductPropertyValueRespVO> propertyValueRespVOS = ProductPropertyValueConvert.INSTANCE.convertList(valueDOList);
        //组装
        propertyRespVO.setPropertyValueList(propertyValueRespVOS);
        return propertyRespVO;
    }

    @Override
    public List<ProductPropertyRespVO> selectByIds(List<Long> propertyIds) {
        List<ProductPropertyRespVO> productPropertyRespVO = ProductPropertyConvert.INSTANCE.convertList(productPropertyMapper.selectBatchIds(propertyIds));
        //查询属性值
        List<ProductPropertyValueDO> valueDOList = productPropertyValueMapper.getPropertyValueListByPropertyId(propertyIds);
        Map<Long, List<ProductPropertyValueDO>> propertyValuesMap = valueDOList.stream().collect(Collectors.groupingBy(ProductPropertyValueDO::getPropertyId));
        productPropertyRespVO.forEach(p -> p.setPropertyValueList(ProductPropertyValueConvert.INSTANCE.convertList(propertyValuesMap.get(p.getId()))));
        return productPropertyRespVO;
    }
}
