package cn.iocoder.yudao.module.promotion.dal.mysql.coupon;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.CouponPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * 优惠劵 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CouponMapper extends BaseMapperX<CouponDO> {

    default PageResult<CouponDO> selectPage(CouponPageReqVO reqVO, Collection<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CouponDO>()
                .eqIfPresent(CouponDO::getTemplateId, reqVO.getTemplateId())
                .inIfPresent(CouponDO::getUserId, userIds)
                .betweenIfPresent(CouponDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CouponDO::getId));
    }

}
