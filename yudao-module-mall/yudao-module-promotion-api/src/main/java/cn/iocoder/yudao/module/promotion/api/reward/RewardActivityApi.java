package cn.iocoder.yudao.module.promotion.api.reward;

import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 满减送活动 API 接口
 *
 * @author 芋道源码
 */
public interface RewardActivityApi {

    /**
     * 获得当前时间内开启的满减送活动
     *
     * @param status   状态
     * @param dateTime 当前时间，即筛选 <= dateTime 的满减送活动
     * @return 满减送活动列表
     */
    List<RewardActivityMatchRespDTO> getRewardActivityListByStatusAndNow(Integer status, LocalDateTime dateTime);

}
