package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.TmsCustomCategoryItemPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.TmsCustomCategoryItemDO;
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
public interface TmsCustomCategoryItemMapper extends BaseMapperX<TmsCustomCategoryItemDO> {
    //buildQueryWrapper
    default MPJLambdaWrapper<TmsCustomCategoryItemDO> buildQueryWrapper(TmsCustomCategoryItemPageReqVO reqVO) {
        return new MPJLambdaWrapperX<TmsCustomCategoryItemDO>()
            .selectAll(TmsCustomCategoryItemDO.class)
            .eqIfPresent(TmsCustomCategoryItemDO::getCountryCode, reqVO.getCountryCode())
            .eqIfPresent(TmsCustomCategoryItemDO::getTaxRate, reqVO.getTaxRate())
            .betweenIfPresent(TmsCustomCategoryItemDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(TmsCustomCategoryItemDO::getId)
            ;
    }

    default PageResult<TmsCustomCategoryItemDO> selectPage(TmsCustomCategoryItemPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO));
    }

    default List<TmsCustomCategoryItemDO> selectListByCategoryId(Long categoryId) {
        return selectList(new LambdaQueryWrapperX<TmsCustomCategoryItemDO>()
            .eq(TmsCustomCategoryItemDO::getCustomCategoryId, categoryId));
    }

    default List<TmsCustomCategoryItemDO> selectListByCategoryId(Collection<Long> categoryIds) {
        return selectList(new LambdaQueryWrapperX<TmsCustomCategoryItemDO>()
            .inIfPresent(TmsCustomCategoryItemDO::getCustomCategoryId, categoryIds));
    }

    default void deleteByCategoryId(Long categoryId) {
        delete(new LambdaQueryWrapperX<TmsCustomCategoryItemDO>()
            .eq(TmsCustomCategoryItemDO::getCustomCategoryId, categoryId));
    }
}