package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordReqDTO;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 拼团活动 API 实现类
 *
 * @author HUIHUI
 */
@Service
public class CombinationApiImpl implements CombinationApi {

    @Resource
    private CombinationActivityService activityService;

    @Override
    public void createRecord(CombinationRecordReqDTO reqDTO) {
        activityService.createRecord(reqDTO);
    }

    @Override
    public boolean validateRecordStatusIsSuccess(Long userId, Long orderId) {
        return activityService.validateRecordStatusIsSuccess(userId, orderId);
    }

    @Override
    public void updateRecordStatus(Long userId, Long orderId, Integer status) {
        activityService.updateRecordStatusByUserIdAndOrderId(userId, orderId, status);
    }

    @Override
    public void updateRecordStatusAndStartTime(Long userId, Long orderId, Integer status) {
        activityService.updateRecordStatusAndStartTimeByUserIdAndOrderId(userId, orderId, status, LocalDateTime.now());
    }


}
