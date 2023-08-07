package cn.iocoder.yudao.module.promotion.service.seckill.seckillactivity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillProductMapper;
import cn.iocoder.yudao.module.promotion.service.seckill.seckillconfig.SeckillConfigService;
import cn.iocoder.yudao.module.promotion.util.PromotionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.promotion.util.PromotionUtils.validateProductSkuAllExists;

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
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillActivity(SeckillActivityCreateReqVO createReqVO) {
        // 校验商品秒秒杀时段是否冲突
        validateProductSpuSeckillConflict(createReqVO.getConfigIds(), createReqVO.getSpuId(), null);
        // 获取所选 spu 下的所有 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(CollUtil.newArrayList(createReqVO.getSpuId()));
        // 校验商品 sku 是否存在
        if (skus.size() != createReqVO.getProducts().size()) {
            throw exception(SKU_NOT_EXISTS);
        }

        // 插入秒杀活动
        SeckillActivityDO activity = SeckillActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(createReqVO.getEndTime()))
                .setTotalStock(getSumValue(createReqVO.getProducts(), SeckillProductCreateReqVO::getStock, Integer::sum));
        seckillActivityMapper.insert(activity);
        // 插入商品
        List<SeckillProductDO> products = SeckillActivityConvert.INSTANCE.convertList(createReqVO.getProducts(), activity);
        seckillProductMapper.insertBatch(products);
        return activity.getId();
    }

    private void validateProductSpuSeckillConflict(List<Long> configIds, Long spuId, Long activityId) {
        // 校验秒杀时段是否存在
        seckillConfigService.validateSeckillConfigExists(configIds);
        // 校验商品 spu 是否存在
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(CollUtil.newArrayList(spuId));
        if (CollUtil.isEmpty(spuList)) {
            throw exception(SPU_NOT_EXISTS);
        }
        // 查询所有开启的秒杀活动
        List<SeckillActivityDO> activityDOs = seckillActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) {
            // 更新时移除本活动
            activityDOs.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 过滤出所有 spuId 有交集的活动
        List<SeckillActivityDO> activityDOs1 = convertList(activityDOs, c -> c, s -> ObjectUtil.equal(s.getSpuId(), spuId));
        if (isNotEmpty(activityDOs1)) {
            throw exception(SECKILL_ACTIVITY_SPU_CONFLICTS);
        }
        List<SeckillActivityDO> activityDOs2 = convertList(activityDOs, c -> c, s -> {
            // 判断秒杀时段是否有交集
            return containsAny(s.getConfigIds(), configIds);
        });

        if (isNotEmpty(activityDOs2)) {
            throw exception(SECKILL_TIME_CONFLICTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillActivity(SeckillActivityUpdateReqVO updateReqVO) {
        // 校验存在
        SeckillActivityDO seckillActivity = validateSeckillActivityExists(updateReqVO.getId());
        if (CommonStatusEnum.DISABLE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 校验商品是否冲突
        validateProductSpuSeckillConflict(updateReqVO.getConfigIds(), updateReqVO.getSpuId(), updateReqVO.getId());
        // 获取所选 spu下的所有 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(CollUtil.newArrayList(updateReqVO.getSpuId()));
        // 校验商品 sku 是否存在
        validateProductSkuAllExists(skus, updateReqVO.getProducts(), SeckillProductUpdateReqVO::getSkuId);

        // 更新活动
        SeckillActivityDO updateObj = SeckillActivityConvert.INSTANCE.convert(updateReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(updateReqVO.getEndTime()))
                .setTotalStock(getSumValue(updateReqVO.getProducts(), SeckillProductUpdateReqVO::getStock, Integer::sum));
        seckillActivityMapper.updateById(updateObj);
        // 更新商品
        updateSeckillProduct(updateObj, updateReqVO.getProducts());
    }


    /**
     * 更新秒杀商品
     *
     * @param updateObj 更新的活动
     * @param products  商品配置
     */
    private void updateSeckillProduct(SeckillActivityDO updateObj, List<SeckillProductUpdateReqVO> products) {
        // 默认全部新增
        List<SeckillProductDO> defaultNewList = SeckillActivityConvert.INSTANCE.convertList(products, updateObj);
        // 数据库中的活动商品
        List<SeckillProductDO> oldList = seckillProductMapper.selectListByActivityId(updateObj.getId());
        // 对比老、新两个列表，找出新增、修改、删除的数据
        List<List<SeckillProductDO>> lists = diffList(oldList, defaultNewList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // create
        if (isNotEmpty(lists.get(0))) {
            seckillProductMapper.insertBatch(lists.get(0));
        }
        // update
        if (isNotEmpty(lists.get(1))) {
            seckillProductMapper.updateBatch(lists.get(1));
        }
        // delete
        if (isNotEmpty(lists.get(2))) {
            seckillProductMapper.deleteBatchIds(convertList(lists.get(2), SeckillProductDO::getId));
        }
    }

    @Override
    public void closeSeckillActivity(Long id) {
        // TODO 待验证没使用过
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
        List<SeckillProductDO> productDOs = seckillProductMapper.selectListByActivityId(id);
        Set<Long> convertSet = convertSet(productDOs, SeckillProductDO::getSkuId);
        seckillProductMapper.deleteBatchIds(convertSet);
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
        return validateSeckillActivityExists(id);
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

    @Override
    public List<SeckillProductDO> getSeckillProductListByActivityId(Collection<Long> ids) {
        return seckillProductMapper.selectListByActivityId(ids);
    }

}
