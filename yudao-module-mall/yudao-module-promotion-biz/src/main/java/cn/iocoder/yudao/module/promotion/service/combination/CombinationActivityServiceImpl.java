package cn.iocoder.yudao.module.promotion.service.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductBaseVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;

/**
 * 拼团活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class CombinationActivityServiceImpl implements CombinationActivityService {

    @Resource
    private CombinationActivityMapper combinationActivityMapper;
    @Resource
    private CombinationProductMapper combinationProductMapper;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCombinationActivity(CombinationActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateProductConflict(createReqVO.getSpuId(), null);
        // 校验商品是否存在
        validateProductExists(createReqVO.getSpuId(), createReqVO.getProducts());

        // 插入拼团活动
        CombinationActivityDO activity = CombinationActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        combinationActivityMapper.insert(activity);
        // 插入商品
        List<CombinationProductDO> products = CombinationActivityConvert.INSTANCE.convertList(createReqVO.getProducts(), activity);
        combinationProductMapper.insertBatch(products);
        return activity.getId();
    }

    /**
     * 校验拼团商品参与的活动是否存在冲突
     *
     * @param spuId      商品 SPU 编号
     * @param activityId 拼团活动编号
     */
    private void validateProductConflict(Long spuId, Long activityId) {
        // 查询所有开启的拼团活动
        List<CombinationActivityDO> activityList = combinationActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) { // 时排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 查找是否有其它活动，选择了该产品
        List<CombinationActivityDO> matchActivityList = filterList(activityList, activity -> ObjectUtil.equal(activity.getId(), spuId));
        if (CollUtil.isNotEmpty(matchActivityList)) {
            throw exception(COMBINATION_ACTIVITY_SPU_CONFLICTS);
        }
    }

    /**
     * 校验拼团商品是否都存在
     *
     * @param spuId    商品 SPU 编号
     * @param products 拼团商品
     */
    private void validateProductExists(Long spuId, List<CombinationProductBaseVO> products) {
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
    public void updateCombinationActivity(CombinationActivityUpdateReqVO updateReqVO) {
        // 校验存在
        CombinationActivityDO activityDO = validateCombinationActivityExists(updateReqVO.getId());
        // 校验状态
        if (ObjectUtil.equal(activityDO.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE_NOT_UPDATE);
        }
        // 校验商品冲突
        validateProductConflict(updateReqVO.getSpuId(), updateReqVO.getId());
        // 校验商品是否存在
        validateProductExists(updateReqVO.getSpuId(), updateReqVO.getProducts());

        // 更新活动
        CombinationActivityDO updateObj = CombinationActivityConvert.INSTANCE.convert(updateReqVO);
        combinationActivityMapper.updateById(updateObj);
        // 更新商品
        updateCombinationProduct(updateObj, updateReqVO.getProducts());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeCombinationActivityById(Long id) {
        // 校验活动是否存在
        CombinationActivityDO activity = validateCombinationActivityExists(id);
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE_NOT_UPDATE);
        }

        // 关闭活动
        combinationActivityMapper.updateById(new CombinationActivityDO().setId(id)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
    }

    /**
     * 更新拼团商品
     *
     * @param activity 拼团活动
     * @param products 该活动的最新商品配置
     */
    private void updateCombinationProduct(CombinationActivityDO activity, List<CombinationProductBaseVO> products) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<CombinationProductDO> newList = CombinationActivityConvert.INSTANCE.convertList(products, activity);
        List<CombinationProductDO> oldList = combinationProductMapper.selectListByActivityIds(CollUtil.newArrayList(activity.getId()));
        List<List<CombinationProductDO>> diffList = CollectionUtils.diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            combinationProductMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            combinationProductMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            combinationProductMapper.deleteBatchIds(CollectionUtils.convertList(diffList.get(2), CombinationProductDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCombinationActivity(Long id) {
        // 校验存在
        CombinationActivityDO activity = validateCombinationActivityExists(id);
        // 校验状态
        if (CommonStatusEnum.isEnable(activity.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除
        combinationActivityMapper.deleteById(id);
    }

    @Override
    public CombinationActivityDO validateCombinationActivityExists(Long id) {
        CombinationActivityDO activityDO = combinationActivityMapper.selectById(id);
        if (activityDO == null) {
            throw exception(COMBINATION_ACTIVITY_NOT_EXISTS);
        }
        return activityDO;
    }

    @Override
    public CombinationActivityDO getCombinationActivity(Long id) {
        return validateCombinationActivityExists(id);
    }

    @Override
    public PageResult<CombinationActivityDO> getCombinationActivityPage(CombinationActivityPageReqVO pageReqVO) {
        return combinationActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CombinationProductDO> getCombinationProductListByActivityIds(Collection<Long> activityIds) {
        return combinationProductMapper.selectListByActivityIds(activityIds);
    }

    @Override
    public List<CombinationActivityDO> getCombinationActivityListByIds(Collection<Long> ids) {
        return combinationActivityMapper.selectList(CombinationActivityDO::getId, ids);
    }

    @Override
    public List<CombinationActivityDO> getCombinationActivityListByCount(Integer count) {
        return combinationActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus(), count);
    }

    @Override
    public PageResult<CombinationActivityDO> getCombinationActivityPage(PageParam pageParam) {
        return combinationActivityMapper.selectPage(pageParam, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public CombinationProductDO selectByActivityIdAndSkuId(Long activityId, Long skuId) {
        return combinationProductMapper.selectOne(
                CombinationProductDO::getActivityId, activityId,
                CombinationProductDO::getSkuId, skuId);
    }

    @Override
    public List<CombinationActivityDO> getCombinationActivityBySpuIdsAndStatusAndDateTimeLt(Collection<Long> spuIds, Integer status, LocalDateTime dateTime) {
        // 1.查询出指定 spuId 的 spu 参加的活动最接近现在的一条记录。多个的话，一个 spuId 对应一个最近的活动编号
        List<Map<String, Object>> spuIdAndActivityIdMaps = combinationActivityMapper.selectSpuIdAndActivityIdMapsBySpuIdsAndStatus(spuIds, status);
        if (CollUtil.isEmpty(spuIdAndActivityIdMaps)) {
            return Collections.emptyList();
        }
        // 2.查询活动详情
        return combinationActivityMapper.selectListByIdsAndDateTimeLt(
                convertSet(spuIdAndActivityIdMaps, map -> MapUtil.getLong(map, "activityId")), dateTime);
    }

}
