package cn.iocoder.yudao.module.crm.dal.mysql.productcategory;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.productcategory.ProductCategoryDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo.*;

/**
 * 产品分类 Mapper
 *
 * @author ZanGe丶
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapperX<ProductCategoryDO> {


    default List<ProductCategoryDO> selectList(ProductCategoryListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ProductCategoryDO>()
                .likeIfPresent(ProductCategoryDO::getName, reqVO.getName())
                .eqIfPresent(ProductCategoryDO::getParentId, reqVO.getParentId())
                .orderByDesc(ProductCategoryDO::getId));
    }

}
