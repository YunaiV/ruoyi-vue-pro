package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.ErpCustomProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 海关产品分类表 Mapper
 *
 * @author 王岽宇
 */
@Mapper
public interface ErpCustomProductMapper extends BaseMapperX<ErpCustomProductDO> {

    default PageResult<ErpCustomProductDO> selectPage(ErpCustomProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpCustomProductDO>()
            .eqIfPresent(ErpCustomProductDO::getProductId, reqVO.getProductId())
            .eqIfPresent(ErpCustomProductDO::getCustomCategoryId, reqVO.getCustomCategoryId())
            .betweenIfPresent(ErpCustomProductDO::getCreateTime, reqVO.getCreateTime())
            .betweenIfPresent(ErpCustomProductDO::getUpdateTime, reqVO.getUpdateTime())
            .orderByDesc(ErpCustomProductDO::getId));
    }

}