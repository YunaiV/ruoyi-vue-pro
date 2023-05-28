package cn.iocoder.yudao.module.promotion.api.reward;

import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 满减送活动 API 接口
 *
 * @author 芋道源码
 */
public interface RewardActivityApi {


    /**
     * 基于指定的 SPU 编号数组，获得它们匹配的满减送活动
     *
     * @param spuIds SPU 编号数组
     * @return 满减送活动列表
     */
    List<RewardActivityMatchRespDTO> getMatchRewardActivityList(Collection<Long> spuIds);

}
