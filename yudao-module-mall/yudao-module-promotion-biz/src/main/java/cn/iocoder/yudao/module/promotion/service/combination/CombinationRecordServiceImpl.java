package cn.iocoder.yudao.module.promotion.service.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationRecordMapper;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.trade.api.order.TradeOrderApi;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

// TODO 芋艿：等拼团记录做完，完整 review 下

/**
 * 拼团记录 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class CombinationRecordServiceImpl implements CombinationRecordService {

    @Resource
    @Lazy
    private CombinationActivityService combinationActivityService;
    @Resource
    private CombinationRecordMapper recordMapper;

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    @Lazy
    private ProductSpuApi productSpuApi;
    @Resource
    @Lazy
    private ProductSkuApi productSkuApi;
    @Resource
    private TradeOrderApi tradeOrderApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCombinationRecordStatusByUserIdAndOrderId(Integer status, Long userId, Long orderId) {
        // 校验拼团是否存在
        CombinationRecordDO recordDO = validateCombinationRecord(userId, orderId);

        // 更新状态
        recordDO.setStatus(status);
        recordMapper.updateById(recordDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecordStatusAndStartTimeByUserIdAndOrderId(Integer status, Long userId, Long orderId, LocalDateTime startTime) {
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
        CombinationRecordDO recordDO = recordMapper.selectByUserIdAndOrderId(userId, orderId);
        if (recordDO == null) {
            throw exception(COMBINATION_RECORD_NOT_EXISTS);
        }
        return recordDO;
    }

    // TODO @芋艿：在详细预览下；
    @Override
    public KeyValue<CombinationActivityDO, CombinationProductDO> validateCombinationRecord(Long activityId, Long userId, Long skuId, Integer count) {
        // 1.1 校验拼团活动是否存在
        CombinationActivityDO activity = combinationActivityService.validateCombinationActivityExists(activityId);
        // 1.2 校验活动是否开启
        if (ObjectUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE);
        }
        // 2 校验是否超出单次限购数量
        if (count > activity.getSingleLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED);
        }
        // 2.1、校验活动商品是否存在
        CombinationProductDO product = combinationActivityService.selectByActivityIdAndSkuId(activityId, skuId);
        if (product == null) {
            throw exception(COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 2.2、校验 sku 是否存在
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 2.3、 校验库存是否充足
        if (count > sku.getStock()) {
            throw exception(COMBINATION_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        // 3、校验是否有拼团记录
        List<CombinationRecordDO> recordList = getRecordListByUserIdAndActivityId(userId, activityId);
        if (CollUtil.isEmpty(recordList)) {
            return new KeyValue<>(activity, product);
        }
        // 4、校验是否超出总限购数量
        Integer sumValue = getSumValue(convertList(recordList, CombinationRecordDO::getCount,
                item -> ObjectUtil.equals(item.getStatus(), CombinationRecordStatusEnum.SUCCESS.getStatus())), i -> i, Integer::sum);
        if ((sumValue + count) > activity.getTotalLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED);
        }
        // 5、校验拼团记录是否存在未支付的订单（如果存在未支付的订单则不允许发起新的拼团）
        CombinationRecordDO record = findFirst(recordList, item -> ObjectUtil.equals(item.getStatus(), null));
        if (record == null) {
            return new KeyValue<>(activity, product);
        }
        // 5.1、查询关联的订单是否已经支付
        // 当前 activityId 已经有未支付的订单，不允许在发起新的；要么支付，要么去掉先；
        // TODO 芋艿：看看是不是可以删除掉；
        Integer orderStatus = tradeOrderApi.getOrderStatus(record.getOrderId());
        if (ObjectUtil.equal(orderStatus, TradeOrderStatusEnum.UNPAID.getStatus())) {
            throw exception(COMBINATION_RECORD_FAILED_ORDER_STATUS_UNPAID);
        }

        return new KeyValue<>(activity, product);
    }

    // TODO 芋艿：在详细 review 下；
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO) {
        // 1.1、 校验拼团活动
        CombinationActivityDO activity = combinationActivityService.validateCombinationActivityExists(reqDTO.getActivityId());
        // 1.2 校验是否超出单次限购数量
        if (reqDTO.getCount() > activity.getSingleLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED);
        }
        // 1.3、校验是否有拼团记录
        List<CombinationRecordDO> records = getRecordListByUserIdAndActivityId(reqDTO.getUserId(), reqDTO.getActivityId());
        if (CollUtil.isEmpty(records)) {
            return;
        }
        // 1.4、校验是否超出总限购数量
        Integer sumValue = getSumValue(convertList(records, CombinationRecordDO::getCount,
                item -> ObjectUtil.equals(item.getStatus(), CombinationRecordStatusEnum.SUCCESS.getStatus())), i -> i, Integer::sum);
        if ((sumValue + reqDTO.getCount()) > activity.getTotalLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED);
        }
        // 2、 校验用户是否参加了其它拼团
        List<CombinationRecordDO> recordDOList = recordMapper.selectListByUserIdAndStatus(reqDTO.getUserId(), CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
        if (CollUtil.isNotEmpty(recordDOList)) {
            throw exception(COMBINATION_RECORD_FAILED_HAVE_JOINED);
        }
        // 3、 校验活动是否开启
        if (LocalDateTime.now().isAfter(activity.getStartTime())) {
            throw exception(COMBINATION_RECORD_FAILED_TIME_NOT_START);
        }
        // 4、 校验当前活动是否过期
        if (LocalDateTime.now().isAfter(activity.getEndTime())) {
            throw exception(COMBINATION_RECORD_FAILED_TIME_END);
        }
        // 5、 父拼团是否存在,是否已经满了
        if (reqDTO.getHeadId() != null) {
            // 查询进行中的父拼团
            CombinationRecordDO record = recordMapper.selectOneByHeadId(reqDTO.getHeadId(), CombinationRecordStatusEnum.IN_PROGRESS.getStatus());
            if (record == null) {
                throw exception(COMBINATION_RECORD_HEAD_NOT_EXISTS);
            }
            // 校验拼团是否满足要求
            if (ObjectUtil.equal(record.getUserCount(), record.getUserSize())) {
                throw exception(COMBINATION_RECORD_USER_FULL);
            }
        }

        // 2. 创建拼团记录
        MemberUserRespDTO user = memberUserApi.getUser(reqDTO.getUserId());
        ProductSpuRespDTO spu = productSpuApi.getSpu(reqDTO.getSpuId());
        ProductSkuRespDTO sku = productSkuApi.getSku(reqDTO.getSkuId());
        recordMapper.insert(CombinationActivityConvert.INSTANCE.convert(reqDTO, activity, user, spu, sku));
    }

    @Override
    public CombinationRecordDO getCombinationRecord(Long userId, Long orderId) {
        return validateCombinationRecord(userId, orderId);
    }

    @Override
    public List<CombinationRecordDO> getRecordListByUserIdAndActivityId(Long userId, Long activityId) {
        return recordMapper.selectListByUserIdAndActivityId(userId, activityId);
    }

    @Override
    public CombinationValidateJoinRespDTO validateJoinCombination(Long activityId, Long userId, Long skuId, Integer count) {
        KeyValue<CombinationActivityDO, CombinationProductDO> keyValue = validateCombinationRecord(activityId, userId, skuId, count);
        return new CombinationValidateJoinRespDTO()
                .setActivityId(keyValue.getKey().getId())
                .setName(keyValue.getKey().getName())
                .setCombinationPrice(keyValue.getValue().getCombinationPrice());
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
