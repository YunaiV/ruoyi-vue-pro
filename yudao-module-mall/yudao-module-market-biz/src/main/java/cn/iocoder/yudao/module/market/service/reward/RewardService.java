package cn.iocoder.yudao.module.market.service.reward;

import cn.iocoder.yudao.module.market.dal.dataobject.reward.RewardActivityDO;

import java.util.Map;
import java.util.Set;

/**
 * 满减送 Service 接口
 *
 * @author 芋道源码
 */
public interface RewardService {

    /**
     * 基于指定的 SPU 编号数组，获得它们匹配的满减送活动
     *
     * @param spuIds SPU 编号数组
     * @return 满减送活动，与对应的 SPU 编号的映射。即，value 就是 SPU 编号的集合
     */
    Map<RewardActivityDO, Set<Long>> getMatchRewardActivities(Set<Long> spuIds);

}
