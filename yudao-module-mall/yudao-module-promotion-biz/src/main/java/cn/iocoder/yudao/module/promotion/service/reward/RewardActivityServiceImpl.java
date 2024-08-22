package cn.iocoder.yudao.module.promotion.service.reward;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.reward.RewardActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.util.PromotionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;

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
        //validateRewardActivitySpuConflicts(null, createReqVO.getProductSpuIds());

        // 2. 插入
        RewardActivityDO rewardActivity = RewardActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(createReqVO.getEndTime()));
        rewardActivityMapper.insert(rewardActivity);
        // 返回
        return rewardActivity.getId();
    }

    @Override
    public void updateRewardActivity(RewardActivityUpdateReqVO updateReqVO) {
        // 1.1 校验存在
        RewardActivityDO dbRewardActivity = validateRewardActivityExists(updateReqVO.getId());
        if (dbRewardActivity.getStatus().equals(PromotionActivityStatusEnum.CLOSE.getStatus())) { // 已关闭的活动，不能修改噢
            throw exception(REWARD_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 1.2 校验商品范围
        validateProductScope(updateReqVO.getProductScope(), updateReqVO.getProductScopeValues());
        // 1.3 校验商品是否冲突
        //validateRewardActivitySpuConflicts(updateReqVO.getId(), updateReqVO.getProductSpuIds());

        // 2. 更新
        RewardActivityDO updateObj = RewardActivityConvert.INSTANCE.convert(updateReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(updateReqVO.getEndTime()));
        rewardActivityMapper.updateById(updateObj);
    }

    @Override
    public void closeRewardActivity(Long id) {
        // 校验存在
        RewardActivityDO dbRewardActivity = validateRewardActivityExists(id);
        if (dbRewardActivity.getStatus().equals(PromotionActivityStatusEnum.CLOSE.getStatus())) { // 已关闭的活动，不能关闭噢
            throw exception(REWARD_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }
        if (dbRewardActivity.getStatus().equals(PromotionActivityStatusEnum.END.getStatus())) { // 已关闭的活动，不能关闭噢
            throw exception(REWARD_ACTIVITY_CLOSE_FAIL_STATUS_END);
        }

        // 更新
        RewardActivityDO updateObj = new RewardActivityDO().setId(id).setStatus(PromotionActivityStatusEnum.CLOSE.getStatus());
        rewardActivityMapper.updateById(updateObj);
    }

    @Override
    public void deleteRewardActivity(Long id) {
        // 校验存在
        RewardActivityDO dbRewardActivity = validateRewardActivityExists(id);
        if (!dbRewardActivity.getStatus().equals(PromotionActivityStatusEnum.CLOSE.getStatus())) { // 未关闭的活动，不能删除噢
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

    // TODO @芋艿：逻辑有问题，需要优化；要分成全场、和指定来校验；
    // TODO @puhui999: 下次提交 fix
    /**
     * 校验商品参加的活动是否冲突
     *
     * @param id     活动编号
     * @param spuIds 商品 SPU 编号数组
     */
    private void validateRewardActivitySpuConflicts(Long id, Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return;
        }
        // 查询商品参加的活动
        List<RewardActivityDO> rewardActivityList = getRewardActivityListBySpuIds(spuIds,
                asList(PromotionActivityStatusEnum.WAIT.getStatus(), PromotionActivityStatusEnum.RUN.getStatus()));
        if (id != null) { // 排除自己这个活动
            rewardActivityList.removeIf(activity -> id.equals(activity.getId()));
        }
        // 如果非空，则说明冲突
        if (CollUtil.isNotEmpty(rewardActivityList)) {
            throw exception(REWARD_ACTIVITY_SPU_CONFLICTS);
        }
    }

    private void validateProductScope(Integer productScope, List<Long> productScopeValues) {
        if (Objects.equals(PromotionProductScopeEnum.SPU.getScope(), productScope)) {
            productSpuApi.validateSpuList(productScopeValues);
        } else if (Objects.equals(PromotionProductScopeEnum.CATEGORY.getScope(), productScope)) {
            productCategoryApi.validateCategoryList(productScopeValues);
        }
    }

    /**
     * 获得商品参加的满减送活动的数组
     *
     * @param spuIds   商品 SPU 编号数组
     * @param statuses 活动状态数组
     * @return 商品参加的满减送活动的数组
     */
    private List<RewardActivityDO> getRewardActivityListBySpuIds(Collection<Long> spuIds,
                                                                 Collection<Integer> statuses) {
        // TODO @puhui999: 下次 fix
        //List<RewardActivityDO> list = rewardActivityMapper.selectListByStatus(statuses);
        //return CollUtil.filter(list, activity -> CollUtil.containsAny(activity.getProductSpuIds(), spuIds));
        return List.of();
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
    public List<RewardActivityMatchRespDTO> getMatchRewardActivityList(Collection<Long> spuIds) {
        // TODO 芋艿：待实现；先指定，然后再全局的；
//        // 如果有全局活动，则直接选择它
//        List<RewardActivityDO> allActivities = rewardActivityMapper.selectListByProductScopeAndStatus(
//                PromotionProductScopeEnum.ALL.getScope(), PromotionActivityStatusEnum.RUN.getStatus());
//        if (CollUtil.isNotEmpty(allActivities)) {
//            return MapUtil.builder(allActivities.get(0), spuIds).build();
//        }
//
//        // 查询某个活动参加的活动
//        List<RewardActivityDO> productActivityList = getRewardActivityListBySpuIds(spuIds,
//                singleton(PromotionActivityStatusEnum.RUN.getStatus()));
//        return convertMap(productActivityList, activity -> activity,
//                rewardActivityDO -> intersectionDistinct(rewardActivityDO.getProductSpuIds(), spuIds)); // 求交集返回
        return null;
    }

    @Override
    public List<RewardActivityDO> getRewardActivityBySpuIdsAndStatusAndDateTimeLt(Collection<Long> spuIds, Integer status, LocalDateTime dateTime) {
        // 1. 查询出指定 spuId 的 spu 参加的活动
        List<RewardActivityDO> rewardActivityList = rewardActivityMapper.selectListBySpuIdsAndStatus(spuIds, status);
        if (CollUtil.isEmpty(rewardActivityList)) {
            return Collections.emptyList();
        }

        // 2. 查询活动详情
        return rewardActivityMapper.selectListByIdsAndDateTimeLt(convertSet(rewardActivityList, RewardActivityDO::getId), dateTime);
    }

}
