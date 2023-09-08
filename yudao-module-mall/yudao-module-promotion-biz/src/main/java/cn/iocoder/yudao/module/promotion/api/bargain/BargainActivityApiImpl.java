package cn.iocoder.yudao.module.promotion.api.bargain;

import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.BARGAIN_ACTIVITY_NOT_EXISTS;

/**
 * 砍价活动 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
public class BargainActivityApiImpl implements BargainActivityApi {

    @Resource
    private BargainActivityService bargainActivityService;

    @Override
    public void updateBargainActivityStock(Long activityId, Integer count) {
        // TODO @puhui999：可以整个实现到 bargainActivityService 中
        // 查询砍价活动
        BargainActivityDO activity = bargainActivityService.getBargainActivity(activityId);
        if (activity == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }

        // 更新砍价库存
        // TODO @puhui999：考虑下并发更新问题
        BargainActivityUpdateReqVO reqVO = new BargainActivityUpdateReqVO();
        reqVO.setId(activityId);
        reqVO.setStock(activity.getStock() - count);
        bargainActivityService.updateBargainActivity(reqVO);
    }

}
