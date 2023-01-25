package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon.CouponPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.coupon.CouponMapper;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;

/**
 * 优惠劵 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CouponServiceImpl implements CouponService {

    @Resource
    private CouponTemplateService couponTemplateService;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private MemberUserApi memberUserApi;

    @Override
    public CouponDO validCoupon(Long id, Long userId) {
        CouponDO coupon = couponMapper.selectByIdAndUserId(id, userId);
        if (coupon == null) {
            throw exception(COUPON_NOT_EXISTS);
        }
        validCoupon(coupon);
        return coupon;
    }

    @Override
    public void validCoupon(CouponDO coupon) {
        // 校验状态
        if (ObjectUtil.notEqual(coupon.getStatus(), CouponStatusEnum.UNUSED.getStatus())) {
            throw exception(COUPON_STATUS_NOT_UNUSED);
        }
        // 校验有效期；为避免定时器没跑，实际优惠劵已经过期
        if (LocalDateTimeUtils.isBetween(coupon.getValidStartTime(), coupon.getValidEndTime())) {
            throw exception(COUPON_VALID_TIME_NOT_NOW);
        }
    }

    @Override
    public PageResult<CouponDO> getCouponPage(CouponPageReqVO pageReqVO) {
        // 获得用户编号
        Set<Long> userIds = null;
        if (StrUtil.isNotEmpty(pageReqVO.getNickname())) {
            userIds = CollectionUtils.convertSet(memberUserApi.getUserListByNickname(pageReqVO.getNickname()),
                    MemberUserRespDTO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return PageResult.empty();
            }
        }
        // 分页查询
        return couponMapper.selectPage(pageReqVO, userIds);
    }

    @Override
    public void useCoupon(Long id, Long userId, Long orderId) {
        // 校验优惠劵
        validCoupon(id, userId);
        // 更新状态
        int updateCount = couponMapper.updateByIdAndStatus(id, CouponStatusEnum.UNUSED.getStatus(),
                new CouponDO().setStatus(CouponStatusEnum.USED.getStatus())
                        .setUseOrderId(orderId).setUseTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(COUPON_STATUS_NOT_UNUSED);
        }
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        // 校验存在
        validateCouponExists(id);

        // 更新优惠劵
        int deleteCount = couponMapper.delete(id,
                asList(CouponStatusEnum.UNUSED.getStatus(), CouponStatusEnum.EXPIRE.getStatus()));
        if (deleteCount == 0) {
            throw exception(COUPON_DELETE_FAIL_USED);
        }
        // 减少优惠劵模板的领取数量 -1
        couponTemplateService.updateCouponTemplateTakeCount(id, -1);
    }

    @Override
    public List<CouponDO> getCouponList(Long userId, Integer status) {
        return couponMapper.selectListByUserIdAndStatus(userId, status);
    }

    private void validateCouponExists(Long id) {
        if (couponMapper.selectById(id) == null) {
            throw exception(COUPON_NOT_EXISTS);
        }
    }

}
