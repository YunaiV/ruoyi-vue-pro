package cn.iocoder.yudao.module.promotion.service.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordReqPageVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationRecordMapper;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.trade.api.order.TradeOrderApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.afterNow;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.beforeNow;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

// TODO 芋艿：等拼团记录做完，完整 review 下

/**
 * 拼团记录 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Slf4j
@Validated
public class CombinationRecordServiceImpl implements CombinationRecordService {

    @Resource
    private CombinationActivityService combinationActivityService;
    @Resource
    private CombinationRecordMapper combinationRecordMapper;

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Resource
    @Lazy
    private TradeOrderApi tradeOrderApi;

    // TODO @芋艿：在详细预览下；
    @Override
    public KeyValue<CombinationActivityDO, CombinationProductDO> validateCombinationRecord(
            Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        // 1. 校验拼团活动是否存在
        CombinationActivityDO activity = combinationActivityService.validateCombinationActivityExists(activityId);
        // 1.1 校验活动是否开启
        if (ObjUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE);
        }
        // 1.2. 校验活动开始时间
        if (afterNow(activity.getStartTime())) {
            throw exception(COMBINATION_RECORD_FAILED_TIME_NOT_START);
        }
        // 1.3 校验是否超出单次限购数量
        if (count > activity.getSingleLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED);
        }

        // 2. 父拼团是否存在,是否已经满了
        if (headId != null) {
            // 2.1. 查询进行中的父拼团
            CombinationRecordDO record = combinationRecordMapper.selectByHeadId(headId, CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
            if (record == null) {
                throw exception(COMBINATION_RECORD_HEAD_NOT_EXISTS);
            }
            // 2.2. 校验拼团是否已满
            if (ObjUtil.equal(record.getUserCount(), record.getUserSize())) {
                throw exception(COMBINATION_RECORD_USER_FULL);
            }
            // 2.3 校验拼团是否过期（有父拼团的时候只校验父拼团的过期时间）
            if (beforeNow(record.getExpireTime())) {
                throw exception(COMBINATION_RECORD_FAILED_TIME_END);
            }
        } else {
            // 3. 校验当前活动是否结束(自己是父拼团的时候才校验活动是否结束)
            if (beforeNow(activity.getEndTime())) {
                throw exception(COMBINATION_RECORD_FAILED_TIME_END);
            }
        }

        // 4.1 校验活动商品是否存在
        CombinationProductDO product = combinationActivityService.selectByActivityIdAndSkuId(activityId, skuId);
        if (product == null) {
            throw exception(COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 4.2 校验 sku 是否存在
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 4.3 校验库存是否充足
        if (count > sku.getStock()) {
            throw exception(COMBINATION_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 6.1 校验是否有拼团记录
        List<CombinationRecordDO> recordList = combinationRecordMapper.selectListByUserIdAndActivityId(userId, activityId);
        recordList.removeIf(record -> CombinationRecordStatusEnum.isFailed(record.getStatus())); // 取消的订单，不算数
        if (CollUtil.isEmpty(recordList)) { // 如果为空，说明可以参与，直接返回
            return new KeyValue<>(activity, product);
        }
        // 6.2 校验用户是否有该活动正在进行的拼团
        CombinationRecordDO inProgressRecord = findFirst(recordList,
                record -> CombinationRecordStatusEnum.isInProgress(record.getStatus()));
        if (inProgressRecord != null) {
            throw exception(COMBINATION_RECORD_FAILED_HAVE_JOINED);
        }
        // 6.3 校验是否超出总限购数量
        Integer sumValue = getSumValue(recordList, CombinationRecordDO::getCount, Integer::sum);
        if (sumValue != null && sumValue + count > activity.getTotalLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED);
        }
        return new KeyValue<>(activity, product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CombinationRecordDO createCombinationRecord(CombinationRecordCreateReqDTO reqDTO) {
        // 1. 校验拼团活动
        KeyValue<CombinationActivityDO, CombinationProductDO> keyValue = validateCombinationRecord(reqDTO.getUserId(),
                reqDTO.getActivityId(), reqDTO.getHeadId(), reqDTO.getSkuId(), reqDTO.getCount());

        // 2. 组合数据创建拼团记录
        MemberUserRespDTO user = memberUserApi.getUser(reqDTO.getUserId());
        ProductSpuRespDTO spu = productSpuApi.getSpu(reqDTO.getSpuId());
        ProductSkuRespDTO sku = productSkuApi.getSku(reqDTO.getSkuId());
        CombinationRecordDO record = CombinationActivityConvert.INSTANCE.convert(reqDTO, keyValue.getKey(), user, spu, sku);
        // 2.1. 如果是团长需要设置 headId 为 CombinationRecordDO#HEAD_ID_GROUP
        if (record.getHeadId() == null) {
            record.setStartTime(LocalDateTime.now())
                    .setExpireTime(keyValue.getKey().getStartTime().plusHours(keyValue.getKey().getLimitDuration()))
                    .setHeadId(CombinationRecordDO.HEAD_ID_GROUP);
        } else {
            // 2.2.有团长的情况下需要设置开始时间和过期时间为团长的
            CombinationRecordDO headRecord = combinationRecordMapper.selectByHeadId(record.getHeadId(),
                    CombinationRecordStatusEnum.IN_PROGRESS.getStatus()); // 查询进行中的父拼团
            record.setStartTime(headRecord.getStartTime()).setExpireTime(headRecord.getExpireTime());
        }
        combinationRecordMapper.insert(record);

        // 3. 更新拼团记录
        if (ObjUtil.notEqual(CombinationRecordDO.HEAD_ID_GROUP, record.getHeadId())) {
            updateCombinationRecordWhenCreate(reqDTO.getHeadId(), keyValue.getKey());
        }
        return record;
    }

    /**
     * 当新增拼团时，更新拼团记录的进展
     *
     * @param headId   团长编号
     * @param activity 活动
     */
    private void updateCombinationRecordWhenCreate(Long headId, CombinationActivityDO activity) {
        // 1. 团长 + 团员
        List<CombinationRecordDO> records = getCombinationRecordListByHeadId(headId);
        if (CollUtil.isEmpty(records)) {
            return;
        }
        CombinationRecordDO headRecord = combinationRecordMapper.selectById(headId);

        // 2. 批量更新记录
        List<CombinationRecordDO> updateRecords = new ArrayList<>();
        records.add(headRecord); // 加入团长，团长也需要更新
        boolean isFull = records.size() >= activity.getUserSize();
        LocalDateTime now = LocalDateTime.now();
        records.forEach(item -> {
            CombinationRecordDO updateRecord = new CombinationRecordDO();
            updateRecord.setId(item.getId()).setUserCount(records.size());
            if (isFull) {
                updateRecord.setStatus(CombinationRecordStatusEnum.SUCCESS.getStatus());
                updateRecord.setEndTime(now);
            }
            updateRecords.add(updateRecord);
        });
        combinationRecordMapper.updateBatch(updateRecords);
    }

    @Override
    public CombinationRecordDO getCombinationRecord(Long userId, Long orderId) {
        return combinationRecordMapper.selectByUserIdAndOrderId(userId, orderId);
    }

    @Override
    public CombinationValidateJoinRespDTO validateJoinCombination(Long userId, Long activityId, Long headId,
                                                                  Long skuId, Integer count) {
        KeyValue<CombinationActivityDO, CombinationProductDO> keyValue = validateCombinationRecord(userId, activityId,
                headId, skuId, count);
        return new CombinationValidateJoinRespDTO().setActivityId(keyValue.getKey().getId())
                .setName(keyValue.getKey().getName()).setCombinationPrice(keyValue.getValue().getCombinationPrice());
    }

    @Override
    public Long getCombinationRecordCount(@Nullable Integer status, @Nullable Boolean virtualGroup, @Nullable Long headId) {
        return combinationRecordMapper.selectCountByHeadAndStatusAndVirtualGroup(status, virtualGroup, headId);
    }

    @Override
    public Long getCombinationUserCount() {
        return combinationRecordMapper.selectUserCount();
    }

    @Override
    public List<CombinationRecordDO> getLatestCombinationRecordList(int count) {
        return combinationRecordMapper.selectLatestList(count);
    }

    @Override
    public List<CombinationRecordDO> getHeadCombinationRecordList(Long activityId, Integer status, Integer count) {
        return combinationRecordMapper.selectListByActivityIdAndStatusAndHeadId(activityId, status,
                CombinationRecordDO.HEAD_ID_GROUP, count);
    }

    @Override
    public CombinationRecordDO getCombinationRecordById(Long id) {
        return combinationRecordMapper.selectById(id);
    }

    @Override
    public List<CombinationRecordDO> getCombinationRecordListByHeadId(Long headId) {
        return combinationRecordMapper.selectList(CombinationRecordDO::getHeadId, headId);
    }

    @Override
    public PageResult<CombinationRecordDO> getCombinationRecordPage(CombinationRecordReqPageVO pageVO) {
        return combinationRecordMapper.selectPage(pageVO);
    }

    @Override
    public Map<Long, Integer> getCombinationRecordCountMapByActivity(Collection<Long> activityIds,
                                                                     @Nullable Integer status, @Nullable Long headId) {
        return combinationRecordMapper.selectCombinationRecordCountMapByActivityIdAndStatusAndHeadId(activityIds, status, headId);
    }

    @Override
    public CombinationRecordDO getCombinationRecordByIdAndUser(Long userId, Long id) {
        return combinationRecordMapper.selectOne(CombinationRecordDO::getUserId, userId, CombinationRecordDO::getId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCombinationRecord(Long userId, Long id, Long headId) {
        // 删除记录
        combinationRecordMapper.deleteById(id);

        // 需要更新的记录
        List<CombinationRecordDO> updateRecords = new ArrayList<>();
        // 如果它是团长，则顺序（下单时间）继承
        if (Objects.equals(headId, CombinationRecordDO.HEAD_ID_GROUP)) { // 情况一：团长
            // 团员
            List<CombinationRecordDO> list = getCombinationRecordListByHeadId(id);
            if (CollUtil.isEmpty(list)) {
                return;
            }
            // 按照创建时间升序排序
            list.sort(Comparator.comparing(CombinationRecordDO::getCreateTime)); // 影响原 list
            CombinationRecordDO newHead = list.get(0); // 新团长继位
            list.forEach(item -> {
                CombinationRecordDO recordDO = new CombinationRecordDO();
                recordDO.setId(item.getId());
                if (ObjUtil.equal(item.getId(), newHead.getId())) { // 新团长
                    recordDO.setHeadId(CombinationRecordDO.HEAD_ID_GROUP);
                } else {
                    recordDO.setHeadId(newHead.getId());
                }
                recordDO.setUserCount(list.size());
                updateRecords.add(recordDO);
            });
        } else { // 情况二：团员
            // 团长
            CombinationRecordDO recordHead = combinationRecordMapper.selectById(headId);
            // 团员
            List<CombinationRecordDO> records = getCombinationRecordListByHeadId(headId);
            if (CollUtil.isEmpty(records)) {
                return;
            }
            records.add(recordHead); // 加入团长，团长数据也需要更新
            records.forEach(item -> {
                CombinationRecordDO recordDO = new CombinationRecordDO();
                recordDO.setId(item.getId());
                recordDO.setUserCount(records.size());
                updateRecords.add(recordDO);
            });
        }

        // 更新拼团记录
        combinationRecordMapper.updateBatch(updateRecords);
    }

    @Override
    public KeyValue<Integer, Integer> expireCombinationRecord() {
        // 1. 获取所有正在进行中的过期的父拼团
        List<CombinationRecordDO> headExpireRecords = combinationRecordMapper.selectListByHeadIdAndStatusAndExpireTimeLt(
                CombinationRecordDO.HEAD_ID_GROUP, CombinationRecordStatusEnum.IN_PROGRESS.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(headExpireRecords)) {
            return new KeyValue<>(0, 0);
        }

        // 2. 获取拼团活动
        List<CombinationActivityDO> activities = combinationActivityService.getCombinationActivityListByIds(
                convertSet(headExpireRecords, CombinationRecordDO::getActivityId));
        Map<Long, CombinationActivityDO> activityMap = convertMap(activities, CombinationActivityDO::getId);

        // 3. 逐个处理拼团，过期 or 虚拟成团
        KeyValue<Integer, Integer> keyValue = new KeyValue<>(0, 0); // 统计过期拼团和虚拟成团
        for (CombinationRecordDO record : headExpireRecords) {
            try {
                CombinationActivityDO activity = activityMap.get(record.getActivityId());
                if (activity == null || !activity.getVirtualGroup()) { // 取不到活动的或者不是虚拟拼团的
                    // 3.1. 处理过期的拼团
                    getSelf().handleExpireRecord(record);
                    keyValue.setKey(keyValue.getKey() + 1);
                } else {
                    // 3.2. 处理虚拟成团
                    getSelf().handleVirtualGroupRecord(record);
                    keyValue.setValue(keyValue.getValue() + 1);
                }
            } catch (Exception ignored) { // 处理异常继续循环
                log.error("[expireCombinationRecord][record({}) 处理异常，请进行处理！record 数据是：{}]",
                        record.getId(), JsonUtils.toJsonString(record));
            }
        }
        return keyValue;
    }

    /**
     * 处理过期拼团
     *
     * @param headRecord 过期拼团团长记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleExpireRecord(CombinationRecordDO headRecord) {
        // 1. 更新拼团记录
        List<CombinationRecordDO> headAndRecords = updateBatchCombinationRecords(headRecord,
                CombinationRecordStatusEnum.FAILED);
        // 2. 订单取消
        headAndRecords.forEach(item -> tradeOrderApi.cancelPaidOrder(item.getUserId(), item.getOrderId()));
    }

    /**
     * 处理虚拟拼团
     *
     * @param headRecord 虚拟成团团长记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleVirtualGroupRecord(CombinationRecordDO headRecord) {
        // 1. 团员补齐
        combinationRecordMapper.insertBatch(CombinationActivityConvert.INSTANCE.convertVirtualRecordList(headRecord));
        // 2. 更新拼团记录
        updateBatchCombinationRecords(headRecord, CombinationRecordStatusEnum.SUCCESS);
    }

    /**
     * 更新拼团记录
     *
     * @param headRecord 团长记录
     * @param status     状态-拼团失败 FAILED 成功 SUCCESS
     * @return 整团记录（包含团长和团成员）
     */
    private List<CombinationRecordDO> updateBatchCombinationRecords(CombinationRecordDO headRecord, CombinationRecordStatusEnum status) {
        // 1. 查询团成员（包含团长）
        List<CombinationRecordDO> records = combinationRecordMapper.selectListByHeadId(headRecord.getId());
        records.add(headRecord);// 把团长加进去

        // 2. 批量更新拼团记录 status 和 endTime
        List<CombinationRecordDO> updateRecords = new ArrayList<>(records.size());
        LocalDateTime now = LocalDateTime.now();
        records.forEach(item -> {
            CombinationRecordDO updateRecord = new CombinationRecordDO().setId(item.getId())
                    .setStatus(status.getStatus()).setEndTime(now);
            if (CombinationRecordStatusEnum.isSuccess(status.getStatus())) { // 虚拟成团完事更改状态成功后还需要把参与人数修改为成团需要人数
                updateRecord.setUserCount(updateRecord.getUserSize());
            }
            updateRecords.add(updateRecord);
        });
        combinationRecordMapper.updateBatch(updateRecords);
        return records;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private CombinationRecordServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
