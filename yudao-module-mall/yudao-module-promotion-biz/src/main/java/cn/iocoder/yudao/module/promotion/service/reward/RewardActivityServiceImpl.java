package cn.iocoder.yudao.module.promotion.service.reward;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.reward.RewardActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.REWARD_ACTIVITY_NOT_EXISTS;

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
        // 插入
        RewardActivityDO rewardActivity = RewardActivityConvert.INSTANCE.convert(createReqVO);
        rewardActivityMapper.insert(rewardActivity);
        // 返回
        return rewardActivity.getId();
    }

    @Override
    public void updateRewardActivity(RewardActivityUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateRewardActivityExists(updateReqVO.getId());
        // 更新
        RewardActivityDO updateObj = RewardActivityConvert.INSTANCE.convert(updateReqVO);
        rewardActivityMapper.updateById(updateObj);
    }

    @Override
    public void deleteRewardActivity(Integer id) {
        // 校验存在
        this.validateRewardActivityExists(id);
        // 删除
        rewardActivityMapper.deleteById(id);
    }

    private void validateRewardActivityExists(Integer id) {
        if (rewardActivityMapper.selectById(id) == null) {
            throw exception(REWARD_ACTIVITY_NOT_EXISTS);
        }
    }

    @Override
    public RewardActivityDO getRewardActivity(Integer id) {
        return rewardActivityMapper.selectById(id);
    }

    @Override
    public List<RewardActivityDO> getRewardActivityList(Collection<Integer> ids) {
        return rewardActivityMapper.selectBatchIds(ids);
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
