package cn.iocoder.yudao.module.promotion.service.reward;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.util.PromotionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.collection.CollUtil.intersectionDistinct;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

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

    @Resource
    private ProductCategoryApi productCategoryApi;
    @Resource
    private ProductSpuApi productSpuApi;

    @Override
    public Long createRewardActivity(RewardActivityCreateReqVO createReqVO) {
        // 1.1 校验商品范围
        validateProductScope(createReqVO.getProductScope(), createReqVO.getProductScopeValues());
        // 1.2 校验商品是否冲突
        validateRewardActivitySpuConflicts(null, createReqVO);

        // 2. 插入
        RewardActivityDO rewardActivity = BeanUtils.toBean(createReqVO, RewardActivityDO.class)
                .setStatus(PromotionUtils.calculateActivityStatus(createReqVO.getEndTime()));
        rewardActivityMapper.insert(rewardActivity);
        // 返回
        return rewardActivity.getId();
    }

    @Override
    public void updateRewardActivity(RewardActivityUpdateReqVO updateReqVO) {
        // 1.1 校验存在
        RewardActivityDO dbRewardActivity = validateRewardActivityExists(updateReqVO.getId());
        if (dbRewardActivity.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) { // 已关闭的活动，不能修改噢
            throw exception(REWARD_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 1.2 校验商品范围
        validateProductScope(updateReqVO.getProductScope(), updateReqVO.getProductScopeValues());
        // 1.3 校验商品是否冲突
        validateRewardActivitySpuConflicts(updateReqVO.getId(), updateReqVO);

        // 2. 更新
        RewardActivityDO updateObj = BeanUtils.toBean(updateReqVO, RewardActivityDO.class)
                .setStatus(PromotionUtils.calculateActivityStatus(updateReqVO.getEndTime()));
        rewardActivityMapper.updateById(updateObj);
    }

    @Override
    public void closeRewardActivity(Long id) {
        // 校验存在
        RewardActivityDO dbRewardActivity = validateRewardActivityExists(id);
        if (dbRewardActivity.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) { // 已关闭的活动，不能关闭噢
            throw exception(REWARD_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }

        // 更新
        rewardActivityMapper.updateById(new RewardActivityDO().setId(id).setStatus(CommonStatusEnum.DISABLE.getStatus()));
    }

    @Override
    public void deleteRewardActivity(Long id) {
        // 校验存在
        RewardActivityDO dbRewardActivity = validateRewardActivityExists(id);
        if (dbRewardActivity.getStatus().equals(CommonStatusEnum.ENABLE.getStatus())) { // 未关闭的活动，不能删除噢
            throw exception(REWARD_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED);
        }

        // 删除
        rewardActivityMapper.deleteById(id);
    }

    private RewardActivityDO validateRewardActivityExists(Long id) {
        RewardActivityDO activity = rewardActivityMapper.selectById(id);
        if (activity == null) {
            throw exception(REWARD_ACTIVITY_NOT_EXISTS);
        }
        return activity;
    }

    /**
     * 校验商品参加的活动是否冲突
     *
     * @param id             活动编号
     * @param rewardActivity 请求
     */
    private void validateRewardActivitySpuConflicts(Long id, RewardActivityBaseVO rewardActivity) {
        // 0. 获得所有的活动包括关闭的
        List<RewardActivityDO> list = rewardActivityMapper.selectList();
        if (id != null) { // 排除自己这个活动
            list.removeIf(activity -> id.equals(activity.getId()));
        }

        // 1.1 校验满减送活动时间是否冲突
        boolean hasConflict = list.stream().anyMatch(item -> LocalDateTimeUtil.isOverlap(item.getStartTime(), item.getEndTime(),
                rewardActivity.getStartTime(), rewardActivity.getEndTime()));
        if (hasConflict) {
            throw exception(REWARD_ACTIVITY_TIME_CONFLICTS);
        }
        // 1.2 校验商品范围是否重叠
        if (PromotionProductScopeEnum.isAll(rewardActivity.getProductScope()) &&  // 情况一：全部商品参加
                anyMatch(list, item -> PromotionProductScopeEnum.isAll(item.getProductScope()))) {
            throw exception(REWARD_ACTIVITY_SCOPE_ALL_EXISTS);
        }
        if (PromotionProductScopeEnum.isSpu(rewardActivity.getProductScope()) ||  // 情况二：指定商品参加
                PromotionProductScopeEnum.isCategory(rewardActivity.getProductScope())) {  // 情况三：指定商品类型参加
            if (anyMatch(list, item -> !intersectionDistinct(item.getProductScopeValues(),
                    rewardActivity.getProductScopeValues()).isEmpty())) {
                throw exception(PromotionProductScopeEnum.isSpu(rewardActivity.getProductScope()) ?
                        REWARD_ACTIVITY_SPU_CONFLICTS : REWARD_ACTIVITY_SCOPE_CATEGORY_EXISTS);
            }
        }
    }

    private void validateProductScope(Integer productScope, List<Long> productScopeValues) {
        if (Objects.equals(PromotionProductScopeEnum.SPU.getScope(), productScope)) {
            productSpuApi.validateSpuList(productScopeValues);
        } else if (Objects.equals(PromotionProductScopeEnum.CATEGORY.getScope(), productScope)) {
            productCategoryApi.validateCategoryList(productScopeValues);
        }
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
    public List<RewardActivityDO> getRewardActivityListByStatusAndDateTimeLt(Integer status, LocalDateTime dateTime) {
        return rewardActivityMapper.selectListByStatusAndDateTimeLt(status, dateTime);
    }

}
