package cn.iocoder.yudao.module.product.service.brand;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.ProductBrandCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.ProductBrandListReqVO;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.ProductBrandPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.ProductBrandUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.brand.ProductBrandConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.ProductBrandDO;
import cn.iocoder.yudao.module.product.dal.mysql.brand.ProductBrandMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.BRAND_NOT_EXISTS;

/**
 * 品牌 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductBrandServiceImpl implements ProductBrandService {

    @Resource
    private ProductBrandMapper brandMapper;

    @Override
    public Long createProductBrand(ProductBrandCreateReqVO createReqVO) {
        // 插入
        ProductBrandDO brand = ProductBrandConvert.INSTANCE.convert(createReqVO);
        brandMapper.insert(brand);
        // 返回
        return brand.getId();
    }

    @Override
    public void updateBrand(ProductBrandUpdateReqVO updateReqVO) {
        // 校验存在
        validateBrandExists(updateReqVO.getId());
        // 更新
        ProductBrandDO updateObj = ProductBrandConvert.INSTANCE.convert(updateReqVO);
        brandMapper.updateById(updateObj);
    }

    @Override
    public void deleteBrand(Long id) {
        // 校验存在
        this.validateBrandExists(id);
        // 删除
        brandMapper.deleteById(id);
    }

    private void validateBrandExists(Long id) {
        if (brandMapper.selectById(id) == null) {
            throw exception(BRAND_NOT_EXISTS);
        }
    }

    @Override
    public ProductBrandDO getBrand(Long id) {
        return brandMapper.selectById(id);
    }

    @Override
    public List<ProductBrandDO> getBrandList(Collection<Long> ids) {
        return brandMapper.selectBatchIds(ids);
    }

    @Override
    public List<ProductBrandDO> getBrandList(ProductBrandListReqVO listReqVO) {
        return brandMapper.selectList(listReqVO);
    }

    @Override
    public void validateProductBrand(Long id) {
        if(getBrand(id) == null){
            throw exception(BRAND_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<ProductBrandDO> getBrandPage(ProductBrandPageReqVO pageReqVO) {
        return brandMapper.selectPage(pageReqVO);
    }

}
