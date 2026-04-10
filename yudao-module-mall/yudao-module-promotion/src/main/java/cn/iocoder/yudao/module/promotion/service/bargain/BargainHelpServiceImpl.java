package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.help.BargainHelpPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.help.AppBargainHelpCreateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainHelpDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.bargain.BargainHelpMapper;
import cn.iocoder.yudao.module.promotion.enums.bargain.BargainRecordStatusEnum;
import jodd.util.MathUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

/**
 * 砍价助力 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class BargainHelpServiceImpl implements BargainHelpService {

    @Resource
    private BargainHelpMapper bargainHelpMapper;

    @Resource
    private BargainRecordService bargainRecordService;
    @Resource
    private BargainActivityService bargainActivityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BargainHelpDO createBargainHelp(Long userId, AppBargainHelpCreateReqVO reqVO) {
        // 1.1 校验砍价记录存在，并且处于进行中
        BargainRecordDO record = bargainRecordService.getBargainRecord(reqVO.getRecordId());
        if (record == null) {
            throw exception(BARGAIN_RECORD_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(record.getStatus(), BargainRecordStatusEnum.IN_PROGRESS.getStatus())) {
            throw exception(BARGAIN_HELP_CREATE_FAIL_RECORD_NOT_IN_PROCESS);
        }
        // 1.2 不能自己给自己砍价
        if (ObjUtil.equal(record.getUserId(), userId)) {
            throw exception(BARGAIN_HELP_CREATE_FAIL_RECORD_SELF);
        }

        // 2.1 校验砍价活动
        BargainActivityDO activity = bargainActivityService.getBargainActivity(record.getActivityId());
        // 2.2 校验自己是否助力次数上限
        if (bargainHelpMapper.selectCountByUserIdAndActivityId(userId, activity.getId())
                >= activity.getBargainCount()) {
            throw exception(BARGAIN_HELP_CREATE_FAIL_LIMIT);
        }
        // 2.3 特殊情况：砍价已经砍到最低价，不能再砍了
        if (record.getBargainPrice() <= activity.getBargainMinPrice()) {
            throw exception(BARGAIN_HELP_CREATE_FAIL_RECORD_NOT_IN_PROCESS);
        }

        // 3. 已经助力
        if (bargainHelpMapper.selectByUserIdAndRecordId(userId, record.getId()) != null) {
            throw exception(BARGAIN_HELP_CREATE_FAIL_HELP_EXISTS);
        }

        // 4.1 计算砍价金额
        Integer reducePrice = calculateReducePrice(activity, record);
        Assert.isTrue(reducePrice > 0, "砍价金额必须大于 0 元");
        // 4.2 创建助力记录
        BargainHelpDO help = BargainHelpDO.builder().userId(userId).activityId(activity.getId())
                .recordId(record.getId()).reducePrice(reducePrice).build();
        bargainHelpMapper.insert(help);

        // 5. 判断砍价记录是否完成
        Boolean success = record.getBargainPrice() - reducePrice <= activity.getBargainMinPrice() // 情况一：砍价已经砍到最低价
                || bargainHelpMapper.selectUserCountMapByRecordId(reqVO.getRecordId()) >= activity.getHelpMaxCount(); // 情况二：砍价助力已经达到上限
        if (!bargainRecordService.updateBargainRecordBargainPrice(
                record.getId(), record.getBargainPrice(), reducePrice, success)) {
            // 多人一起砍价，需要重试
            throw exception(BARGAIN_HELP_CREATE_FAIL_CONFLICT);
        }
        return help;
    }

    // TODO 芋艿：优化点：实现一个更随机的逻辑，可以按照你自己的业务；
    private Integer calculateReducePrice(BargainActivityDO activity, BargainRecordDO record) {
        // 1. 随机金额
        Integer reducePrice = MathUtil.randomInt(activity.getBargainMinPrice(),
                activity.getRandomMaxPrice() + 1); // + 1 的原因是，randomInt 默认不包含第二个参数
        // 2. 校验是否超过砍价上限
        if (record.getBargainPrice() - reducePrice < activity.getBargainMinPrice()) {
            reducePrice = record.getBargainPrice() - activity.getBargainMinPrice();
        }
        return reducePrice;
    }

    @Override
    public Map<Long, Integer> getBargainHelpUserCountMapByActivity(Collection<Long> activityIds) {
        return bargainHelpMapper.selectUserCountMapByActivityId(activityIds);
    }

    @Override
    public Map<Long, Integer> getBargainHelpUserCountMapByRecord(Collection<Long> recordIds) {
        return bargainHelpMapper.selectUserCountMapByRecordId(recordIds);
    }

    @Override
    public Long getBargainHelpCountByActivity(Long activityId, Long userId) {
        return bargainHelpMapper.selectCountByUserIdAndActivityId(userId, activityId);
    }

    @Override
    public PageResult<BargainHelpDO> getBargainHelpPage(BargainHelpPageReqVO pageReqVO) {
        return bargainHelpMapper.selectPage(pageReqVO);
    }

    @Override
    public List<BargainHelpDO> getBargainHelpListByRecordId(Long recordId) {
        return bargainHelpMapper.selectListByRecordId(recordId);
    }

    @Override
    public BargainHelpDO getBargainHelp(Long recordId, Long userId) {
        return bargainHelpMapper.selectByUserIdAndRecordId(userId, recordId);
    }

}
