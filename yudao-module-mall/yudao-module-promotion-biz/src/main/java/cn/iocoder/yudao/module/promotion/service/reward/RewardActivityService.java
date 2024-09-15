package cn.iocoder.yudao.module.promotion.service.reward;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 满减送活动 Service 接口
 *
 * @author 芋道源码
 */
public interface RewardActivityService {

    /**
     * 创建满减送活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRewardActivity(@Valid RewardActivityCreateReqVO createReqVO);

    /**
     * 更新满减送活动
     *
     * @param updateReqVO 更新信息
     */
    void updateRewardActivity(@Valid RewardActivityUpdateReqVO updateReqVO);

    /**
     * 关闭满减送活动
     *
     * @param id 活动编号
     */
    void closeRewardActivity(Long id);

    /**
     * 删除满减送活动
     *
     * @param id 编号
     */
    void deleteRewardActivity(Long id);

    /**
     * 获得满减送活动
     *
     * @param id 编号
     * @return 满减送活动
     */
    RewardActivityDO getRewardActivity(Long id);

    /**
     * 获得满减送活动分页
     *
     * @param pageReqVO 分页查询
     * @return 满减送活动分页
     */
    PageResult<RewardActivityDO> getRewardActivityPage(RewardActivityPageReqVO pageReqVO);

    /**
     * 获得 spuId 商品匹配的的满减送活动列表
     *
     * @param spuIds   SPU 编号数组
     * @return 满减送活动列表
     */
    List<RewardActivityMatchRespDTO> getMatchRewardActivityListBySpuIds(Collection<Long> spuIds);

}
