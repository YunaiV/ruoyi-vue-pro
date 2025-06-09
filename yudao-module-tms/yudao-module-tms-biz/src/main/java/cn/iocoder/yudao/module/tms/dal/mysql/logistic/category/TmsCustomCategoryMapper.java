package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.TmsCustomProductDO;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.TmsCustomCategoryBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;


/**
 * 海关分类 Mapper
 *
 * @author 王岽宇
 */
@Mapper
public interface TmsCustomCategoryMapper extends BaseMapperX<TmsCustomCategoryDO> {

    default MPJLambdaWrapper<TmsCustomCategoryDO> buildQueryWrapper(TmsCustomCategoryPageReqVO reqVO) {
        return new MPJLambdaWrapperX<TmsCustomCategoryDO>()
            .betweenIfPresent(TmsCustomCategoryDO::getCreateTime, reqVO.getCreateTime())
            .eqIfPresent(TmsCustomCategoryDO::getMaterial, reqVO.getMaterial())
            .likeIfPresent(TmsCustomCategoryDO::getDeclaredType, reqVO.getDeclaredType())
            .likeIfPresent(TmsCustomCategoryDO::getDeclaredTypeEn, reqVO.getDeclaredTypeEn())
            .likeIfPresent(TmsCustomCategoryDO::getCustomsPurpose, reqVO.getCustomsPurpose())
            .likeIfPresent(TmsCustomCategoryDO::getCustomsMaterial, reqVO.getCustomsMaterial())
            .orderByDesc(TmsCustomCategoryDO::getId);
    }

    //getBOWrapper
    default MPJLambdaWrapper<TmsCustomCategoryDO> getBOWrapper(TmsCustomCategoryPageReqVO reqVO) {
        return buildQueryWrapper(reqVO).selectAll(TmsCustomCategoryDO.class)
            .leftJoin(TmsCustomProductDO.class, TmsCustomProductDO::getCustomCategoryId, TmsCustomCategoryDO::getId)
            .selectCount(TmsCustomProductDO::getCustomCategoryId, TmsCustomCategoryBO::getProductCount)
            .groupBy(TmsCustomCategoryDO::getId, TmsCustomCategoryDO::getMaterial, TmsCustomCategoryDO::getDeclaredType, TmsCustomCategoryDO::getDeclaredTypeEn,
                TmsCustomCategoryDO::getTenantId, TmsCustomCategoryDO::getCreateTime, TmsCustomCategoryDO::getUpdateTime, TmsCustomCategoryDO::getCreator,
                TmsCustomCategoryDO::getUpdater, TmsCustomCategoryDO::getDeleted);
    }

    default PageResult<TmsCustomCategoryDO> selectPage(@NotNull TmsCustomCategoryPageReqVO reqVO) {
        return selectJoinPage(reqVO, TmsCustomCategoryDO.class, buildQueryWrapper(reqVO));
    }

    //getBO分页,关联（产品分类关联）的带有关联数量的DO
    default PageResult<TmsCustomCategoryBO> selectPageBO(@NotNull TmsCustomCategoryPageReqVO reqVO) {
        MPJLambdaWrapper<TmsCustomCategoryDO> wrapper = getBOWrapper(reqVO);

        return selectJoinPage(reqVO, TmsCustomCategoryBO.class, wrapper);
    }

    //getBO对象
    default TmsCustomCategoryBO getTmsCustomCategoryBOById(@NotNull Long id) {
        MPJLambdaWrapper<TmsCustomCategoryDO> wrapper = getBOWrapper(new TmsCustomCategoryPageReqVO()).eq(TmsCustomCategoryDO::getId, id);
        return selectJoinOne(TmsCustomCategoryBO.class, wrapper);
    }
    //getBO List


    //获得海关分类列表的list
    default List<TmsCustomCategoryDO> getCustomRuleCategoryList(@NotNull TmsCustomCategoryPageReqVO reqVO) {
        return selectJoinList(TmsCustomCategoryDO.class, buildQueryWrapper(reqVO));
    }

    default Collection<TmsCustomCategoryDO> getCustomRuleByMaterialAndDeclaredType(Integer material, String declaredType) {
        return selectList(
            new MPJLambdaWrapper<TmsCustomCategoryDO>().eq(TmsCustomCategoryDO::getMaterial, material).eq(TmsCustomCategoryDO::getDeclaredType, declaredType));
    }

//    //获得BO List
    //    default List<TmsCustomCategoryBO> getTmsCustomCategoryBOListByCategoryId(@NotNull Long categoryId) {
//        MPJLambdaWrapper<TmsCustomCategoryDO> wrapper = buildQueryWrapper(new TmsCustomCategoryPageReqVO())
//            .selectAll(TmsCustomCategoryDO.class)
//            .leftJoin(TmsCustomCategoryItemDO.class, TmsCustomCategoryItemDO::getCustomCategoryId, TmsCustomCategoryDO::getId)
    //            .selectCollection(TmsCustomCategoryItemDO.class, TmsCustomCategoryBO::getItems)
    //            .leftJoin(TmsProductDO.class, TmsProductDO::getCustomCategoryId, TmsCustomCategoryDO::getId)
    //            .selectAssociation(TmsProductDO.class, TmsCustomCategoryBO::getProductDO)
//            .eq(TmsCustomCategoryDO::getId, categoryId)
//            ;
    //        return selectJoinList(TmsCustomCategoryBO.class, wrapper);
//    }
}