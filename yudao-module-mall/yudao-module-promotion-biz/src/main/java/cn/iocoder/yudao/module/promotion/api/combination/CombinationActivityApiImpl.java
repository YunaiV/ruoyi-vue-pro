package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 拼团活动 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
public class CombinationActivityApiImpl implements CombinationActivityApi {

    @Resource
    private CombinationActivityService activityService;

    @Override
    public void validateCombination(Long activityId, Long userId, Long skuId, Integer count) {
        activityService.validateCombination(activityId, userId, skuId, count);
    }

}
