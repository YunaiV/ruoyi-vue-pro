package cn.iocoder.yudao.module.coupon.dal.mysql.CouponTemplete;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteExportReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempletePageReqVO;
import cn.iocoder.yudao.module.coupon.dal.dataobject.CouponTemplete.CouponTempleteDO;
import org.apache.ibatis.annotations.Mapper;


/**
 * 优惠券模板 Mapper
 *
 * @author wxr
 */
@Mapper
public interface CouponTempleteMapper extends BaseMapperX<CouponTempleteDO> {

    default PageResult<CouponTempleteDO> selectPage(CouponTempletePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CouponTempleteDO>()
                .eqIfPresent(CouponTempleteDO::getType, reqVO.getType())
                .likeIfPresent(CouponTempleteDO::getName, reqVO.getName())
                .eqIfPresent(CouponTempleteDO::getCouponNameRemark, reqVO.getCouponNameRemark())
                .eqIfPresent(CouponTempleteDO::getImage, reqVO.getImage())
                .eqIfPresent(CouponTempleteDO::getCount, reqVO.getCount())
                .eqIfPresent(CouponTempleteDO::getLeadCount, reqVO.getLeadCount())
                .eqIfPresent(CouponTempleteDO::getUsedCount, reqVO.getUsedCount())
                .eqIfPresent(CouponTempleteDO::getGoodsType, reqVO.getGoodsType())
                .eqIfPresent(CouponTempleteDO::getProductIds, reqVO.getProductIds())
                .eqIfPresent(CouponTempleteDO::getHasUseLimit, reqVO.getHasUseLimit())
                .eqIfPresent(CouponTempleteDO::getAtLeast, reqVO.getAtLeast())
                .eqIfPresent(CouponTempleteDO::getMoney, reqVO.getMoney())
                .eqIfPresent(CouponTempleteDO::getDiscount, reqVO.getDiscount())
                .eqIfPresent(CouponTempleteDO::getDiscountLimit, reqVO.getDiscountLimit())
                .eqIfPresent(CouponTempleteDO::getMinMoney, reqVO.getMinMoney())
                .eqIfPresent(CouponTempleteDO::getMaxMoney, reqVO.getMaxMoney())
                .eqIfPresent(CouponTempleteDO::getValidityType, reqVO.getValidityType())
                .betweenIfPresent(CouponTempleteDO::getStartUseTime, reqVO.getStartUseTime())
                .betweenIfPresent(CouponTempleteDO::getEndUseTime, reqVO.getEndUseTime())
                .eqIfPresent(CouponTempleteDO::getFixedTerm, reqVO.getFixedTerm())
                .eqIfPresent(CouponTempleteDO::getWhetherLimitless, reqVO.getWhetherLimitless())
                .eqIfPresent(CouponTempleteDO::getMaxFetch, reqVO.getMaxFetch())
                .eqIfPresent(CouponTempleteDO::getWhetherExpireNotice, reqVO.getWhetherExpireNotice())
                .eqIfPresent(CouponTempleteDO::getExpireNoticeFixedTerm, reqVO.getExpireNoticeFixedTerm())
                .eqIfPresent(CouponTempleteDO::getWhetherForbidPreference, reqVO.getWhetherForbidPreference())
                .eqIfPresent(CouponTempleteDO::getWhetherShow, reqVO.getWhetherShow())
                .eqIfPresent(CouponTempleteDO::getDiscountOrderMoney, reqVO.getDiscountOrderMoney())
                .eqIfPresent(CouponTempleteDO::getOrderMoney, reqVO.getOrderMoney())
                .eqIfPresent(CouponTempleteDO::getWhetherForbidden, reqVO.getWhetherForbidden())
                .eqIfPresent(CouponTempleteDO::getOrderGoodsNum, reqVO.getOrderGoodsNum())
                .eqIfPresent(CouponTempleteDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CouponTempleteDO::getEndTime, reqVO.getEndTime())
                .betweenIfPresent(CouponTempleteDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CouponTempleteDO::getId));
    }

    default List<CouponTempleteDO> selectList(CouponTempleteExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CouponTempleteDO>()
                .eqIfPresent(CouponTempleteDO::getType, reqVO.getType())
                .likeIfPresent(CouponTempleteDO::getName, reqVO.getName())
                .eqIfPresent(CouponTempleteDO::getCouponNameRemark, reqVO.getCouponNameRemark())
                .eqIfPresent(CouponTempleteDO::getImage, reqVO.getImage())
                .eqIfPresent(CouponTempleteDO::getCount, reqVO.getCount())
                .eqIfPresent(CouponTempleteDO::getLeadCount, reqVO.getLeadCount())
                .eqIfPresent(CouponTempleteDO::getUsedCount, reqVO.getUsedCount())
                .eqIfPresent(CouponTempleteDO::getGoodsType, reqVO.getGoodsType())
                .eqIfPresent(CouponTempleteDO::getProductIds, reqVO.getProductIds())
                .eqIfPresent(CouponTempleteDO::getHasUseLimit, reqVO.getHasUseLimit())
                .eqIfPresent(CouponTempleteDO::getAtLeast, reqVO.getAtLeast())
                .eqIfPresent(CouponTempleteDO::getMoney, reqVO.getMoney())
                .eqIfPresent(CouponTempleteDO::getDiscount, reqVO.getDiscount())
                .eqIfPresent(CouponTempleteDO::getDiscountLimit, reqVO.getDiscountLimit())
                .eqIfPresent(CouponTempleteDO::getMinMoney, reqVO.getMinMoney())
                .eqIfPresent(CouponTempleteDO::getMaxMoney, reqVO.getMaxMoney())
                .eqIfPresent(CouponTempleteDO::getValidityType, reqVO.getValidityType())
                .betweenIfPresent(CouponTempleteDO::getStartUseTime, reqVO.getStartUseTime())
                .betweenIfPresent(CouponTempleteDO::getEndUseTime, reqVO.getEndUseTime())
                .eqIfPresent(CouponTempleteDO::getFixedTerm, reqVO.getFixedTerm())
                .eqIfPresent(CouponTempleteDO::getWhetherLimitless, reqVO.getWhetherLimitless())
                .eqIfPresent(CouponTempleteDO::getMaxFetch, reqVO.getMaxFetch())
                .eqIfPresent(CouponTempleteDO::getWhetherExpireNotice, reqVO.getWhetherExpireNotice())
                .eqIfPresent(CouponTempleteDO::getExpireNoticeFixedTerm, reqVO.getExpireNoticeFixedTerm())
                .eqIfPresent(CouponTempleteDO::getWhetherForbidPreference, reqVO.getWhetherForbidPreference())
                .eqIfPresent(CouponTempleteDO::getWhetherShow, reqVO.getWhetherShow())
                .eqIfPresent(CouponTempleteDO::getDiscountOrderMoney, reqVO.getDiscountOrderMoney())
                .eqIfPresent(CouponTempleteDO::getOrderMoney, reqVO.getOrderMoney())
                .eqIfPresent(CouponTempleteDO::getWhetherForbidden, reqVO.getWhetherForbidden())
                .eqIfPresent(CouponTempleteDO::getOrderGoodsNum, reqVO.getOrderGoodsNum())
                .eqIfPresent(CouponTempleteDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CouponTempleteDO::getEndTime, reqVO.getEndTime())
                .betweenIfPresent(CouponTempleteDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CouponTempleteDO::getId));
    }

}
