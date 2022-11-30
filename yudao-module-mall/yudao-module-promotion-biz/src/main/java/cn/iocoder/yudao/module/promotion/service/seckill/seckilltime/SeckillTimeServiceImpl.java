package cn.iocoder.yudao.module.promotion.service.seckill.seckilltime;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.seckilltime.vo.SeckillTimeCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.seckilltime.vo.SeckillTimeUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckilltime.SeckillTimeConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckilltime.SeckillTimeMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.SECKILL_TIME_CONFLICTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.SECKILL_TIME_NOT_EXISTS;

/**
 * 秒杀时段 Service 实现类
 *
 * @author 芋道源码     // TODO @halfninety：作者改成你自己哈
 */
@Service
@Validated
public class SeckillTimeServiceImpl implements SeckillTimeService {

    @Resource
    private SeckillTimeMapper seckillTimeMapper;

    @Override
    public Long createSeckillTime(SeckillTimeCreateReqVO createReqVO) {
        // 校验时间段是否冲突
        validateSeckillTimeConflict(null,createReqVO.getStartTime(), createReqVO.getEndTime());
        // 插入
        SeckillTimeDO seckillTime = SeckillTimeConvert.INSTANCE.convert(createReqVO);
        seckillTimeMapper.insert(seckillTime);
        // 返回
        return seckillTime.getId();
    }

    @Override
    public void updateSeckillTime(SeckillTimeUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSeckillTimeExists(updateReqVO.getId());
        // 校验时间段是否冲突
        validateSeckillTimeConflict(updateReqVO.getId(), updateReqVO.getStartTime(), updateReqVO.getEndTime());
        // 更新
        SeckillTimeDO updateObj = SeckillTimeConvert.INSTANCE.convert(updateReqVO);
        seckillTimeMapper.updateById(updateObj);
    }

    @Override
    public void deleteSeckillTime(Long id) {
        // 校验存在
        this.validateSeckillTimeExists(id);
        // 删除
        seckillTimeMapper.deleteById(id);
    }

    private void validateSeckillTimeExists(Long id) {
        if (seckillTimeMapper.selectById(id) == null) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
    }

    /**
     * 校验时间是否存在冲突
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private void validateSeckillTimeConflict(Long id, LocalTime startTime, LocalTime endTime) {
        //查询开始时间，结束时间，是否在别人的时间段内
        List<SeckillTimeDO> startTimeList = seckillTimeMapper.selectListWithTime(startTime);
        List<SeckillTimeDO> endTimeList = seckillTimeMapper.selectListWithTime(endTime);
        //查询自己时间段内是否有时间段
        List<SeckillTimeDO> startEndTimeList = seckillTimeMapper.selectListWithTime(startTime, endTime);
        if (id != null) {
            //移除自己
            startTimeList.removeIf(seckillTime -> Objects.equals(seckillTime.getId(), id));
            endTimeList.removeIf(seckillTime -> Objects.equals(seckillTime.getId(), id));
            startEndTimeList.removeIf(seckillTime -> Objects.equals(seckillTime.getId(), id));
        }
        if (CollUtil.isNotEmpty(startTimeList) || CollUtil.isNotEmpty(endTimeList)
                || CollUtil.isNotEmpty(startEndTimeList)) {
            throw exception(SECKILL_TIME_CONFLICTS);
        }
    }

    @Override
    public SeckillTimeDO getSeckillTime(Long id) {
        return seckillTimeMapper.selectById(id);
    }

    @Override
    public List<SeckillTimeDO> getSeckillTimeList() {
        return seckillTimeMapper.selectList();
    }

    // TODO @halfninety：updateActivityCount + 和 -，可以执使用一个方法实现哈。多传递一个参数
    @Override
    public void sekillActivityCountAdd(List<Long> ids) {
        seckillTimeMapper.sekillActivityCountAdd(ids);
    }

    @Override
    public void sekillActivityCountReduce(List<Long> ids) {
        seckillTimeMapper.sekillActivityCountReduce(ids);
    }

}
