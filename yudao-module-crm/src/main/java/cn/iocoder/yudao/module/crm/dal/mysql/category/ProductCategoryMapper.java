package cn.iocoder.yudao.module.crm.dal.mysql.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategoryPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.category.ProductCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品分类 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapperX<ProductCategoryDO> {

    /**
     * 分页查询
     */
    default PageResult<ProductCategoryDO> selectPage(ProductCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductCategoryDO>()
                .likeIfPresent(ProductCategoryDO::getName, reqVO.getName())
                .eqIfPresent(ProductCategoryDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProductCategoryDO::getParentId, reqVO.getParentId())
                .orderByAsc(ProductCategoryDO::getSort)
                .orderByDesc(ProductCategoryDO::getId));
    }

    /**
     * 根据父分类ID查询子分类
     */
    default List<ProductCategoryDO> selectListByParentId(Long parentId) {
        return selectList(ProductCategoryDO::getParentId, parentId);
    }

    /**
     * 根据分类名称查询
     */
    default ProductCategoryDO selectByName(String name) {
        return selectOne(ProductCategoryDO::getName, name);
    }

}
