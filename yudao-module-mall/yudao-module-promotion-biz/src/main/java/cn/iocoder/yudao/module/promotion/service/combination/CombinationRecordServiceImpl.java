package cn.iocoder.yudao.module.promotion.service.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateStatusReqDTO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationRecordMapper;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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
    private CombinationActivityService combinationActivityService;

    @Resource
    private CombinationRecordMapper recordMapper;

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
        CombinationActivityDO activity = combinationActivityService.validateCombinationActivityExists(reqDTO.getActivityId());
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

    @Override
    public List<CombinationRecordDO> getRecordListByUserIdAndActivityId(Long userId, Long activityId) {
        return recordMapper.selectListByUserIdAndActivityId(userId, activityId);
    }

    @Override
    public void validateCombinationLimitCount(Long activityId, Integer count, Integer sumCount) {
        // 1.1 校验拼团活动
        CombinationActivityDO activity = combinationActivityService.validateCombinationActivityExists(activityId);
        // 校验是否达到限购总限购标准
        if ((sumCount + count) > activity.getTotalLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED);
        }
        // 单次购买是否达到限购标准
        if (count > activity.getSingleLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED);
        }
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
