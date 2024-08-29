package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon.CouponPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponMatchReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTakeTypeEnum;

import java.util.*;

/**
 * 优惠劵 Service 接口
 *
 * @author 芋道源码
 */
public interface CouponService {

    /**
     * 校验优惠劵，包括状态、有限期
     * <p>
     * 1. 如果校验通过，则返回优惠劵信息
     * 2. 如果校验不通过，则直接抛出业务异常
     *
     * @param id     优惠劵编号
     * @param userId 用户编号
     * @return 优惠劵信息
     */
    CouponDO validCoupon(Long id, Long userId);

    /**
     * 校验优惠劵，包括状态、有限期
     *
     * @param coupon 优惠劵
     * @see #validCoupon(Long, Long) 逻辑相同，只是入参不同
     */
    void validCoupon(CouponDO coupon);

    /**
     * 获得优惠劵分页
     *
     * @param pageReqVO 分页查询
     * @return 优惠劵分页
     */
    PageResult<CouponDO> getCouponPage(CouponPageReqVO pageReqVO);

    /**
     * 使用优惠劵
     *
     * @param id      优惠劵编号
     * @param userId  用户编号
     * @param orderId 订单编号
     */
    void useCoupon(Long id, Long userId, Long orderId);

    /**
     * 退还已使用的优惠券
     *
     * @param id 优惠券编号
     */
    void returnUsedCoupon(Long id);

    /**
     * 回收优惠劵
     *
     * @param id 优惠劵编号
     */
    void deleteCoupon(Long id);

    /**
     * 获得用户的优惠劵列表
     *
     * @param userId 用户编号
     * @param status 优惠劵状态
     * @return 优惠劵列表
     */
    List<CouponDO> getCouponList(Long userId, Integer status);

    /**
     * 获得未使用的优惠劵数量
     *
     * @param userId 用户编号
     * @return 未使用的优惠劵数量
     */
    Long getUnusedCouponCount(Long userId);

    /**
     * 领取优惠券
     *
     * @param templateId 优惠券模板编号
     * @param userIds    用户编号列表
     * @param takeType   领取方式
     */
    void takeCoupon(Long templateId, Set<Long> userIds, CouponTakeTypeEnum takeType);

    /**
     * 【管理员】给用户发送优惠券
     *
     * @param templateId 优惠券模板编号
     * @param userIds    用户编号列表
     */
    default void takeCouponByAdmin(Long templateId, Set<Long> userIds) {
        takeCoupon(templateId, userIds, CouponTakeTypeEnum.ADMIN);
    }

    /**
     * 【管理员】给指定用户批量发送优惠券
     *
     * @param templateIds 优惠劵编号的数组
     * @param counts      优惠券数量的数组
     * @param userId      用户编号
     */
    void takeCouponsByAdmin(List<Long> templateIds, List<Integer> counts, Long userId);

    /**
     * 【会员】领取优惠券
     *
     * @param templateId 优惠券模板编号
     * @param userId     用户编号
     */
    default void takeCouponByUser(Long templateId, Long userId) {
        takeCoupon(templateId, CollUtil.newHashSet(userId), CouponTakeTypeEnum.USER);
    }

    /**
     * 【系统】给用户发送新人券
     *
     * @param userId 用户编号
     */
    void takeCouponByRegister(Long userId);

    /**
     * 获取会员领取指定优惠券的数量
     *
     * @param templateId 优惠券模板编号
     * @param userId     用户编号
     * @return 领取优惠券的数量
     */
    default Integer getTakeCount(Long templateId, Long userId) {
        Map<Long, Integer> map = getTakeCountMapByTemplateIds(Collections.singleton(templateId), userId);
        return MapUtil.getInt(map, templateId, 0);
    }

    /**
     * 统计会员领取优惠券的数量
     *
     * @param templateIds 优惠券模板编号列表
     * @param userId      用户编号
     * @return 领取优惠券的数量
     */
    Map<Long, Integer> getTakeCountMapByTemplateIds(Collection<Long> templateIds, Long userId);

    /**
     * 获取用户匹配的优惠券列表
     *
     * @param userId     用户编号
     * @param matchReqVO 匹配参数
     * @return 优惠券列表
     */
    List<CouponDO> getMatchCouponList(Long userId, AppCouponMatchReqVO matchReqVO);

    /**
     * 过期优惠券
     *
     * @return 过期数量
     */
    int expireCoupon();

    /**
     * 获取用户是否可以领取优惠券
     *
     * @param userId    用户编号
     * @param templates 优惠券列表
     * @return 是否可以领取
     */
    Map<Long, Boolean> getUserCanCanTakeMap(Long userId, List<CouponTemplateDO> templates);

    /**
     * 获得优惠劵
     *
     * @param userId 用户编号
     * @param id     编号
     * @return 优惠劵
     */
    CouponDO getCoupon(Long userId, Long id);

}
