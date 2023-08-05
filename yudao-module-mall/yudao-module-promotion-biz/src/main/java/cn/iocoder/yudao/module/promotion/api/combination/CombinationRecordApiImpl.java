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
    public void createRecord(CombinationRecordCreateReqDTO reqDTO) {
        recordService.createRecord(reqDTO);
    }

    @Override
    public boolean validateRecordStatusIsSuccess(Long userId, Long orderId) {
        return CombinationRecordStatusEnum.isSuccess(recordService.getRecord(userId, orderId).getStatus());
    }

    @Override
    public void updateRecordStatus(Long userId, Long orderId, Integer status) {
        recordService.updateRecordStatusByUserIdAndOrderId(userId, orderId, status);
    }

    @Override
    public void updateRecordStatusAndStartTime(Long userId, Long orderId, Integer status) {
        recordService.updateRecordStatusAndStartTimeByUserIdAndOrderId(userId, orderId, status, LocalDateTime.now());
    }

}
