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
     * 获得 spuId 商品匹配的的满减送活动列表
     *
     * @param spuIds   SPU 编号
     * @return 满减送活动列表
     */
    List<RewardActivityMatchRespDTO> getMatchRewardActivityListBySpuIds(Collection<Long> spuIds);

}
