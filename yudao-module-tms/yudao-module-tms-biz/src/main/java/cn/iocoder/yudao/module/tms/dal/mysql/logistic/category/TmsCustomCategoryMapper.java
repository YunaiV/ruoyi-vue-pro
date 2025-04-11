package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

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
            .orderByDesc(TmsCustomCategoryDO::getId);
    }

    default PageResult<TmsCustomCategoryDO> selectPage(@NotNull TmsCustomCategoryPageReqVO reqVO) {
        return selectJoinPage(reqVO, TmsCustomCategoryDO.class, buildQueryWrapper(reqVO));
    }

    //获得海关分类列表的list
    default List<TmsCustomCategoryDO> getCustomRuleCategoryList(@NotNull TmsCustomCategoryPageReqVO reqVO) {
        return selectJoinList(TmsCustomCategoryDO.class, buildQueryWrapper(reqVO));
    }

//    //获得BO List
//    default List<ErpCustomCategoryBO> getErpCustomCategoryBOListByCategoryId(@NotNull Long categoryId) {
//        MPJLambdaWrapper<TmsCustomCategoryDO> wrapper = buildQueryWrapper(new TmsCustomCategoryPageReqVO())
//            .selectAll(TmsCustomCategoryDO.class)
//            .leftJoin(TmsCustomCategoryItemDO.class, TmsCustomCategoryItemDO::getCustomCategoryId, TmsCustomCategoryDO::getId)
//            .selectCollection(TmsCustomCategoryItemDO.class, ErpCustomCategoryBO::getItems)
//            .leftJoin(ErpProductDO.class, ErpProductDO::getCustomCategoryId, TmsCustomCategoryDO::getId)
//            .selectAssociation(ErpProductDO.class, ErpCustomCategoryBO::getProductDO)
//            .eq(TmsCustomCategoryDO::getId, categoryId)
//            ;
//        return selectJoinList(ErpCustomCategoryBO.class, wrapper);
//    }
}