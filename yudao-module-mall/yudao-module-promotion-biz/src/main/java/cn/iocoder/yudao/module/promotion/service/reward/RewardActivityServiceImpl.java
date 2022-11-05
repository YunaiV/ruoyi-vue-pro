package cn.iocoder.yudao.module.promotion.service.reward;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.reward.RewardActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.module.promotion.util.PromotionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.REWARD_ACTIVITY_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.REWARD_ACTIVITY_SPU_CONFLICTS;

/**
 * 满减送活动 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class RewardActivityServiceImpl implements RewardActivityService {

    @Resource
    private RewardActivityMapper rewardActivityMapper;

    @Override
    public Long createRewardActivity(RewardActivityCreateReqVO createReqVO) {
        // 校验商品是否冲突
        validateRewardActivitySpuConflicts(null, createReqVO.getProductSpuIds());

        // 插入
        RewardActivityDO rewardActivity = RewardActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(createReqVO.getStartTime(), createReqVO.getEndTime()));
        rewardActivityMapper.insert(rewardActivity);
        // 返回
        return rewardActivity.getId();
    }

    @Override
    public void updateRewardActivity(RewardActivityUpdateReqVO updateReqVO) {
        // 校验存在
        validateRewardActivityExists(updateReqVO.getId());
        validateRewardActivitySpuConflicts(updateReqVO.getId(), updateReqVO.getProductSpuIds());

        // 更新
        RewardActivityDO updateObj = RewardActivityConvert.INSTANCE.convert(updateReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(updateReqVO.getStartTime(), updateReqVO.getEndTime()));
        rewardActivityMapper.updateById(updateObj);
    }

    @Override
    public void deleteRewardActivity(Long id) {
        // 校验存在
        validateRewardActivityExists(id);
        // 删除
        rewardActivityMapper.deleteById(id);
    }

    private void validateRewardActivityExists(Long id) {
        if (rewardActivityMapper.selectById(id) == null) {
            throw exception(REWARD_ACTIVITY_NOT_EXISTS);
        }
    }

    /**
     * 校验商品参加的活动是否冲突
     *
     * @param id 活动编号
     * @param spuIds 商品 SPU 编号数组
     */
    private void validateRewardActivitySpuConflicts(Long id, Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return;
        }
        // 查询商品参加的活动
        List<RewardActivityDO> rewardActivityList = getRewardActivityListBySpuIds(spuIds);
        if (id != null) { // 排除活动
            rewardActivityList.removeIf(activity -> id.equals(activity.getId()));
        }
        // 如果非空，则说明冲突
        if (CollUtil.isNotEmpty(rewardActivityList)) {
            throw exception(REWARD_ACTIVITY_SPU_CONFLICTS);
        }
    }

    /**
     * 获得商品参加的满减送活动的数组
     *
     * @param spuIds 商品 SPU 编号数组
     * @return 商品参加的满减送活动的数组
     */
    private List<RewardActivityDO> getRewardActivityListBySpuIds(Collection<Long> spuIds) {
        List<RewardActivityDO> list = rewardActivityMapper.selectList();
        return CollUtil.filter(list, activity -> CollUtil.containsAny(activity.getProductSpuIds(), spuIds));
    }

    @Override
    public RewardActivityDO getRewardActivity(Long id) {
        return rewardActivityMapper.selectById(id);
    }

    @Override
    public PageResult<RewardActivityDO> getRewardActivityPage(RewardActivityPageReqVO pageReqVO) {
        return rewardActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<RewardActivityDO, Set<Long>> getMatchRewardActivities(Set<Long> spuIds) {
        // TODO 芋艿：待实现
        return null;
    }

}
