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

        // TODO 艿艿 有个小问题：现在有活动时间和限制时长，活动时间的结束时间早于设置的限制时间怎么算状态比如：
        //  活动时间 2023-08-05 15:00:00 - 2023-08-05 15:20:00 限制时长 2小时，那么活动时间结束就结束还是加时到满两小时
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
     * @param updateObj 更新的活动
     * @param products  商品配置
     */
    private void updateCombinationProduct(CombinationActivityDO updateObj, List<CombinationProductUpdateReqVO> products) {
        // 默认全部新增
        List<CombinationProductDO> defaultNewList = CombinationActivityConvert.INSTANCE.convertList(products, updateObj);
        // 数据库中的老数据
        List<CombinationProductDO> oldList = combinationProductMapper.selectListByActivityIds(CollUtil.newArrayList(updateObj.getId()));
        List<List<CombinationProductDO>> lists = CollectionUtils.diffList(oldList, defaultNewList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // create
        if (CollUtil.isNotEmpty(lists.get(0))) {
            combinationProductMapper.insertBatch(lists.get(0));
        }
        // update
        if (CollUtil.isNotEmpty(lists.get(1))) {
            combinationProductMapper.updateBatch(lists.get(1));
        }
        // delete
        if (CollUtil.isNotEmpty(lists.get(2))) {
            combinationProductMapper.deleteBatchIds(CollectionUtils.convertList(lists.get(2), CombinationProductDO::getId));
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
    public void updateCombinationRecordStatusByUserIdAndOrderId(Long userId, Long orderId, Integer status) {
        // 校验拼团是否存在
        CombinationRecordDO recordDO = validateCombinationRecord(userId, orderId);

        // 更新状态
        recordDO.setStatus(status);
        recordMapper.updateById(recordDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCombinationRecordStatusAndStartTimeByUserIdAndOrderId(Long userId, Long orderId, Integer status, LocalDateTime startTime) {
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
    public void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO) {
        // 1.1 校验拼团活动
        CombinationActivityDO activity = validateCombinationActivityExists(reqDTO.getActivityId());
        // 1.2 需要校验下，他当前是不是已经参加了该拼团；
        CombinationRecordDO recordDO = recordMapper.selectRecord(reqDTO.getUserId(), reqDTO.getOrderId());
        if (recordDO != null) {
            throw exception(COMBINATION_RECORD_EXISTS);
        }
        // 1.3 父拼团是否存在,是否已经满了
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
        // TODO @puhui999：应该还有一些校验，后续补噶；例如说，一个团，自己已经参与进去了，不能再参与进去；

        // 2. 创建拼团记录
        CombinationRecordDO record = CombinationActivityConvert.INSTANCE.convert(reqDTO);
        if (reqDTO.getHeadId() == null) {
            // TODO @puhui999：不是自己呀；headId 是父团长的 CombinationRecordDO.id 哈
            record.setHeadId(reqDTO.getUserId());
        }
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

}
