package cn.iocoder.yudao.module.promotion.api.reward;

import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 满减送活动 API 接口
 *
 * @author 芋道源码
 */
public interface RewardActivityApi {

    /**
     * 获取指定 spu 编号最近参加的活动，每个 spuId 只返回一条记录
     *
     * @param spuIds   spu 编号
     * @param status   状态
     * @param dateTime 当前日期时间
     * @return 满减送活动列表
     */
    List<RewardActivityMatchRespDTO> getRewardActivityBySpuIdsAndStatusAndDateTimeLt(Collection<Long> spuIds, Integer status, LocalDateTime dateTime);

}
