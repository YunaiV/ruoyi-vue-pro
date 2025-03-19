package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.bargain.BargainActivityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

/**
 * 砍价活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class BargainActivityServiceImpl implements BargainActivityService {

    @Resource
    private BargainActivityMapper bargainActivityMapper;

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBargainActivity(BargainActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateBargainConflict(createReqVO.getSpuId(), null);
        // 校验商品 sku 是否存在
        validateSku(createReqVO.getSkuId());

        // 插入砍价活动
        BargainActivityDO activityDO = BargainActivityConvert.INSTANCE.convert(createReqVO)
                .setTotalStock(createReqVO.getStock())
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        bargainActivityMapper.insert(activityDO);
        return activityDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBargainActivity(BargainActivityUpdateReqVO updateReqVO) {
        // 校验存在
        BargainActivityDO activity = validateBargainActivityExists(updateReqVO.getId());
        // 校验状态
        if (ObjectUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_STATUS_DISABLE);
        }
        // 校验商品冲突
        validateBargainConflict(updateReqVO.getSpuId(), updateReqVO.getId());
        // 校验商品 sku 是否存在
        validateSku(updateReqVO.getSkuId());

        // 更新
        BargainActivityDO updateObj = BargainActivityConvert.INSTANCE.convert(updateReqVO);
        if (updateObj.getStock() > activity.getTotalStock()) { // 如果更新的库存大于原来的库存，则更新总库存
            updateObj.setTotalStock(updateObj.getStock());
        }
        bargainActivityMapper.updateById(updateObj);
    }

    @Override
    public void updateBargainActivityStock(Long id, Integer count) {
        if (count < 0) {
            // 更新库存。如果更新失败，则抛出异常
            int updateCount = bargainActivityMapper.updateStock(id, count);
            if (updateCount == 0) {
                throw exception(BARGAIN_ACTIVITY_STOCK_NOT_ENOUGH);
            }
        } else if (count > 0) {
            bargainActivityMapper.updateStock(id, count);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeBargainActivityById(Long id) {
        // 校验砍价活动是否存在
        BargainActivityDO activity = validateBargainActivityExists(id);
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_STATUS_DISABLE);
        }

        bargainActivityMapper.updateById(new BargainActivityDO().setId(id)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
    }

    private void validateBargainConflict(Long spuId, Long activityId) {
        // 查询所有开启的砍价活动
        List<BargainActivityDO> activityList = bargainActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) { // 更新时排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 校验商品 spu 是否参加了其它活动
        if (anyMatch(activityList, activity -> ObjectUtil.equal(activity.getSpuId(), spuId))) {
            throw exception(BARGAIN_ACTIVITY_SPU_CONFLICTS);
        }
    }

    private void validateSku(Long skuId) {
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBargainActivity(Long id) {
        // 校验存在
        BargainActivityDO activityDO = validateBargainActivityExists(id);
        // 校验状态
        if (CommonStatusEnum.isEnable(activityDO.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除
        bargainActivityMapper.deleteById(id);
    }

    private BargainActivityDO validateBargainActivityExists(Long id) {
        BargainActivityDO activityDO = bargainActivityMapper.selectById(id);
        if (activityDO == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }
        return activityDO;
    }

    @Override
    public BargainActivityDO getBargainActivity(Long id) {
        return bargainActivityMapper.selectById(id);
    }

    @Override
    public List<BargainActivityDO> getBargainActivityList(Set<Long> ids) {
        return bargainActivityMapper.selectBatchIds(ids);
    }

    @Override
    public BargainActivityDO validateBargainActivityCanJoin(Long id) {
        BargainActivityDO activity = bargainActivityMapper.selectById(id);
        if (activity == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_STATUS_CLOSED);
        }
        if (activity.getStock() <= 0) {
            throw exception(BARGAIN_ACTIVITY_STOCK_NOT_ENOUGH);
        }
        if (!LocalDateTimeUtils.isBetween(activity.getStartTime(), activity.getEndTime())) {
            throw exception(BARGAIN_ACTIVITY_TIME_END);
        }
        return activity;
    }

    @Override
    public PageResult<BargainActivityDO> getBargainActivityPage(BargainActivityPageReqVO pageReqVO) {
        return bargainActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<BargainActivityDO> getBargainActivityPage(PageParam pageReqVO) {
        // 只查询进行中，且在时间范围内的
        return bargainActivityMapper.selectPage(pageReqVO, CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
    }

    @Override
    public List<BargainActivityDO> getBargainActivityListByCount(Integer count) {
        return bargainActivityMapper.selectList(count, CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
    }

    @Override
    public BargainActivityDO getMatchBargainActivityBySpuId(Long spuId) {
        return bargainActivityMapper.selectBySpuIdAndStatusAndNow(spuId, CommonStatusEnum.ENABLE.getStatus());
    }

}
