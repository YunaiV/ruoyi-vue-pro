package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 海关分类子表 Mapper
 *
 * @author 王岽宇
 */
@Mapper
public interface ErpCustomCategoryItemMapper extends BaseMapperX<ErpCustomCategoryItemDO> {
    //buildQueryWrapper
    default MPJLambdaWrapper<ErpCustomCategoryItemDO> buildQueryWrapper(ErpCustomCategoryItemPageReqVO reqVO) {
        return new MPJLambdaWrapperX<ErpCustomCategoryItemDO>()
            .selectAll(ErpCustomCategoryItemDO.class)
            .eqIfPresent(ErpCustomCategoryItemDO::getCountryCode, reqVO.getCountryCode())
            .eqIfPresent(ErpCustomCategoryItemDO::getTaxRate, reqVO.getTaxRate())
            .betweenIfPresent(ErpCustomCategoryItemDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(ErpCustomCategoryItemDO::getId)
            ;
    }

    default PageResult<ErpCustomCategoryItemDO> selectPage(ErpCustomCategoryItemPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO));
    }

    default List<ErpCustomCategoryItemDO> selectListByCategoryId(Long categoryId) {
        return selectList(new LambdaQueryWrapperX<ErpCustomCategoryItemDO>()
            .eq(ErpCustomCategoryItemDO::getCustomCategoryId, categoryId));
    }

    default List<ErpCustomCategoryItemDO> selectListByCategoryId(Collection<Long> categoryIds) {
        return selectList(new LambdaQueryWrapperX<ErpCustomCategoryItemDO>()
            .inIfPresent(ErpCustomCategoryItemDO::getCustomCategoryId, categoryIds));
    }

    default void deleteByCategoryId(Long categoryId) {
        delete(new LambdaQueryWrapperX<ErpCustomCategoryItemDO>()
            .eq(ErpCustomCategoryItemDO::getCustomCategoryId, categoryId));
    }
}