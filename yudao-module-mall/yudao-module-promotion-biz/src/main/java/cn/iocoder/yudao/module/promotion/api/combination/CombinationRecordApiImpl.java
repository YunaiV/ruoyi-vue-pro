package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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
    public void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO) {
        recordService.createCombinationRecord(reqDTO);
    }

    @Override
    public boolean isCombinationRecordSuccess(Long userId, Long orderId) {
        return CombinationRecordStatusEnum.isSuccess(recordService.getCombinationRecord(userId, orderId).getStatus());
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

}
