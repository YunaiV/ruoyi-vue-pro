package cn.iocoder.yudao.module.promotion.api.point;

import cn.iocoder.yudao.module.promotion.api.point.dto.PointValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.service.point.PointActivityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 积分商城活动 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class PointActivityApiImpl implements PointActivityApi {

    @Resource
    private PointActivityService pointActivityService;

    @Override
    public PointValidateJoinRespDTO validateJoinPointActivity(Long activityId, Long skuId, Integer count) {
        return pointActivityService.validateJoinPointActivity(activityId, skuId, count);
    }

}
