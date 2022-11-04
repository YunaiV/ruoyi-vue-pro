package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.UserRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon.CouponPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.coupon.CouponMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 优惠劵 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CouponServiceImpl implements CouponService {

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private CouponMapper couponMapper;

    // TODO 芋艿：待实现
    @Override
    public CouponDO validCoupon(Long id, Long userId) {
        return null;
    }

    @Override
    public PageResult<CouponDO> getCouponPage(CouponPageReqVO pageReqVO) {
        // 获得用户编号
        Set<Long> userIds = null;
        if (StrUtil.isNotEmpty(pageReqVO.getNickname())) {
            userIds = CollectionUtils.convertSet(memberUserApi.getUserListByNickname(pageReqVO.getNickname()),
                    UserRespDTO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return PageResult.empty();
            }
        }
        // 分页查询
        return couponMapper.selectPage(pageReqVO, userIds);
    }

}
