package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationActivityUpdateStockReqDTO;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 拼团活动 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class CombinationApiImpl implements CombinationApi {

    @Resource
    private CombinationActivityService activityService;

    @Override
    public void validateCombination(CombinationActivityUpdateStockReqDTO reqDTO) {
        activityService.validateCombination(reqDTO);
    }

}
