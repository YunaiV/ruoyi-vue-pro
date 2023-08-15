package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateStatusReqDTO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
    public List<CombinationRecordRespDTO> getRecordListByUserIdAndActivityId(Long userId, Long activityId) {
        return CombinationActivityConvert.INSTANCE.convert(recordService.getRecordListByUserIdAndActivityId(userId, activityId));
    }

    @Override
    public void validateCombinationLimitCount(Long activityId, Integer count, Integer sumCount) {
        recordService.validateCombinationLimitCount(activityId, count, sumCount);
    }

    @Override
    public void updateCombinationRecordStatus(CombinationRecordUpdateStatusReqDTO reqDTO) {
        if (null == reqDTO.getStartTime()) {
            recordService.updateCombinationRecordStatusByUserIdAndOrderId(reqDTO);
        } else {
            recordService.updateCombinationRecordStatusAndStartTimeByUserIdAndOrderId(reqDTO);
        }
    }

}
