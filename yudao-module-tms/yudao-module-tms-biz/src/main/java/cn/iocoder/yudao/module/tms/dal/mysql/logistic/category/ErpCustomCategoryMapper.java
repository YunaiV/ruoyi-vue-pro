package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.ErpCustomProductDO;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.ErpCustomCategoryBO;
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
public interface ErpCustomCategoryMapper extends BaseMapperX<ErpCustomCategoryDO> {

    default MPJLambdaWrapper<ErpCustomCategoryDO> buildQueryWrapper(ErpCustomCategoryPageReqVO reqVO) {
        return new MPJLambdaWrapperX<ErpCustomCategoryDO>()
            .betweenIfPresent(ErpCustomCategoryDO::getCreateTime, reqVO.getCreateTime())
            .eqIfPresent(ErpCustomCategoryDO::getMaterial, reqVO.getMaterial())
            .likeIfPresent(ErpCustomCategoryDO::getDeclaredType, reqVO.getDeclaredType())
            .likeIfPresent(ErpCustomCategoryDO::getDeclaredTypeEn, reqVO.getDeclaredTypeEn())
            .orderByDesc(ErpCustomCategoryDO::getId);
    }

    //getBOWrapper
    default MPJLambdaWrapper<ErpCustomCategoryDO> getBOWrapper(ErpCustomCategoryPageReqVO reqVO) {
        return buildQueryWrapper(reqVO)
            .selectAll(ErpCustomCategoryDO.class)
            .leftJoin(ErpCustomProductDO.class, ErpCustomProductDO::getCustomCategoryId, ErpCustomCategoryDO::getId)
            .selectCount(ErpCustomProductDO::getCustomCategoryId, ErpCustomCategoryBO::getProductCount)
            .groupBy(
                ErpCustomCategoryDO::getId,
                ErpCustomCategoryDO::getMaterial,
                ErpCustomCategoryDO::getDeclaredType,
                ErpCustomCategoryDO::getDeclaredTypeEn,
                ErpCustomCategoryDO::getTenantId,
                ErpCustomCategoryDO::getCreateTime,
                ErpCustomCategoryDO::getUpdateTime,
                ErpCustomCategoryDO::getCreator,
                ErpCustomCategoryDO::getUpdater,
                ErpCustomCategoryDO::getDeleted
            )
            .orderByDesc(ErpCustomCategoryDO::getId);
    }

    default PageResult<ErpCustomCategoryDO> selectPage(@NotNull ErpCustomCategoryPageReqVO reqVO) {
        return selectJoinPage(reqVO, ErpCustomCategoryDO.class, buildQueryWrapper(reqVO));
    }

    //getBO分页,关联（产品分类关联）的带有关联数量的DO
    default PageResult<ErpCustomCategoryBO> selectPageBO(@NotNull ErpCustomCategoryPageReqVO reqVO) {
        MPJLambdaWrapper<ErpCustomCategoryDO> wrapper = getBOWrapper(reqVO);

        return selectJoinPage(reqVO, ErpCustomCategoryBO.class, wrapper);
    }

    //getBO对象
    default ErpCustomCategoryBO getErpCustomCategoryBO(@NotNull Long id) {
        MPJLambdaWrapper<ErpCustomCategoryDO> wrapper = getBOWrapper(new ErpCustomCategoryPageReqVO())
            .eq(ErpCustomCategoryDO::getId, id);
        return selectJoinOne(ErpCustomCategoryBO.class, wrapper);
    }
    //getBO List


    //获得海关分类列表的list
    default List<ErpCustomCategoryDO> getCustomRuleCategoryList(@NotNull ErpCustomCategoryPageReqVO reqVO) {
        return selectJoinList(ErpCustomCategoryDO.class, buildQueryWrapper(reqVO));
    }

//    //获得BO List
//    default List<ErpCustomCategoryBO> getErpCustomCategoryBOListByCategoryId(@NotNull Long categoryId) {
//        MPJLambdaWrapper<ErpCustomCategoryDO> wrapper = buildQueryWrapper(new ErpCustomCategoryPageReqVO())
//            .selectAll(ErpCustomCategoryDO.class)
//            .leftJoin(ErpCustomCategoryItemDO.class, ErpCustomCategoryItemDO::getCustomCategoryId, ErpCustomCategoryDO::getId)
//            .selectCollection(ErpCustomCategoryItemDO.class, ErpCustomCategoryBO::getItems)
//            .leftJoin(ErpProductDO.class, ErpProductDO::getCustomCategoryId, ErpCustomCategoryDO::getId)
//            .selectAssociation(ErpProductDO.class, ErpCustomCategoryBO::getProductDO)
//            .eq(ErpCustomCategoryDO::getId, categoryId)
//            ;
//        return selectJoinList(ErpCustomCategoryBO.class, wrapper);
//    }
}