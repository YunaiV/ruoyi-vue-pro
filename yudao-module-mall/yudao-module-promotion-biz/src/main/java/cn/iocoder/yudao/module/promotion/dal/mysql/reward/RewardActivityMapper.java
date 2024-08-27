package cn.iocoder.yudao.module.promotion.dal.mysql.reward;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    default List<RewardActivityDO> selectListByProductScopeAndStatus(Integer productScope, Integer status) {
        return selectList(new LambdaQueryWrapperX<RewardActivityDO>()
                .eq(RewardActivityDO::getProductScope, productScope)
                .eq(RewardActivityDO::getStatus, status));
    }

    default List<RewardActivityDO> selectListBySpuIdsAndStatus(Collection<Long> spuIds, Integer status) {
        Function<Collection<Long>, String> productScopeValuesFindInSetFunc = ids -> ids.stream()
                .map(id -> StrUtil.format("FIND_IN_SET({}, product_spu_ids) ", id))
                .collect(Collectors.joining(" OR "));
        return selectList(new QueryWrapper<RewardActivityDO>()
                .eq("status", status)
                .apply(productScopeValuesFindInSetFunc.apply(spuIds)));
    }

    /**
     * 获取指定活动编号的活动列表且
     * 开始时间和结束时间小于给定时间 dateTime 的活动列表
     *
     * @param status   状态
     * @param dateTime 指定日期
     * @return 活动列表
     */
    default List<RewardActivityDO> selectListByStatusAndDateTimeLt(Integer status, LocalDateTime dateTime) {
        return selectList(new LambdaQueryWrapperX<RewardActivityDO>()
                .eq(RewardActivityDO::getStatus, status)
                .lt(RewardActivityDO::getStartTime, dateTime)
                .gt(RewardActivityDO::getEndTime, dateTime)// 开始时间 < 指定时间 < 结束时间，也就是说获取指定时间段的活动
                .orderByAsc(RewardActivityDO::getStartTime)
        );
    }

}
