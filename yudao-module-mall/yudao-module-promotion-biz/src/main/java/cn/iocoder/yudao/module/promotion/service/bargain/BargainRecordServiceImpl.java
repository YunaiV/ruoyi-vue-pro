package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.bargain.BargainRecordMapper;
import cn.iocoder.yudao.module.promotion.enums.bargain.BargainRecordStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

/**
 * 砍价记录 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class BargainRecordServiceImpl implements BargainRecordService {

    @Resource
    private BargainActivityService bargainActivityService;

    @Resource
    private BargainRecordMapper bargainRecordMapper;

    // TODO puhui999：create 时，需要校验下限购数量；

    @Override
    public BargainValidateJoinRespDTO validateJoinBargain(Long userId, Long bargainRecordId, Long skuId) {
        // 1.1 拼团记录不存在
        BargainRecordDO record = bargainRecordMapper.selectByIdAndUserId(bargainRecordId, userId);
        if (record == null) {
            throw exception(BARGAIN_RECORD_NOT_EXISTS);
        }
        // 1.2 拼团记录未在进行中
        if (ObjUtil.notEqual(record.getStatus(), BargainRecordStatusEnum.IN_PROGRESS)) {
            throw exception(BARGAIN_JOIN_RECORD_NOT_IN_PROGRESS);
        }

        // 2.1 砍价活动不存在
        BargainActivityDO activity = bargainActivityService.getBargainActivity(record.getActivityId());
        if (activity == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(activity.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(BARGAIN_JOIN_ACTIVITY_STATUS_CLOSED);
        }
        Assert.isTrue(Objects.equals(skuId, activity.getSkuId()), "砍价商品不匹配"); // 防御性校验
        // 2.2 活动已过期
        if (LocalDateTimeUtils.isBetween(activity.getStartTime(), activity.getEndTime())) {
            throw exception(BARGAIN_JOIN_FAILED_ACTIVITY_TIME_END);
        }
        // 2.3 库存不足
        if (activity.getStock() <= 0) {
            throw exception(BARGAIN_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        return new BargainValidateJoinRespDTO().setActivityId(activity.getId()).setName(activity.getName())
                .setBargainPrice(record.getPayPrice());
    }

}
