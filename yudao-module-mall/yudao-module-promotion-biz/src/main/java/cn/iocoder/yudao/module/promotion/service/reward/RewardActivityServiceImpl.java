package cn.iocoder.yudao.module.promotion.service.reward;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.hutool.core.collection.CollUtil.intersectionDistinct;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
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

        // 插入
        RewardActivityDO rewardActivity = BeanUtils.toBean(createReqVO, RewardActivityDO.class)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
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
        RewardActivityDO updateObj = BeanUtils.toBean(updateReqVO, RewardActivityDO.class);
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
        // 1. 获得开启的所有的活动
        List<RewardActivityDO> list = rewardActivityMapper.selectList(RewardActivityDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
        if (id != null) { // 排除自己这个活动
            list.removeIf(activity -> id.equals(activity.getId()));
        }

        // 2. 完全不允许重叠
        for (RewardActivityDO item : list) {
            // 2.1 校验满减送活动时间是否冲突，如果时段不冲突那么不同的时间段内则可以存在相同的商品范围
            if (!LocalDateTimeUtil.isOverlap(item.getStartTime(), item.getEndTime(),
                    rewardActivity.getStartTime(), rewardActivity.getEndTime())) {
                continue;
            }
            // 2.2 校验商品范围是否重叠
            // 情况一：如果与该时间段内商品范围为全部的活动冲突，或 rewardActivity 商品范围为全部，那么则直接校验不通过
            // 例如说，rewardActivity 是全部活动，结果有个 db 里的 activity 是某个分类，它也是冲突的。也就是说，当前时间段内，有且仅有只能有一个活动！
            if (PromotionProductScopeEnum.isAll(item.getProductScope()) ||
                    PromotionProductScopeEnum.isAll(rewardActivity.getProductScope())) {
                throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(),
                        PromotionProductScopeEnum.isAll(item.getProductScope()) ?
                                "该活动商品范围为全部已覆盖包含本活动范围" : "本活动商品范围为全部已覆盖包含了该活动商品范围");
            }
            // 情况二：如果与该时间段内商品范围为类别的活动冲突
            if (PromotionProductScopeEnum.isCategory(item.getProductScope())) {
                // 校验分类是否冲突
                if (PromotionProductScopeEnum.isCategory(rewardActivity.getProductScope())) {
                    if (!intersectionDistinct(item.getProductScopeValues(), rewardActivity.getProductScopeValues()).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "商品分类范围重叠");
                    }
                }
                // 校验商品分类是否冲突
                if (PromotionProductScopeEnum.isSpu(rewardActivity.getProductScope())) {
                    List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(rewardActivity.getProductScopeValues());
                    if (!intersectionDistinct(item.getProductScopeValues(),
                            convertSet(spuList, ProductSpuRespDTO::getCategoryId)).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "该活动商品分类范围已包含本活动所选商品");
                    }
                }
            }
            // 情况三：如果与该时间段内商品范围为商品的活动冲突
            if (PromotionProductScopeEnum.isSpu(item.getProductScope())) {
                // 校验商品是否冲突
                if (PromotionProductScopeEnum.isSpu(rewardActivity.getProductScope())) {
                    if (!intersectionDistinct(item.getProductScopeValues(), rewardActivity.getProductScopeValues()).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "活动商品范围所选商品重叠");
                    }
                }
                // 校验商品分类是否冲突
                if (PromotionProductScopeEnum.isCategory(rewardActivity.getProductScope())) {
                    List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(item.getProductScopeValues());
                    if (!intersectionDistinct(rewardActivity.getProductScopeValues(),
                            convertSet(spuList, ProductSpuRespDTO::getCategoryId)).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "本活动商品分类范围包含了该活动所选商品");
                    }
                }
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
    public List<RewardActivityMatchRespDTO> getMatchRewardActivityListBySpuIds(Collection<Long> spuIds) {
        // 1. 查询商品分类
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(spuIds);
        if (CollUtil.isEmpty(spuList)) {
            return Collections.emptyList();
        }
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);

        // 2. 查询出指定 spuId 的 spu 参加的活动
        List<RewardActivityDO> activityList = rewardActivityMapper.selectListBySpuIdAndStatusAndNow(
                spuIds, convertSet(spuList, ProductSpuRespDTO::getCategoryId), CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(activityList)) {
            return Collections.emptyList();
        }

        // 3. 转换成 Response DTO
        return BeanUtils.toBean(activityList, RewardActivityMatchRespDTO.class, activityDTO -> {
            // 3.1 设置对应匹配的 spuIds
            activityDTO.setSpuIds(new ArrayList<>());
            for (Long spuId : spuIds) {
                if (PromotionProductScopeEnum.isAll(activityDTO.getProductScope())) {
                    activityDTO.getSpuIds().add(spuId);
                } else if (PromotionProductScopeEnum.isSpu(activityDTO.getProductScope())) {
                    if (CollUtil.contains(activityDTO.getProductScopeValues(), spuId)) {
                        activityDTO.getSpuIds().add(spuId);
                    }
                } else if (PromotionProductScopeEnum.isCategory(activityDTO.getProductScope())) {
                    ProductSpuRespDTO spu = spuMap.get(spuId);
                    if (spu != null && CollUtil.contains(activityDTO.getProductScopeValues(), spu.getCategoryId())) {
                        activityDTO.getSpuIds().add(spuId);
                    }
                }
            }

            // 3.2 设置每个 Rule 的描述
            activityDTO.getRules().forEach(rule -> {
                String description = "";
                if (PromotionConditionTypeEnum.PRICE.getType().equals(activityDTO.getConditionType())) {
                    description += StrUtil.format("满 {} 元", MoneyUtils.fenToYuanStr(rule.getLimit()));
                } else {
                    description += StrUtil.format("满 {} 件", rule.getLimit());
                }
                if (rule.getDiscountPrice() != null) {
                    description += StrUtil.format("减 {}", MoneyUtils.fenToYuanStr(rule.getDiscountPrice()));
                } else if (Boolean.TRUE.equals(rule.getFreeDelivery())) {
                    description += "包邮";
                } else if (rule.getPoint() != null && rule.getPoint() > 0) {
                    description += StrUtil.format("增 {} 积分", rule.getPoint());
                } else if (CollUtil.isNotEmpty(rule.getGiveCouponTemplateCounts())) {
                    description += StrUtil.format("送 {} 张优惠券",
                            getSumValue(rule.getGiveCouponTemplateCounts().values(), count -> count, Integer::sum));
                }
                rule.setDescription(description);
            });
        });
    }

}
