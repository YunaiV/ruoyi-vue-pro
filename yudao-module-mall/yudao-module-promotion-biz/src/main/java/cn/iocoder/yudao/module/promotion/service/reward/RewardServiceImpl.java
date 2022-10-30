package cn.iocoder.yudao.module.promotion.service.reward;

import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 满减送 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class RewardServiceImpl implements RewardService {

    // TODO 芋艿：待实现
    @Override
    public Map<RewardActivityDO, Set<Long>> getMatchRewardActivities(Set<Long> spuIds) {
        return Collections.emptyMap();
    }

}
