package cn.iocoder.yudao.module.promotion.api.reward;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 满减送活动 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class RewardActivityApiImpl implements RewardActivityApi {

    @Resource
    private RewardActivityService rewardActivityService;

    @Override
    public List<RewardActivityMatchRespDTO> getRewardActivityListByStatusAndNow(Integer status, LocalDateTime dateTime) {
        List<RewardActivityDO> list = rewardActivityService.getRewardActivityListByStatusAndDateTimeLt(status, dateTime);
        return BeanUtils.toBean(list, RewardActivityMatchRespDTO.class);
    }

}
