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
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateStatusReqDTO;
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
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.promotion.util.PromotionUtils.validateProductSkuAllExists;

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
    @Transactional(rollbackFor = Exception.class)
    public Long createCombinationActivity(CombinationActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateProductCombinationConflict(createReqVO.getSpuId(), null);
        // 获取所选 spu下的所有 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(CollectionUtil.newArrayList(createReqVO.getSpuId()));
        // 校验商品 sku 是否存在
        validateProductSkuAllExists(skus, createReqVO.getProducts(), CombinationProductCreateReqVO::getSkuId);

        // 插入拼团活动
        CombinationActivityDO activityDO = CombinationActivityConvert.INSTANCE.convert(createReqVO);
        // TODO 营销相关属性初始化 拼团成功更新相关属性
        activityDO.setTotalNum(0);
        activityDO.setSuccessNum(0);
        activityDO.setOrderUserCount(0);
        activityDO.setVirtualGroup(0);
        activityDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        combinationActivityMapper.insert(activityDO);
        // 插入商品
        List<CombinationProductDO> productDOs = CombinationActivityConvert.INSTANCE.convertList(createReqVO.getProducts(), activityDO);
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
    @Transactional(rollbackFor = Exception.class)
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
        validateProductSkuAllExists(skus, updateReqVO.getProducts(), CombinationProductUpdateReqVO::getSkuId);

        // 更新
        CombinationActivityDO updateObj = CombinationActivityConvert.INSTANCE.convert(updateReqVO);
        combinationActivityMapper.updateById(updateObj);
        // 更新商品
        updateCombinationProduct(updateObj, updateReqVO.getProducts());
    }

    /**
     * 更新拼团商品
     *
     * @param activity 拼团活动
     * @param products 该活动的最新商品配置
     */
    private void updateCombinationProduct(CombinationActivityDO activity, List<CombinationProductUpdateReqVO> products) {
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
    public List<CombinationProductDO> getCombinationProductsByActivityIds(Collection<Long> ids) {
        return combinationProductMapper.selectListByActivityIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCombinationRecordStatusByUserIdAndOrderId(CombinationRecordUpdateStatusReqDTO reqDTO) {
        // 校验拼团是否存在
        CombinationRecordDO recordDO = validateCombinationRecord(reqDTO.getUserId(), reqDTO.getOrderId());

        // 更新状态
        recordDO.setStatus(reqDTO.getStatus());
        recordMapper.updateById(recordDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCombinationRecordStatusAndStartTimeByUserIdAndOrderId(CombinationRecordUpdateStatusReqDTO reqDTO) {
        CombinationRecordDO recordDO = validateCombinationRecord(reqDTO.getUserId(), reqDTO.getOrderId());
        // 更新状态
        recordDO.setStatus(reqDTO.getStatus());
        // 更新开始时间
        recordDO.setStartTime(reqDTO.getStartTime());
        recordMapper.updateById(recordDO);

        // 更新拼团参入人数
        List<CombinationRecordDO> recordDOs = recordMapper.selectListByHeadIdAndStatus(recordDO.getHeadId(), reqDTO.getStatus());
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
        CombinationRecordDO recordDO = recordMapper.selectByUserIdAndOrderId(userId, orderId);
        if (recordDO == null) {
            throw exception(COMBINATION_RECORD_NOT_EXISTS);
        }
        return recordDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO) {
        // 1.1 校验拼团活动
        CombinationActivityDO activity = validateCombinationActivityExists(reqDTO.getActivityId());
        // 1.2 需要校验下，他当前是不是已经参加了该拼团；
        CombinationRecordDO recordDO = recordMapper.selectByUserIdAndOrderId(reqDTO.getUserId(), reqDTO.getOrderId());
        if (recordDO != null) {
            throw exception(COMBINATION_RECORD_EXISTS);
        }
        // 1.3 校验用户是否参加了其它拼团
        List<CombinationRecordDO> recordDOList = recordMapper.selectListByUserIdAndStatus(reqDTO.getUserId(), CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
        if (CollUtil.isNotEmpty(recordDOList)) {
            throw exception(COMBINATION_RECORD_FAILED_HAVE_JOINED);
        }
        // 1.4 校验当前活动是否过期
        if (LocalDateTime.now().isAfter(activity.getEndTime())) {
            throw exception(COMBINATION_RECORD_FAILED_TIME_END);
        }
        // 1.5 父拼团是否存在,是否已经满了
        if (reqDTO.getHeadId() != null) {
            // 查询进行中的父拼团
            CombinationRecordDO recordDO1 = recordMapper.selectOneByHeadId(reqDTO.getHeadId(), CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
            if (recordDO1 == null) {
                throw exception(COMBINATION_RECORD_HEAD_NOT_EXISTS);
            }
            // 校验拼团是否满足要求
            if (ObjectUtil.equal(recordDO1.getUserCount(), recordDO1.getUserSize())) {
                throw exception(COMBINATION_RECORD_USER_FULL);
            }
        }

        // 2. 创建拼团记录
        CombinationRecordDO record = CombinationActivityConvert.INSTANCE.convert(reqDTO);
        record.setVirtualGroup(false);
        // TODO @puhui999：过期时间，应该是 Date 哈；
        record.setExpireTime(activity.getLimitDuration());
        record.setUserSize(activity.getUserSize());
        recordMapper.insert(record);
    }

    @Override
    public CombinationRecordDO getCombinationRecord(Long userId, Long orderId) {
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

    public List<CombinationRecordDO> getRecordListByUserIdAndActivityId(Long userId, Long activityId) {
        return recordMapper.selectListByUserIdAndActivityId(userId, activityId);
    }

    @Override
    public void validateCombinationLimitCount(Long activityId, Integer count, Integer sumCount) {
        // 1.1 校验拼团活动
        CombinationActivityDO activity = validateCombinationActivityExists(activityId);
        // 校验是否达到限购总限购标准
        if ((sumCount + count) > activity.getTotalLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED);
        }
        // 单次购买是否达到限购标准
        if (count > activity.getSingleLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED);
        }
    }

}
