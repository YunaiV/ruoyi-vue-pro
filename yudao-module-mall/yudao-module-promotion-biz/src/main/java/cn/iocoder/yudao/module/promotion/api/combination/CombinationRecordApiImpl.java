package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COMBINATION_RECORD_NOT_EXISTS;

/**
 * 拼团活动 API 实现类
 *
 * @author HUIHUI
 */
@Service
public class CombinationRecordApiImpl implements CombinationRecordApi {

    @Resource
    private CombinationRecordService recordService;

    @Override
    public void validateCombinationRecord(Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        recordService.validateCombinationRecord(userId, activityId, headId, skuId, count);
    }

    @Override
    public void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO) {
        recordService.createCombinationRecord(reqDTO);
    }

    @Override
    public boolean isCombinationRecordSuccess(Long userId, Long orderId) {
        CombinationRecordDO combinationRecord = recordService.getCombinationRecord(userId, orderId);
        if (combinationRecord == null) {
            throw exception(COMBINATION_RECORD_NOT_EXISTS);
        }

        return CombinationRecordStatusEnum.isSuccess(combinationRecord.getStatus());
    }

    @Override
    public void updateRecordStatusToSuccess(Long userId, Long orderId) {
        recordService.updateCombinationRecordStatusByUserIdAndOrderId(CombinationRecordStatusEnum.SUCCESS.getStatus(), userId, orderId);
    }

    @Override
    public void updateRecordStatusToFailed(Long userId, Long orderId) {
        recordService.updateCombinationRecordStatusByUserIdAndOrderId(CombinationRecordStatusEnum.FAILED.getStatus(), userId, orderId);
    }

    @Override
    public void updateRecordStatusToInProgress(Long userId, Long orderId, LocalDateTime startTime) {
        recordService.updateRecordStatusAndStartTimeByUserIdAndOrderId(CombinationRecordStatusEnum.IN_PROGRESS.getStatus(),
                userId, orderId, startTime);
    }

    @Override
    public CombinationValidateJoinRespDTO validateJoinCombination(Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        return recordService.validateJoinCombination(userId, activityId, headId, skuId, count);
    }

}
