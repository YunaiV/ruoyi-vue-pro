package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.recrod.BargainRecordPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordCreateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.bargain.BargainRecordMapper;
import cn.iocoder.yudao.module.promotion.enums.bargain.BargainRecordStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    @Override
    public Long createBargainRecord(Long userId, AppBargainRecordCreateReqVO reqVO) {
        // 1. 校验砍价活动（包括库存）
        BargainActivityDO activity = bargainActivityService.validateBargainActivityCanJoin(reqVO.getActivityId());

        // 2.1 校验当前是否已经有参与中的砍价活动
        if (CollUtil.isNotEmpty(bargainRecordMapper.selectListByUserIdAndActivityIdAndStatus(
                userId, reqVO.getActivityId(), BargainRecordStatusEnum.IN_PROGRESS.getStatus()))) {
            throw exception(BARGAIN_RECORD_CREATE_FAIL_EXISTS);
        }
        // 2.2 是否超过参与的上限
        if (bargainRecordMapper.selectCountByUserIdAndActivityIdAndStatus(
                userId, reqVO.getActivityId(), BargainRecordStatusEnum.SUCCESS.getStatus()) >= activity.getTotalLimitCount()) {
            throw exception(BARGAIN_RECORD_CREATE_FAIL_LIMIT);
        }

        // 3. 创建砍价记录
        BargainRecordDO record = BargainRecordDO.builder().userId(userId)
                .activityId(reqVO.getActivityId()).spuId(activity.getSpuId()).skuId(activity.getSkuId())
                .bargainFirstPrice(activity.getBargainFirstPrice()).bargainPrice(activity.getBargainFirstPrice())
                .status(BargainRecordStatusEnum.IN_PROGRESS.getStatus()).build();
        bargainRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public Boolean updateBargainRecordBargainPrice(Long id, Integer whereBargainPrice,
                                                   Integer reducePrice, Boolean success) {
        BargainRecordDO updateObj = new BargainRecordDO().setBargainPrice(whereBargainPrice - reducePrice);
        if (success) {
            updateObj.setStatus(BargainRecordStatusEnum.SUCCESS.getStatus());
        }
        return bargainRecordMapper.updateByIdAndBargainPrice(id, whereBargainPrice, updateObj) > 0;
    }

    @Override
    public BargainValidateJoinRespDTO validateJoinBargain(Long userId, Long bargainRecordId, Long skuId) {
        // 1.1 砍价记录不存在
        BargainRecordDO record = bargainRecordMapper.selectByIdAndUserId(bargainRecordId, userId);
        if (record == null) {
            throw exception(BARGAIN_RECORD_NOT_EXISTS);
        }
        // 1.2 砍价记录未在进行中
        if (ObjUtil.notEqual(record.getStatus(), BargainRecordStatusEnum.SUCCESS.getStatus())) {
            throw exception(BARGAIN_JOIN_RECORD_NOT_SUCCESS);
        }
        // 1.3 砍价记录已经下单
        if (record.getOrderId() != null) {
            throw exception(BARGAIN_JOIN_RECORD_ALREADY_ORDER);
        }

        // 2.1 校验砍价活动（包括库存）
        BargainActivityDO activity = bargainActivityService.validateBargainActivityCanJoin(record.getActivityId());
        Assert.isTrue(Objects.equals(skuId, activity.getSkuId()), "砍价商品不匹配"); // 防御性校验
        return new BargainValidateJoinRespDTO().setActivityId(activity.getId()).setName(activity.getName())
                .setBargainPrice(record.getBargainPrice());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBargainRecordOrderId(Long id, Long orderId) {
        // 更新失败，说明已经下单
        int updateCount = bargainRecordMapper.updateOrderIdById(id, orderId);
        if (updateCount == 0) {
            throw exception(BARGAIN_JOIN_RECORD_ALREADY_ORDER);
        }
    }

    @Override
    public BargainRecordDO getBargainRecord(Long id) {
        return bargainRecordMapper.selectById(id);
    }

    @Override
    public BargainRecordDO getLastBargainRecord(Long userId, Long activityId) {
        return bargainRecordMapper.selectLastByUserIdAndActivityId(userId, activityId);
    }

    @Override
    public Map<Long, Integer> getBargainRecordUserCountMap(Collection<Long> activityIds, @Nullable Integer status) {
        return bargainRecordMapper.selectUserCountByActivityIdsAndStatus(activityIds, status);
    }

    @Override
    public Integer getBargainRecordUserCount(Integer status) {
        return bargainRecordMapper.selectUserCountByStatus(status);
    }

    @Override
    public Integer getBargainRecordUserCount(Long activityId, Integer status) {
        return bargainRecordMapper.selectUserCountByActivityIdAndStatus(activityId, status);
    }

    @Override
    public PageResult<BargainRecordDO> getBargainRecordPage(BargainRecordPageReqVO pageReqVO) {
        return bargainRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<BargainRecordDO> getBargainRecordPage(Long userId, PageParam pageParam) {
        return bargainRecordMapper.selectBargainRecordPage(userId, pageParam);
    }

    @Override
    public List<BargainRecordDO> getBargainRecordList(Integer status, Integer count) {
        return bargainRecordMapper.selectListByStatusAndCount(status, count);
    }

}
