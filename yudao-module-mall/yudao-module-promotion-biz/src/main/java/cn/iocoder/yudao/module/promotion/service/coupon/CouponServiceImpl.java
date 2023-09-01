package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon.CouponPageReqVO;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.coupon.CouponMapper;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTakeTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTemplateValidityTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public Long getUnusedCouponCount(Long userId) {
        return couponMapper.selectCountByUserIdAndStatus(userId, CouponStatusEnum.UNUSED.getStatus());
    }

    @Override
    public Boolean takeCoupon(Long templateId, Set<Long> userIds, CouponTakeTypeEnum takeType) {
        // 1. 校验并过滤用户
        CouponTemplateDO template = couponTemplateService.getCouponTemplate(templateId);
        userIds = validateAndFilterTakeUserId(template, userIds, takeType);

        // 2. 批量保存优惠劵
        // TODO @疯狂：这里可以使用 CollectionUtils.convertList 更简洁；stream 可以简化很多代码，常用的 stream 操作，使用 util 可以进一步简洁，同时提升可读性
        List<CouponDO> couponList = userIds.stream()
                .map(userId -> CouponConvert.INSTANCE.convert(template, userId))
                .collect(Collectors.toList());
        couponMapper.insertBatch(couponList);

        // 3. 增加优惠劵模板的领取数量
        couponTemplateService.updateCouponTemplateTakeCount(templateId, userIds.size());
        return true;
    }

    /**
     * 校验优惠券模板, 并过滤不可以领取的用户
     *
     * @param couponTemplate 优惠券模板
     * @param userIds        领取人列表
     * @param takeType       领取方式
     * @return 可领取此券的用户列表
     */
    // TODO @疯狂：我建议哈，校验模版，和过滤用户分成两个方法；混在一起，有点小重，后续单测可能也比较难写哈；
    private Set<Long> validateAndFilterTakeUserId(CouponTemplateDO couponTemplate, Set<Long> userIds, CouponTakeTypeEnum takeType) {
        // 1.1 校验模板
        if (couponTemplate == null) {
            throw exception(COUPON_TEMPLATE_NOT_EXISTS);
        }
        // 1.2 校验剩余数量
        if (couponTemplate.getTakeCount() + userIds.size() > couponTemplate.getTotalCount()) {
            throw exception(COUPON_TEMPLATE_NOT_ENOUGH);
        }
        // 1.3 校验"固定日期"的有效期类型是否过期
        if (CouponTemplateValidityTypeEnum.DATE.getType().equals(couponTemplate.getValidityType())) {
            if (LocalDateTimeUtils.beforeNow(couponTemplate.getValidEndTime())) {
                throw exception(COUPON_TEMPLATE_EXPIRED);
            }
        }
        // 1.4 校验领取方式
        // TODO @疯狂：如果要做这样的判断，使用 !ObjectUtils.equalsAny() 会更简洁
        if (!CouponTakeTypeEnum.COMMON.getValue().equals(couponTemplate.getTakeType())) {
            if (ObjectUtil.notEqual(couponTemplate.getTakeType(), takeType.getValue())) {
                throw exception(COUPON_TEMPLATE_CANNOT_TAKE);
            }
        }

        // 2.1 过滤掉，已经领取到上限的用户
        List<CouponDO> alreadyTakeCoupons = couponMapper.selectByTemplateIdAndUserId(couponTemplate.getId(), userIds);
        // 校验新人券
        // TODO @疯狂：我在想，这个判断，是不是和下面的 couponTemplate.getTakeLimitCount() > 0 冗余了；可以先都过滤，然后最终去判断 userIds 是不是空了；
        if (CouponTakeTypeEnum.BY_REGISTER.equals(takeType)) {
            if (!alreadyTakeCoupons.isEmpty()) {
                throw exception(COUPON_TEMPLATE_USER_ALREADY_TAKE);
            }
        }
        // 校验领取数量限制
        if (couponTemplate.getTakeLimitCount() > 0) {
            Map<Long, Integer> userTakeCountMap = CollStreamUtil.groupBy(alreadyTakeCoupons, CouponDO::getUserId, Collectors.summingInt(c -> 1));
            userIds.removeIf(userId -> MapUtil.getInt(userTakeCountMap, userId, 0) >= couponTemplate.getTakeLimitCount());
            // 2.2 如果所有用户都领取过，则抛出异常
            if (userIds.isEmpty()) {
                throw exception(COUPON_TEMPLATE_USER_ALREADY_TAKE);
            }
        }
        return userIds;
    }

}
