package cn.iocoder.yudao.module.promotion.service.seckill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductBaseVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillProductMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillconfig.SeckillConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isBetween;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;

/**
 * 秒杀活动 Service 实现类
 *
 * @author halfninety
 */
@Service
@Validated
public class SeckillActivityServiceImpl implements SeckillActivityService {

    @Resource
    private SeckillActivityMapper seckillActivityMapper;
    @Resource
    private SeckillProductMapper seckillProductMapper;
    @Resource
    private SeckillConfigMapper seckillConfigMapper;
    @Resource
    private SeckillConfigService seckillConfigService;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillActivity(SeckillActivityCreateReqVO createReqVO) {
        // 1.1 校验商品秒杀时段是否冲突
        validateProductConflict(createReqVO.getConfigIds(), createReqVO.getSpuId(), null);
        // 1.2 校验商品是否存在
        validateProductExists(createReqVO.getSpuId(), createReqVO.getProducts());

        // 2.1 插入秒杀活动
        SeckillActivityDO activity = SeckillActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setStock(getSumValue(createReqVO.getProducts(), SeckillProductBaseVO::getStock, Integer::sum));
        activity.setTotalStock(activity.getStock());
        seckillActivityMapper.insert(activity);
        // 2.2 插入商品
        List<SeckillProductDO> products = SeckillActivityConvert.INSTANCE.convertList(createReqVO.getProducts(), activity);
        seckillProductMapper.insertBatch(products);
        return activity.getId();
    }

    /**
     * 校验秒杀商品参与的活动是否存在冲突
     *
     * 1. 校验秒杀时段是否存在
     * 2. 秒杀商品是否参加其它活动
     *
     * @param configIds  秒杀时段数组
     * @param spuId      商品 SPU 编号
     * @param activityId 秒杀活动编号
     */
    private void validateProductConflict(List<Long> configIds, Long spuId, Long activityId) {
        // 1. 校验秒杀时段是否存在
        seckillConfigService.validateSeckillConfigExists(configIds);

        // 2.1 查询所有开启的秒杀活动
        List<SeckillActivityDO> activityList = seckillActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) { // 排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 2.2 过滤出所有 configIds 有交集的活动，判断是否存在重叠
        List<SeckillActivityDO> conflictActivityList = filterList(activityList, s -> containsAny(s.getConfigIds(), configIds));
        if (isNotEmpty(conflictActivityList)) {
            throw exception(SECKILL_ACTIVITY_SPU_CONFLICTS);
        }
    }

    /**
     * 校验秒杀商品是否都存在
     *
     * @param spuId    商品 SPU 编号
     * @param products 秒杀商品
     */
    private void validateProductExists(Long spuId, List<SeckillProductBaseVO> products) {
        // 1. 校验商品 spu 是否存在
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            throw exception(SPU_NOT_EXISTS);
        }

        // 2. 校验商品 sku 都存在
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(singletonList(spuId));
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        products.forEach(product -> {
            if (!skuMap.containsKey(product.getSkuId())) {
                throw exception(SKU_NOT_EXISTS);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillActivity(SeckillActivityUpdateReqVO updateReqVO) {
        // 1.1 校验存在
        SeckillActivityDO activity = validateSeckillActivityExists(updateReqVO.getId());
        if (CommonStatusEnum.DISABLE.getStatus().equals(activity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 1.2 校验商品是否冲突
        validateProductConflict(updateReqVO.getConfigIds(), updateReqVO.getSpuId(), updateReqVO.getId());
        // 1.3 校验商品是否存在
        validateProductExists(updateReqVO.getSpuId(), updateReqVO.getProducts());

        // 2.1 更新活动
        SeckillActivityDO updateObj = SeckillActivityConvert.INSTANCE.convert(updateReqVO)
                .setStock(getSumValue(updateReqVO.getProducts(), SeckillProductBaseVO::getStock, Integer::sum));
        if (updateObj.getStock() > activity.getTotalStock()) { // 如果更新的库存大于原来的库存，则更新总库存
            updateObj.setTotalStock(updateObj.getStock());
        }
        seckillActivityMapper.updateById(updateObj);
        // 2.2 更新商品
        updateSeckillProduct(updateObj, updateReqVO.getProducts());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillStockDecr(Long id, Long skuId, Integer count) {
        // 1.1 校验活动库存是否充足
        SeckillActivityDO seckillActivity = validateSeckillActivityExists(id);
        if (count > seckillActivity.getTotalStock()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        // 1.2 校验商品库存是否充足
        SeckillProductDO product = seckillProductMapper.selectByActivityIdAndSkuId(id, skuId);
        if (product == null || count > product.getStock()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 2.1 更新活动商品库存
        int updateCount = seckillProductMapper.updateStockDecr(product.getId(), count);
        if (updateCount == 0) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 2.2 更新活动库存
        updateCount = seckillActivityMapper.updateStockDecr(seckillActivity.getId(), count);
        if (updateCount == 0) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillStockIncr(Long id, Long skuId, Integer count) {
        SeckillProductDO product = seckillProductMapper.selectByActivityIdAndSkuId(id, skuId);
        // 更新活动商品库存
        seckillProductMapper.updateStockIncr(product.getId(), count);
        // 更新活动库存
        seckillActivityMapper.updateStockIncr(id, count);
    }

    /**
     * 更新秒杀商品
     *
     * @param activity 秒杀活动
     * @param products 该活动的最新商品配置
     */
    private void updateSeckillProduct(SeckillActivityDO activity, List<SeckillProductBaseVO> products) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SeckillProductDO> newList = SeckillActivityConvert.INSTANCE.convertList(products, activity);
        List<SeckillProductDO> oldList = seckillProductMapper.selectListByActivityId(activity.getId());
        List<List<SeckillProductDO>> diffList = diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (isNotEmpty(diffList.get(0))) {
            seckillProductMapper.insertBatch(diffList.get(0));
        }
        if (isNotEmpty(diffList.get(1))) {
            seckillProductMapper.updateBatch(diffList.get(1));
        }
        if (isNotEmpty(diffList.get(2))) {
            seckillProductMapper.deleteBatchIds(convertList(diffList.get(2), SeckillProductDO::getId));
        }
    }

    @Override
    public void closeSeckillActivity(Long id) {
        // 校验存在
        SeckillActivityDO activity = validateSeckillActivityExists(id);
        if (CommonStatusEnum.DISABLE.getStatus().equals(activity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }

        // 更新
        SeckillActivityDO updateObj = new SeckillActivityDO().setId(id).setStatus(CommonStatusEnum.DISABLE.getStatus());
        seckillActivityMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckillActivity(Long id) {
        // 校验存在
        SeckillActivityDO seckillActivity = this.validateSeckillActivityExists(id);
        if (CommonStatusEnum.ENABLE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除活动
        seckillActivityMapper.deleteById(id);
        // 删除活动商品
        List<SeckillProductDO> products = seckillProductMapper.selectListByActivityId(id);
        seckillProductMapper.deleteBatchIds(convertSet(products, SeckillProductDO::getId));
    }

    private SeckillActivityDO validateSeckillActivityExists(Long id) {
        SeckillActivityDO seckillActivity = seckillActivityMapper.selectById(id);
        if (seckillActivity == null) {
            throw exception(SECKILL_ACTIVITY_NOT_EXISTS);
        }
        return seckillActivity;
    }

    @Override
    public SeckillActivityDO getSeckillActivity(Long id) {
        return seckillActivityMapper.selectById(id);
    }

    @Override
    public PageResult<SeckillActivityDO> getSeckillActivityPage(SeckillActivityPageReqVO pageReqVO) {
        return seckillActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SeckillProductDO> getSeckillProductListByActivityId(Long activityId) {
        return seckillProductMapper.selectListByActivityId(activityId);
    }

    @Override
    public List<SeckillProductDO> getSeckillProductListByActivityId(Collection<Long> activityIds) {
        return seckillProductMapper.selectListByActivityId(activityIds);
    }

    @Override
    public List<SeckillActivityDO> getSeckillActivityListByConfigIdAndStatus(Long configId, Integer status) {
        return filterList(seckillActivityMapper.selectList(SeckillActivityDO::getStatus, status),
                item -> anyMatch(item.getConfigIds(), id -> ObjectUtil.equal(id, configId)) // 校验时段
                        && isBetween(item.getStartTime(), item.getEndTime())); // 追加当前日期是否处在活动日期之间的校验条件
    }

    @Override
    public PageResult<SeckillActivityDO> getSeckillActivityAppPageByConfigId(AppSeckillActivityPageReqVO pageReqVO) {
        return seckillActivityMapper.selectPage(pageReqVO, CommonStatusEnum.ENABLE.getStatus(),LocalDateTime.now());
    }

    @Override
    public SeckillValidateJoinRespDTO validateJoinSeckill(Long activityId, Long skuId, Integer count) {
        // 1.1 校验秒杀活动是否存在
        SeckillActivityDO activity = validateSeckillActivityExists(activityId);
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(SECKILL_JOIN_ACTIVITY_STATUS_CLOSED);
        }
        // 1.2 是否在活动时间范围内
        if (!LocalDateTimeUtils.isBetween(activity.getStartTime(), activity.getEndTime())) {
            throw exception(SECKILL_JOIN_ACTIVITY_TIME_ERROR);
        }
        SeckillConfigDO config = seckillConfigService.getCurrentSeckillConfig();
        if (config == null || !CollectionUtil.contains(activity.getConfigIds(), config.getId())) {
            throw exception(SECKILL_JOIN_ACTIVITY_TIME_ERROR);
        }
        // 1.3 超过单次购买限制
        if (count > activity.getSingleLimitCount()) {
            throw exception(SECKILL_JOIN_ACTIVITY_SINGLE_LIMIT_COUNT_EXCEED);
        }

        // 2.1 校验秒杀商品是否存在
        SeckillProductDO product = seckillProductMapper.selectByActivityIdAndSkuId(activityId, skuId);
        if (product == null) {
            throw exception(SECKILL_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 2.2 校验库存是否充足
        if (count > product.getStock()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        return SeckillActivityConvert.INSTANCE.convert02(activity, product);
    }

    @Override
    public List<SeckillActivityDO> getSeckillActivityBySpuIdsAndStatusAndDateTimeLt(Collection<Long> spuIds, Integer status, LocalDateTime dateTime) {
        // 1.查询出指定 spuId 的 spu 参加的活动最接近现在的一条记录。多个的话，一个 spuId 对应一个最近的活动编号
        List<Map<String, Object>> spuIdAndActivityIdMaps = seckillActivityMapper.selectSpuIdAndActivityIdMapsBySpuIdsAndStatus(spuIds, status);
        if (CollUtil.isEmpty(spuIdAndActivityIdMaps)) {
            return Collections.emptyList();
        }
        // 2.查询活动详情
        return seckillActivityMapper.selectListByIdsAndDateTimeLt(
                convertSet(spuIdAndActivityIdMaps, map -> MapUtil.getLong(map, "activityId")), dateTime);
    }

}
