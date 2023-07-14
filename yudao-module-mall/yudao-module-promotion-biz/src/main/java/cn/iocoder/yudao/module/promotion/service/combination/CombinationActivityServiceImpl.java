package cn.iocoder.yudao.module.promotion.service.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityExportReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.combinationactivity.CombinationActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.combinationactivity.CombinationProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.promotion.util.PromotionUtils.validateProductSkuExistence;

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
    public Long createCombinationActivity(CombinationActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateProductCombinationConflict(createReqVO.getSpuId(), null);
        // 获取所选 spu下的所有 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(CollectionUtil.newArrayList(createReqVO.getSpuId()));
        // 校验商品 sku 是否存在
        validateProductSkuExistence(skus, createReqVO.getProducts(), CombinationProductCreateReqVO::getSkuId);

        // TODO 艿艿 有个小问题：现在有活动时间和限制时长，活动时间的结束时间早于设置的限制时间怎么算状态比如：
        //  活动时间 2023-08-05 15:00:00 - 2023-08-05 15:20:00 限制时长 2小时，那么活动时间结束就结束还是加时到满两小时
        // 插入拼团活动
        CombinationActivityDO activityDO = CombinationActivityConvert.INSTANCE.convert(createReqVO);
        // TODO 营销相关属性初始化
        activityDO.setUserSize(0);
        activityDO.setTotalNum(0);
        activityDO.setSuccessNum(0);
        activityDO.setOrderUserCount(0);
        activityDO.setVirtualGroup(0);
        activityDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        combinationActivityMapper.insert(activityDO);
        // 插入商品
        List<CombinationProductDO> productDOs = CombinationActivityConvert.INSTANCE.convertList(activityDO, createReqVO.getProducts());
        combinationProductMapper.insertBatch(productDOs);
        // 返回
        return activityDO.getId();
    }

    private void validateProductCombinationConflict(Long spuId, Long activityId) {
        // 校验商品 spu 是否存在
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(CollUtil.newArrayList(spuId));
        if (CollUtil.isEmpty(spuList)) {
            throw exception(SPU_NOT_EXISTS);
        }
        // 查询所有开启的拼团活动
        List<CombinationActivityDO> activityDOs = combinationActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 更新时排除自己
        if (activityId != null) {
            activityDOs.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 过滤出所有 spuIds 有交集的活动
        List<CombinationActivityDO> doList = CollectionUtils.convertList(activityDOs, c -> c, s -> ObjectUtil.equal(s.getId(), spuId));
        if (CollUtil.isNotEmpty(doList)) {
            throw exception(COMBINATION_ACTIVITY_SPU_CONFLICTS);
        }
    }

    @Override
    public void updateCombinationActivity(CombinationActivityUpdateReqVO updateReqVO) {
        // 校验存在
        CombinationActivityDO activityDO = validateCombinationActivityExists(updateReqVO.getId());
        // 校验状态
        if (ObjectUtil.equal(activityDO.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE);
        }
        // 校验商品冲突
        validateProductCombinationConflict(updateReqVO.getSpuId(), updateReqVO.getId());
        // 获取所选 spu下的所有 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(CollectionUtil.newArrayList(updateReqVO.getSpuId()));
        // 校验商品 sku 是否存在
        validateProductSkuExistence(skus, updateReqVO.getProducts(), CombinationProductUpdateReqVO::getSkuId);

        // 更新
        CombinationActivityDO updateObj = CombinationActivityConvert.INSTANCE.convert(updateReqVO);
        combinationActivityMapper.updateById(updateObj);
        // 更新商品
        updateCombinationProduct(updateObj, updateReqVO.getProducts());
    }

    /**
     * 更新秒杀商品
     *  TODO 更新商品要不要封装成通用方法
     *
     * @param updateObj DO
     * @param products  商品配置
     */
    private void updateCombinationProduct(CombinationActivityDO updateObj, List<CombinationProductUpdateReqVO> products) {
        List<CombinationProductDO> combinationProductDOs = combinationProductMapper.selectListByActivityIds(CollUtil.newArrayList(updateObj.getId()));
        // 数据库中的活动商品
        Set<Long> convertSet = CollectionUtils.convertSet(combinationProductDOs, CombinationProductDO::getSkuId);
        // 前端传过来的活动商品
        Set<Long> convertSet1 = CollectionUtils.convertSet(products, CombinationProductUpdateReqVO::getSkuId);
        // 删除后台存在的前端不存在的商品
        List<Long> d = CollectionUtils.filterList(convertSet, item -> !convertSet1.contains(item));
        if (CollUtil.isNotEmpty(d)) {
            combinationProductMapper.deleteBatchIds(d);
        }
        // 前端存在的后端不存在的商品
        List<Long> c = CollectionUtils.filterList(convertSet1, item -> !convertSet.contains(item));
        if (CollUtil.isNotEmpty(c)) {
            List<CombinationProductUpdateReqVO> vos = CollectionUtils.filterList(products, item -> c.contains(item.getSkuId()));
            List<CombinationProductDO> productDOs = CombinationActivityConvert.INSTANCE.convertList(updateObj, vos);
            combinationProductMapper.insertBatch(productDOs);
        }
        // 更新已存在的商品
        List<Long> u = CollectionUtils.filterList(convertSet1, convertSet::contains);
        if (CollUtil.isNotEmpty(u)) {
            List<CombinationProductUpdateReqVO> vos = CollectionUtils.filterList(products, item -> u.contains(item.getSkuId()));
            List<CombinationProductDO> productDOs = CombinationActivityConvert.INSTANCE.convertList1(updateObj, vos, combinationProductDOs);
            combinationProductMapper.updateBatch(productDOs);
        }
    }

    @Override
    public void deleteCombinationActivity(Long id) {
        // 校验存在
        CombinationActivityDO activityDO = validateCombinationActivityExists(id);
        // 校验状态
        if (ObjectUtil.equal(activityDO.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除
        combinationActivityMapper.deleteById(id);
    }

    private CombinationActivityDO validateCombinationActivityExists(Long id) {
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
    public List<CombinationActivityDO> getCombinationActivityList(Collection<Long> ids) {
        return combinationActivityMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CombinationActivityDO> getCombinationActivityPage(CombinationActivityPageReqVO pageReqVO) {
        return combinationActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CombinationActivityDO> getCombinationActivityList(CombinationActivityExportReqVO exportReqVO) {
        return combinationActivityMapper.selectList(exportReqVO);
    }

    @Override
    public List<CombinationProductDO> getProductsByActivityIds(Collection<Long> ids) {
        return combinationProductMapper.selectListByActivityIds(ids);
    }

}
