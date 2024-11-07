package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.aop.SynExternalData;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.aop.SynExternalDataAspect.ERP_PRODUCT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 产品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@RequiredArgsConstructor
public class ErpProductServiceImpl implements ErpProductService {

    private final ErpProductMapper productMapper;

    private final ErpProductCategoryService productCategoryService;

    private final ErpProductUnitService productUnitService;

    private final DeptApi deptApi;

    @Override
    public Long createProduct(ProductSaveReqVO createReqVO) {
        //校验是否存在相同的产品编码
        validateProductCodeUnique(null,createReqVO.getBarCode());
        //获取部门id
        Long deptId = createReqVO.getDeptId();
        //校验部门id的合法性
        validateDept(deptId);
        // 插入
        ErpProductDO product = BeanUtils.toBean(createReqVO, ErpProductDO.class);
        productMapper.insert(product);
        // 返回
        return product.getId();
    }

    /**
    * @Author Wqh
    * @Description 校验部门的合法性
     {当前只允许二级部门和三级部门上传}
    * @Date 14:51 2024/10/30
    * @Param [deptId]
    **/
    private void validateDept(Long deptId) {
        List<Integer> levels = Arrays.asList(2, 3);
        //获取部门等级
        Integer level = deptApi.getDeptLevel(deptId);
        //判断登记是否符合要求
        ThrowUtil.ifThrow(!levels.contains(level), DEPT_LEVEL_NOT_MATCH);
    }

    @Override
    @SynExternalData(table = ERP_PRODUCT,inClazz = ProductSaveReqVO.class)
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        //校验相同的id下是否存在相同的产品编码
        validateProductCodeUnique(updateReqVO.getId(),updateReqVO.getBarCode());
        // 校验存在
        validateProductExists(updateReqVO.getId());
        Long deptId = updateReqVO.getDeptId();
        this.validateDept(deptId);
        // 更新
        ErpProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductDO.class);
        productMapper.updateById(updateObj);
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        // 删除
        productMapper.deleteById(id);
    }

    @Override
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectBatchIds(ids);
        Map<Long, ErpProductDO> productMap = convertMap(list, ErpProductDO::getId);
        for (Long id : ids) {
            ErpProductDO product = productMap.get(id);
            if (productMap.get(id) == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(product.getStatus())) {
                throw exception(PRODUCT_NOT_ENABLE, product.getName());
            }
        }
        return list;
    }

    private void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private void validateProductCodeUnique(Long id, String code) {
        ErpProductDO product = productMapper.selectByCode(code);
        if (ObjUtil.isEmpty(product)){
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null){
            throw exception(PRODUCT_CODE_DUPLICATE);
        }
        if (!product.getId().equals(id)) {
            throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
        }
    }

    @Override
    public ErpProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status) {
        List<ErpProductDO> list = productMapper.selectListByStatus(status);
        return buildProductVOList(list);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectBatchIds(ids);
        return buildProductVOList(list);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = productMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }

    private List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
                convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(list, ErpProductDO::getUnitId));
        return BeanUtils.toBean(list, ErpProductRespVO.class, product -> {
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> product.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                    unit -> product.setUnitName(unit.getName()));
        });
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        return productMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        return productMapper.selectCountByUnitId(unitId);
    }

}