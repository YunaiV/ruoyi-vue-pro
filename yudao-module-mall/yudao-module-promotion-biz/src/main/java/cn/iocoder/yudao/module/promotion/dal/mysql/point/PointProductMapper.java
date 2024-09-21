package cn.iocoder.yudao.module.promotion.dal.mysql.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.product.PointProductPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.pointproduct.PointProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分商城商品 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface PointProductMapper extends BaseMapperX<PointProductDO> {

    default PageResult<PointProductDO> selectPage(PointProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PointProductDO>()
                .eqIfPresent(PointProductDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(PointProductDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(PointProductDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(PointProductDO::getMaxCount, reqVO.getMaxCount())
                .eqIfPresent(PointProductDO::getPoint, reqVO.getPoint())
                .eqIfPresent(PointProductDO::getPrice, reqVO.getPrice())
                .eqIfPresent(PointProductDO::getType, reqVO.getType())
                .eqIfPresent(PointProductDO::getActivityStatus, reqVO.getActivityStatus())
                .betweenIfPresent(PointProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PointProductDO::getId));
    }

}