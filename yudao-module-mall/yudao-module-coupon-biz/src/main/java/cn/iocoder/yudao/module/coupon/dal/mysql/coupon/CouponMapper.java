package cn.iocoder.yudao.module.coupon.dal.mysql.coupon;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.coupon.dal.dataobject.coupon.CouponDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo.*;

/**
 * 优惠券 Mapper
 *
 * @author wxr
 */
@Mapper
public interface CouponMapper extends BaseMapperX<CouponDO> {

    default PageResult<CouponDO> selectPage(CouponPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CouponDO>()
                .eqIfPresent(CouponDO::getType, reqVO.getType())
                .likeIfPresent(CouponDO::getName, reqVO.getName())
                .eqIfPresent(CouponDO::getCouponTypeId, reqVO.getCouponTypeId())
                .eqIfPresent(CouponDO::getCouponCode, reqVO.getCouponCode())
                .eqIfPresent(CouponDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(CouponDO::getUseOrderId, reqVO.getUseOrderId())
                .eqIfPresent(CouponDO::getGoodsType, reqVO.getGoodsType())
                .eqIfPresent(CouponDO::getGoodsIds, reqVO.getGoodsIds())
                .eqIfPresent(CouponDO::getAtLeast, reqVO.getAtLeast())
                .eqIfPresent(CouponDO::getMoney, reqVO.getMoney())
                .eqIfPresent(CouponDO::getDiscount, reqVO.getDiscount())
                .eqIfPresent(CouponDO::getDiscountLimit, reqVO.getDiscountLimit())
                .eqIfPresent(CouponDO::getWhetherForbidPreference, reqVO.getWhetherForbidPreference())
                .eqIfPresent(CouponDO::getWhetherExpireNotice, reqVO.getWhetherExpireNotice())
                .eqIfPresent(CouponDO::getExpireNoticeFixedTerm, reqVO.getExpireNoticeFixedTerm())
                .eqIfPresent(CouponDO::getWhetherNoticed, reqVO.getWhetherNoticed())
                .eqIfPresent(CouponDO::getState, reqVO.getState())
                .eqIfPresent(CouponDO::getGetType, reqVO.getGetType())
                .betweenIfPresent(CouponDO::getFetchTime, reqVO.getFetchTime())
                .betweenIfPresent(CouponDO::getUseTime, reqVO.getUseTime())
                .betweenIfPresent(CouponDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(CouponDO::getEndTime, reqVO.getEndTime())
                .betweenIfPresent(CouponDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CouponDO::getId));
    }

    default List<CouponDO> selectList(CouponExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CouponDO>()
                .eqIfPresent(CouponDO::getType, reqVO.getType())
                .likeIfPresent(CouponDO::getName, reqVO.getName())
                .eqIfPresent(CouponDO::getCouponTypeId, reqVO.getCouponTypeId())
                .eqIfPresent(CouponDO::getCouponCode, reqVO.getCouponCode())
                .eqIfPresent(CouponDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(CouponDO::getUseOrderId, reqVO.getUseOrderId())
                .eqIfPresent(CouponDO::getGoodsType, reqVO.getGoodsType())
                .eqIfPresent(CouponDO::getGoodsIds, reqVO.getGoodsIds())
                .eqIfPresent(CouponDO::getAtLeast, reqVO.getAtLeast())
                .eqIfPresent(CouponDO::getMoney, reqVO.getMoney())
                .eqIfPresent(CouponDO::getDiscount, reqVO.getDiscount())
                .eqIfPresent(CouponDO::getDiscountLimit, reqVO.getDiscountLimit())
                .eqIfPresent(CouponDO::getWhetherForbidPreference, reqVO.getWhetherForbidPreference())
                .eqIfPresent(CouponDO::getWhetherExpireNotice, reqVO.getWhetherExpireNotice())
                .eqIfPresent(CouponDO::getExpireNoticeFixedTerm, reqVO.getExpireNoticeFixedTerm())
                .eqIfPresent(CouponDO::getWhetherNoticed, reqVO.getWhetherNoticed())
                .eqIfPresent(CouponDO::getState, reqVO.getState())
                .eqIfPresent(CouponDO::getGetType, reqVO.getGetType())
                .betweenIfPresent(CouponDO::getFetchTime, reqVO.getFetchTime())
                .betweenIfPresent(CouponDO::getUseTime, reqVO.getUseTime())
                .betweenIfPresent(CouponDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(CouponDO::getEndTime, reqVO.getEndTime())
                .betweenIfPresent(CouponDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CouponDO::getId));
    }

}
