package cn.iocoder.yudao.module.promotion.service.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationProductMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationRecordMapper;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

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
public class CombinationServiceImpl implements CombinationActivityService, CombinationRecordService {

    @Resource
    private CombinationActivityMapper combinationActivityMapper;
    @Resource
    private CombinationRecordMapper recordMapper;
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
        validateProductSkuExistence(createReqVO.getProducts(), skus, CombinationProductCreateReqVO::getSkuId);

        // TODO 艿艿 有个小问题：现在有活动时间和限制时长，活动时间的结束时间早于设置的限制时间怎么算状态比如：
        //  活动时间 2023-08-05 15:00:00 - 2023-08-05 15:20:00 限制时长 2小时，那么活动时间结束就结束还是加时到满两小时
        // 插入拼团活动
        CombinationActivityDO activityDO = CombinationActivityConvert.INSTANCE.convert(createReqVO);
        // TODO 营销相关属性初始化
        activityDO.setTotalNum(0);
        activityDO.setSuccessNum(0);
        activityDO.setOrderUserCount(0);
        activityDO.setVirtualGroup(0);
        activityDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        combinationActivityMapper.insert(activityDO);
        // 插入商品
        List<CombinationProductDO> productDOs = CombinationActivityConvert.INSTANCE.complementList(createReqVO.getProducts(), activityDO);
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
        validateProductSkuExistence(updateReqVO.getProducts(), skus, CombinationProductUpdateReqVO::getSkuId);

        // 更新
        CombinationActivityDO updateObj = CombinationActivityConvert.INSTANCE.convert(updateReqVO);
        combinationActivityMapper.updateById(updateObj);
        // 更新商品
        updateCombinationProduct(updateObj, updateReqVO.getProducts());
    }

    /**
     * 更新秒杀商品
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
        // 分化数据
        Map<String, List<CombinationProductDO>> data = CollectionUtils.convertCDUMap(convertSet1, convertSet, mapData -> {
            HashMap<String, List<CombinationProductDO>> cdu = MapUtil.newHashMap(3);
            MapUtils.findAndThen(mapData, "create", list -> {
                cdu.put("create", CombinationActivityConvert.INSTANCE.complementList(CollectionUtils.filterList(products, item -> list.contains(item.getSkuId())), updateObj));
            });
            MapUtils.findAndThen(mapData, "delete", list -> {
                cdu.put("create", CollectionUtils.filterList(combinationProductDOs, item -> list.contains(item.getSkuId())));
            });
            MapUtils.findAndThen(mapData, "update", list -> {
                cdu.put("update", CombinationActivityConvert.INSTANCE.complementList(combinationProductDOs,
                        CollectionUtils.filterList(products, item -> list.contains(item.getSkuId())), updateObj));
            });
            return cdu;
        });

        // 执行增删改
        MapUtils.findAndThen(data, "create", item -> combinationProductMapper.insertBatch(item));
        MapUtils.findAndThen(data, "delete", item -> combinationProductMapper.deleteBatchIds(
                CollectionUtils.convertSet(item, CombinationProductDO::getId)));
        MapUtils.findAndThen(data, "update", item -> combinationProductMapper.updateBatch(item));
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
    public List<CombinationProductDO> getProductsByActivityIds(Collection<Long> ids) {
        return combinationProductMapper.selectListByActivityIds(ids);
    }

    @Override
    public void updateRecordStatusByUserIdAndOrderId(Long userId, Long orderId, Integer status) {
        // 校验拼团是否存在
        CombinationRecordDO recordDO = validateCombinationRecord(userId, orderId);

        // 更新状态
        recordDO.setStatus(status);
        recordMapper.updateById(recordDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecordStatusAndStartTimeByUserIdAndOrderId(Long userId, Long orderId, Integer status, LocalDateTime startTime) {
        CombinationRecordDO recordDO = validateCombinationRecord(userId, orderId);
        // 更新状态
        recordDO.setStatus(status);
        // 更新开始时间
        recordDO.setStartTime(startTime);
        recordMapper.updateById(recordDO);

        // 更新拼团参入人数
        List<CombinationRecordDO> recordDOs = recordMapper.selectListByHeadIdAndStatus(recordDO.getHeadId(), status);
        if (CollUtil.isNotEmpty(recordDOs)) {
            recordDOs.forEach(item -> {
                item.setUserCount(recordDOs.size());
                // 校验拼团是否满足要求
                if (ObjectUtil.equal(recordDOs.size(), recordDO.getUserSize())) {
                    item.setStatus(CombinationRecordStatusEnum.SUCCESS.getStatus());
                }
            });
        }
        recordMapper.updateBatch(recordDOs);
    }

    private CombinationRecordDO validateCombinationRecord(Long userId, Long orderId) {
        // 校验拼团是否存在
        CombinationRecordDO recordDO = recordMapper.selectRecord(userId, orderId);
        if (recordDO == null) {
            throw exception(COMBINATION_RECORD_NOT_EXISTS);
        }
        return recordDO;
    }

    @Override
    public void createRecord(CombinationRecordCreateReqDTO reqDTO) {
        // 校验拼团活动
        CombinationActivityDO activity = validateCombinationActivityExists(reqDTO.getActivityId());
        // 需要校验下，它当前是不是已经参加了该拼团；
        CombinationRecordDO recordDO = recordMapper.selectRecord(reqDTO.getUserId(), reqDTO.getOrderId());
        if (recordDO != null) {
            throw exception(COMBINATION_RECORD_EXISTS);
        }
        // 父拼团是否存在,是否已经满了
        if (reqDTO.getHeadId() != null) {
            CombinationRecordDO recordDO1 = recordMapper.selectRecordByHeadId(reqDTO.getHeadId(), reqDTO.getActivityId(), CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
            if (recordDO1 == null) {
                throw exception(COMBINATION_RECORD_HEAD_NOT_EXISTS);
            }
            // 校验拼团是否满足要求
            if (ObjectUtil.equal(recordDO1.getUserCount(), recordDO1.getUserSize())) {
                throw exception(COMBINATION_RECORD_USER_FULL);
            }
        }

        CombinationRecordDO record = CombinationActivityConvert.INSTANCE.convert(reqDTO);
        if (reqDTO.getHeadId() == null) {
            record.setHeadId(reqDTO.getUserId());
        }
        record.setVirtualGroup(false);
        // TODO @puhui999：过期时间，应该是 Date 哈；
        record.setExpireTime(activity.getLimitDuration());
        record.setUserSize(activity.getUserSize());
        recordMapper.insert(record);
    }

    @Override
    public CombinationRecordDO getRecord(Long userId, Long orderId) {
        return validateCombinationRecord(userId, orderId);
    }

    /**
     * APP 端获取开团记录
     *
     * @return 开团记录
     */
    public List<CombinationRecordDO> getRecordListByStatus(Integer status) {
        return recordMapper.selectListByStatus(status);
    }

}
