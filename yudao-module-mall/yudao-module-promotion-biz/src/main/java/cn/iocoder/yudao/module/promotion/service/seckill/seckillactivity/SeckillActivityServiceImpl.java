package cn.iocoder.yudao.module.promotion.service.seckill.seckillactivity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductBaseVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillProductMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import cn.iocoder.yudao.module.promotion.service.seckill.seckillconfig.SeckillConfigService;
import cn.iocoder.yudao.module.promotion.util.PromotionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;

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
    private SeckillConfigService seckillConfigService;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public Long createSeckillActivity(SeckillActivityCreateReqVO createReqVO) {
        // 校验商品秒秒杀时段是否冲突
        validateProductSpuSeckillConflict(createReqVO.getConfigIds(), createReqVO.getSpuIds());
        // 校验商品 sku 是否存在
        validateProductSkuExistence(createReqVO.getSpuIds(), createReqVO.getProducts());

        // 插入秒杀活动
        SeckillActivityDO activity = SeckillActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(createReqVO.getEndTime()));
        seckillActivityMapper.insert(activity);
        // 插入商品
        List<SeckillProductDO> product = SeckillActivityConvert.INSTANCE.convertList(activity, createReqVO.getProducts());
        seckillProductMapper.insertBatch(product);
        return activity.getId();
    }

    private <T extends SeckillProductBaseVO> void validateProductSkuExistence(List<Long> spuIds, List<T> products) {
        // 校验 spu 个数是否相等
        // TODO @puhui999：不用校验 SPU 哈，只校验 sku 对应的 spuId 是否一致；
        Set<Long> convertedSpuIds = CollectionUtils.convertSet(products, T::getSpuId);
        if (ObjectUtil.notEqual(spuIds.size(), convertedSpuIds.size())) {
            throw exception(SKU_NOT_EXISTS);
        }
        // 获取所选 spu下的所有 sku
        // TODO @puhui999：变量可以简单一点；skus
        List<ProductSkuRespDTO> skuRespDTOs = productSkuApi.getSkuListBySpuId(spuIds);
        // 校验 sku 个数是否一致
        Set<Long> skuIdsSet = CollectionUtils.convertSet(products, T::getSkuId);
        Set<Long> skuIdsSet1 = CollectionUtils.convertSet(skuRespDTOs, ProductSkuRespDTO::getId);
        if (ObjectUtil.notEqual(skuIdsSet.size(), skuIdsSet1.size())) {
            throw exception(SKU_NOT_EXISTS);
        }
        // 校验 skuId 是否存在
        if (!skuIdsSet1.containsAll(skuIdsSet) || !skuIdsSet.containsAll(skuIdsSet1)) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    private void validateProductSpuSeckillConflict(List<Long> configIds, List<Long> spuIds) {
        // 校验秒杀时段是否存在
        seckillConfigService.validateSeckillConfigExists(configIds);
        // 校验商品 spu 是否存在
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(spuIds);
        if (ObjectUtil.notEqual(spuIds.size(), spuList.size())) {
            throw exception(SPU_NOT_EXISTS);
        }
        // 查询所有开启的秒杀活动
        List<SeckillActivityDO> activityDOs = seckillActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 过滤出所有 spuIds 有交集的活动
        List<SeckillActivityDO> doList = activityDOs.stream().filter(s -> {
            // 判断 spu 是否有交集
            List<Long> spuIdsClone = ArrayUtil.clone(s.getSpuIds());
            spuIdsClone.retainAll(spuIds);
            if (CollUtil.isEmpty(spuIdsClone)) {
                return false;
            }
            // 判断秒杀时段是否有交集
            List<Long> configIdsClone = ArrayUtil.clone(s.getConfigIds());
            configIdsClone.retainAll(configIds);
            return CollUtil.isNotEmpty(configIdsClone);
        }).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(doList)) {
            throw exception(SECKILL_ACTIVITY_SPU_CONFLICTS);
        }
    }

    @Override
    public void updateSeckillActivity(SeckillActivityUpdateReqVO updateReqVO) {
        // 校验存在
        SeckillActivityDO seckillActivity = validateSeckillActivityExists(updateReqVO.getId());
        if (CommonStatusEnum.ENABLE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 校验商品是否冲突
        validateProductSpuSeckillConflict(updateReqVO.getConfigIds(), updateReqVO.getSpuIds());
        // 校验商品 sku 是否存在
        validateProductSkuExistence(updateReqVO.getSpuIds(), updateReqVO.getProducts());

        // 更新活动
        SeckillActivityDO updateObj = SeckillActivityConvert.INSTANCE.convert(updateReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(updateReqVO.getEndTime()));
        seckillActivityMapper.updateById(updateObj);
        // 更新商品
        //updateSeckillProduct(updateReqVO);
    }


    /**
     * 更新秒杀商品
     * 后台查出的数据和前台查出的数据进行遍历，
     * 1. 对前台数据进行遍历：如果不存在于后台的 sku 中需要新增
     * 2. 对后台数据进行遍历：如果不存在于前台的 sku 中需要删除
     * 3. 最后对当前活动商品全部更新，更新秒杀时段id列表
     *
     * @param updateReqVO 更新的请求VO
     */
    private void updateSeckillProduct(SeckillActivityUpdateReqVO updateReqVO) {
        // TODO puhui999：要不这里简单一点；删除原本的，插入新增的；不做的这么细致
        // TODO puhui999：后续完善
        //List<SeckillProductDO> seckillProductDOs = seckillProductMapper.selectListByActivityId(updateReqVO.getId());
        //List<SeckillProductUpdateReqVO> products = updateReqVO.getProducts();

        ////计算需要删除的数据
        //List<Long> deleteIds = CollectionUtils.convertList(seckillProductDOs, SeckillProductDO::getId,
        //        seckillProductDO -> products.stream()
        //                .noneMatch(product -> SeckillActivityConvert.INSTANCE.isEquals(seckillProductDO, product)));
        //if (CollUtil.isNotEmpty(deleteIds)) {
        //    seckillProductMapper.deleteBatchIds(deleteIds);
        //}
        //
        //// 计算需要新增的数据
        //List<SeckillProductDO> newSeckillProductDOs = CollectionUtils.convertList(products,
        //        product -> SeckillActivityConvert.INSTANCE.convert(product).setActivityId(updateReqVO.getId()));
        //newSeckillProductDOs.removeIf(product -> seckillProductDOs.stream()
        //        .anyMatch(seckillProduct -> SeckillActivityConvert.INSTANCE.isEquals(seckillProduct, product)));
        //if (CollUtil.isNotEmpty(newSeckillProductDOs)) {
        //    seckillProductMapper.insertBatch(newSeckillProductDOs);
        //}

        //全量更新当前活动商品的秒杀时段id列表（timeIds）
        seckillProductMapper.updateTimeIdsByActivityId(updateReqVO.getId(), updateReqVO.getConfigIds());
    }

    @Override
    public void closeSeckillActivity(Long id) {
        // 校验存在
        SeckillActivityDO seckillActivity = this.validateSeckillActivityExists(id);
        if (PromotionActivityStatusEnum.CLOSE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }
        if (PromotionActivityStatusEnum.END.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_END);
        }
        // 更新
        SeckillActivityDO updateObj = new SeckillActivityDO().setId(id).setStatus(PromotionActivityStatusEnum.CLOSE.getStatus());
        seckillActivityMapper.updateById(updateObj);
    }

    @Override
    public void deleteSeckillActivity(Long id) {
        // 校验存在
        SeckillActivityDO seckillActivity = this.validateSeckillActivityExists(id);
        List<Integer> statuses = asList(PromotionActivityStatusEnum.CLOSE.getStatus(), PromotionActivityStatusEnum.END.getStatus());
        if (!statuses.contains(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除
        seckillActivityMapper.deleteById(id);
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
    public List<SeckillActivityDO> getSeckillActivityList(Collection<Long> ids) {
        return seckillActivityMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SeckillActivityDO> getSeckillActivityPage(SeckillActivityPageReqVO pageReqVO) {
        return seckillActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SeckillProductDO> getSeckillProductListByActivityId(Long id) {
        return seckillProductMapper.selectListByActivityId(id);
    }

}
