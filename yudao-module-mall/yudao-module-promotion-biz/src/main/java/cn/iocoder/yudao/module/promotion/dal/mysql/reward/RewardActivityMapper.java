package cn.iocoder.yudao.module.promotion.dal.mysql.reward;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 满减送活动 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface RewardActivityMapper extends BaseMapperX<RewardActivityDO> {

    default PageResult<RewardActivityDO> selectPage(RewardActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RewardActivityDO>()
                .likeIfPresent(RewardActivityDO::getName, reqVO.getName())
                .eqIfPresent(RewardActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(RewardActivityDO::getId));
    }

    default List<RewardActivityDO> selectListBySpuIdAndStatusAndNow(Collection<Long> spuIds,
                                                                    Collection<Long> categoryIds,
                                                                    Integer status) {
        LocalDateTime now = LocalDateTime.now();
        Function<Collection<Long>, String> productScopeValuesFindInSetFunc = ids -> ids.stream()
                .map(id -> StrUtil.format("FIND_IN_SET({}, product_scope_values) ", id))
                .collect(Collectors.joining(" OR "));
        return selectList(new LambdaQueryWrapperX<RewardActivityDO>()
                .eq(RewardActivityDO::getStatus, status)
                .lt(RewardActivityDO::getStartTime, now)
                .gt(RewardActivityDO::getEndTime, now)
                .and(i -> i.eq(RewardActivityDO::getProductScope, PromotionProductScopeEnum.SPU.getScope())
                            .and(i1 -> i1.apply(productScopeValuesFindInSetFunc.apply(spuIds)))
                        .or(i1 -> i1.eq(RewardActivityDO::getProductScope, PromotionProductScopeEnum.ALL.getScope()))
                        .or(i1 -> i1.eq(RewardActivityDO::getProductScope, PromotionProductScopeEnum.CATEGORY.getScope())
                                .and(i2 -> i2.apply(productScopeValuesFindInSetFunc.apply(categoryIds)))))
                .orderByDesc(RewardActivityDO::getId)
        );
    }

}
