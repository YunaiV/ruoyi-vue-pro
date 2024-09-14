package cn.iocoder.yudao.module.promotion.service.reward;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityBaseVO;
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
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.collection.CollUtil.intersectionDistinct;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
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
        RewardActivityDO rewardActivity = RewardActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(
                        PromotionUtils.calculateActivityStatus(createReqVO.getEndTime()).equals(CommonStatusEnum.DISABLE.getStatus())?
                                PromotionActivityStatusEnum.WAIT.getStatus():
                                PromotionActivityStatusEnum.RUN.getStatus()
                );
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
                // TODO puhui999：需要提示出来与满减送活动“xxx 活动”存在商品范围冲突；这里可能要分情况，下面需要把 activityName 传入
                throw exception(REWARD_ACTIVITY_SCOPE_EXISTS);
            }
            // 情况二：如果与该时间段内商品范围为类别的活动冲突
            if (PromotionProductScopeEnum.isCategory(item.getProductScope())) {
                // TODO puhui999：前端我们有限制，只允许子分类么？可能要限制下，不然基于分类查询不到对应的商品。因为商品目前必须在子分类下
                // 校验分类是否冲突
                if (PromotionProductScopeEnum.isCategory(rewardActivity.getProductScope())) {
                    if (!intersectionDistinct(item.getProductScopeValues(), rewardActivity.getProductScopeValues()).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS);
                    }
                }
                // 校验商品分类是否冲突
                if (PromotionProductScopeEnum.isSpu(rewardActivity.getProductScope())) {
                    List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(rewardActivity.getProductScopeValues());
                    if (!intersectionDistinct(item.getProductScopeValues(),
                            convertSet(spuList, ProductSpuRespDTO::getCategoryId)).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS);
                    }
                }
            }
            // 情况三：如果与该时间段内商品范围为商品的活动冲突
            if (PromotionProductScopeEnum.isSpu(item.getProductScope())) {
                // 校验商品是否冲突
                if (PromotionProductScopeEnum.isSpu(rewardActivity.getProductScope())) {
                    if (!intersectionDistinct(item.getProductScopeValues(), rewardActivity.getProductScopeValues()).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS);
                    }
                }
                // 校验商品分类是否冲突
                if (PromotionProductScopeEnum.isCategory(rewardActivity.getProductScope())) {
                    List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(item.getProductScopeValues());
                    if (!intersectionDistinct(rewardActivity.getProductScopeValues(),
                            convertSet(spuList, ProductSpuRespDTO::getCategoryId)).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS);
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
    public List<RewardActivityDO> getRewardActivityListByStatusAndDateTimeLt(Integer status, LocalDateTime dateTime) {
        return rewardActivityMapper.selectListByStatusAndDateTimeLt(status, dateTime);
    }

}
