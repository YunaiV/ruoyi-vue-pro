package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.TmsCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.TmsCustomProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 海关产品分类表 Mapper
 *
 * @author 王岽宇
 */
@Mapper
public interface TmsCustomProductMapper extends BaseMapperX<TmsCustomProductDO> {

    default PageResult<TmsCustomProductDO> selectPage(TmsCustomProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TmsCustomProductDO>()
            .eqIfPresent(TmsCustomProductDO::getProductId, reqVO.getProductId())
            .eqIfPresent(TmsCustomProductDO::getCustomCategoryId, reqVO.getCustomCategoryId())
            .betweenIfPresent(TmsCustomProductDO::getCreateTime, reqVO.getCreateTime())
            .betweenIfPresent(TmsCustomProductDO::getUpdateTime, reqVO.getUpdateTime())
            .orderByDesc(TmsCustomProductDO::getId));
    }

}