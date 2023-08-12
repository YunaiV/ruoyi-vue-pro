package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainProductDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 砍价活动 Service 接口
 *
 * @author HUIHUI
 */
public interface BargainActivityService {

    /**
     * 创建砍价活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBargainActivity(@Valid BargainActivityCreateReqVO createReqVO);

    /**
     * 更新砍价活动
     *
     * @param updateReqVO 更新信息
     */
    void updateBargainActivity(@Valid BargainActivityUpdateReqVO updateReqVO);

    /**
     * 删除砍价活动
     *
     * @param id 编号
     */
    void deleteBargainActivity(Long id);

    /**
     * 获得砍价活动
     *
     * @param id 编号
     * @return 砍价活动
     */
    BargainActivityDO getBargainActivity(Long id);

    /**
     * 获得砍价活动分页
     *
     * @param pageReqVO 分页查询
     * @return 砍价活动分页
     */
    PageResult<BargainActivityDO> getBargainActivityPage(BargainActivityPageReqVO pageReqVO);

    /**
     * 获得砍价活动商品列表
     *
     * @param activityIds 砍价活动 ids
     * @return 砍价活动的商品列表
     */
    List<BargainProductDO> getBargainProductsByActivityIds(Collection<Long> activityIds);

}
