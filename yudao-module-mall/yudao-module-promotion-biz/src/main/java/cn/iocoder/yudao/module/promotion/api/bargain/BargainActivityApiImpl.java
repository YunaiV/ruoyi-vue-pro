package cn.iocoder.yudao.module.promotion.api.bargain;

import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    public void updateBargainActivityStock(Long id, Integer count) {
        bargainActivityService.updateBargainActivityStock(id, count);
    }

}
