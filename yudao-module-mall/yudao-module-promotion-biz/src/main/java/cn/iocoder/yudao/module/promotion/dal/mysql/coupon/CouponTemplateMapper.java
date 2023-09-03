package cn.iocoder.yudao.module.promotion.dal.mysql.coupon;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.CouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 优惠劵模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CouponTemplateMapper extends BaseMapperX<CouponTemplateDO> {

    default PageResult<CouponTemplateDO> selectPage(CouponTemplatePageReqVO reqVO) {
        // 构建可领取的查询条件, 好啰嗦  ( ╯-_-)╯┴—┴
        Consumer<LambdaQueryWrapper<CouponTemplateDO>> canTakeConsumer = null;
        if (CollUtil.isNotEmpty(reqVO.getCanTakeTypes())) {
            canTakeConsumer = w ->
                    w.eq(CouponTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus()) // 1. 状态为可用的
                            .in(CouponTemplateDO::getTakeType, reqVO.getCanTakeTypes()) // 2. 领取方式一致
                            .and(ww -> ww.isNull(CouponTemplateDO::getValidEndTime)  // 3. 未过期
                                    .or().gt(CouponTemplateDO::getValidEndTime, LocalDateTime.now()))
                            .apply(" take_count < total_count "); // 4. 剩余数量大于 0
        }
        // 执行分页查询
        return selectPage(reqVO, new LambdaQueryWrapperX<CouponTemplateDO>()
                .likeIfPresent(CouponTemplateDO::getName, reqVO.getName())
                .eqIfPresent(CouponTemplateDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CouponTemplateDO::getDiscountType, reqVO.getDiscountType())
                .betweenIfPresent(CouponTemplateDO::getCreateTime, reqVO.getCreateTime())
                .and(canTakeConsumer != null, canTakeConsumer)
                .orderByDesc(CouponTemplateDO::getId));
    }

    void updateTakeCount(@Param("id") Long id, @Param("incrCount") Integer incrCount);

}
