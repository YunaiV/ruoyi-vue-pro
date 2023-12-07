package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
     * 更新砍价活动库存
     *
     * 如果更新失败（库存不足），则抛出业务异常
     *
     * @param id    砍价活动编号
     * @param count 购买数量
     */
    void updateBargainActivityStock(Long id, Integer count);

    /**
     * 关闭砍价活动
     *
     * @param id 砍价活动编号
     */
    void closeBargainActivityById(Long id);

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
     * 获得砍价活动列表
     *
     * @param ids 编号数组
     * @return 砍价活动列表
     */
    List<BargainActivityDO> getBargainActivityList(Set<Long> ids);

    /**
     * 校验砍价活动，是否可以参与（发起砍价、下单、帮好友砍价）
     *
     * @param id 编号
     * @return 砍价活动
     */
    BargainActivityDO validateBargainActivityCanJoin(Long id);

    /**
     * 获得砍价活动分页
     *
     * @param pageReqVO 分页查询
     * @return 砍价活动分页
     */
    PageResult<BargainActivityDO> getBargainActivityPage(BargainActivityPageReqVO pageReqVO);

    /**
     * 获取正在进行的活动分页数据
     *
     * @param pageReqVO 分页请求
     * @return 砍价活动分页
     */
    PageResult<BargainActivityDO> getBargainActivityPage(PageParam pageReqVO);

    /**
     * 获取正在进行的活动分页数据
     *
     * @param count 需要的数量
     * @return 砍价活动分页
     */
    List<BargainActivityDO> getBargainActivityListByCount(Integer count);

    /**
     * 获取指定 spu 编号最近参加的活动，每个 spuId 只返回一条记录
     *
     * @param spuIds   spu 编号
     * @param status   状态
     * @param dateTime 日期时间
     * @return 砍价活动列表
     */
    List<BargainActivityDO> getBargainActivityBySpuIdsAndStatusAndDateTimeLt(Collection<Long> spuIds, Integer status, LocalDateTime dateTime);

}
