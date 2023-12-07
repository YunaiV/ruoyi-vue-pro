package cn.iocoder.yudao.module.crm.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.dal.mysql.product.CrmProductMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

// TODO 芋艿：数据权限
/**
 * CRM 产品 Service 实现类
 *
 * @author ZanGe丶
 */
@Service
@Validated
public class CrmProductServiceImpl implements CrmProductService {

    @Resource(name = "crmProductMapper")
    private CrmProductMapper productMapper;

    @Resource
    private CrmProductCategoryService productCategoryService;
    @Resource
    private CrmPermissionService permissionService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createProduct(CrmProductSaveReqVO createReqVO) {
        // 校验产品
        adminUserApi.validateUserList(Collections.singleton(createReqVO.getOwnerUserId()));
        validateProductNoDuplicate(null, createReqVO.getNo());
        validateProductCategoryExists(createReqVO.getCategoryId());

        // 插入产品
        CrmProductDO product = BeanUtils.toBean(createReqVO, CrmProductDO.class);
        productMapper.insert(product);

        // 插入数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(product.getOwnerUserId())
                .setBizType(CrmBizTypeEnum.CRM_PRODUCT.getType()).setBizId(product.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        return product.getId();
    }

    @Override
    public void updateProduct(CrmProductSaveReqVO updateReqVO) {
        // 校验产品
        updateReqVO.setOwnerUserId(null); // 不修改负责人
        validateProductExists(updateReqVO.getId());
        validateProductNoDuplicate(updateReqVO.getId(), updateReqVO.getNo());
        validateProductCategoryExists(updateReqVO.getCategoryId());

        // 更新产品
        CrmProductDO updateObj = BeanUtils.toBean(updateReqVO, CrmProductDO.class);
        productMapper.updateById(updateObj);
    }

    private void validateProductExists(Long id) {
        CrmProductDO product = productMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private void validateProductNoDuplicate(Long id, String no) {
        CrmProductDO product = productMapper.selectByNo(no);
        if (product == null
            || product.getId().equals(id)) {
            return;
        }
        throw exception(PRODUCT_NO_EXISTS);
    }

    private void validateProductCategoryExists(Long categoryId) {
        CrmProductCategoryDO category = productCategoryService.getProductCategory(categoryId);
        if (category == null) {
            throw exception(PRODUCT_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        // 删除
        productMapper.deleteById(id);
    }

    @Override
    public CrmProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<CrmProductDO> getProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return productMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmProductDO> getProductPage(CrmProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    public CrmProductDO getProductByCategoryId(Long categoryId) {
        return productMapper.selectOne(new LambdaQueryWrapper<CrmProductDO>().eq(CrmProductDO::getCategoryId, categoryId));
    }

}
