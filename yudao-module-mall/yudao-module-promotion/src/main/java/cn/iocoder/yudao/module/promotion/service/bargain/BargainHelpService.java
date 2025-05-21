package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.help.BargainHelpPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.help.AppBargainHelpCreateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainHelpDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 砍价助力 Service 接口
 *
 * @author 芋道源码
 */
public interface BargainHelpService {

    /**
     * 创建砍价助力（帮人砍价）
     *
     * @param userId 用户编号
     * @param reqVO 请求信息
     * @return 砍价助力记录
     */
    BargainHelpDO createBargainHelp(Long userId, AppBargainHelpCreateReqVO reqVO);

    /**
     * 【砍价活动】获得助力人数 Map
     *
     * @param activityIds 活动编号
     * @return 助力人数 Map
     */
    Map<Long, Integer> getBargainHelpUserCountMapByActivity(Collection<Long> activityIds);

    /**
     * 【砍价记录】获得助力人数 Map
     *
     * @param recordIds 记录编号
     * @return 助力人数 Map
     */
    Map<Long, Integer> getBargainHelpUserCountMapByRecord(Collection<Long> recordIds);

    /**
     * 【砍价活动】获得用户的助力次数
     *
     * @param activityId 活动编号
     * @param userId 用户编号
     * @return 助力次数
     */
    Long getBargainHelpCountByActivity(Long activityId, Long userId);

    /**
     * 获得砍价助力分页
     *
     * @param pageReqVO 分页查询
     * @return 砍价助力分页
     */
    PageResult<BargainHelpDO> getBargainHelpPage(BargainHelpPageReqVO pageReqVO);

    /**
     * 获得指定砍价记录编号，对应的砍价助力列表
     *
     * @param recordId 砍价记录编号
     * @return 砍价助力列表
     */
    List<BargainHelpDO> getBargainHelpListByRecordId(Long recordId);

    /**
     * 获得助力记录
     *
     * @param recordId 砍价记录编号
     * @param userId 用户编号
     * @return 助力记录
     */
    BargainHelpDO getBargainHelp(Long recordId, Long userId);

}
