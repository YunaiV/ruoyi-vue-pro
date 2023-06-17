package cn.iocoder.yudao.module.promotion.service.seckill.seckillconfig;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillconfig.SeckillConfigConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillconfig.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillconfig.SeckillConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.SECKILL_TIME_CONFLICTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.SECKILL_TIME_NOT_EXISTS;

/**
 * 秒杀时段 Service 实现类
 *
 * @author halfninety
 */
@Service
@Validated
public class SeckillConfigServiceImpl implements SeckillConfigService {

    @Resource
    private SeckillConfigMapper seckillConfigMapper;

    @Override
    public Long createSeckillConfig(SeckillConfigCreateReqVO createReqVO) {
        // 校验时间段是否冲突
        validateSeckillConfigConflict(null, createReqVO.getStartTime(), createReqVO.getEndTime());
        // 插入
        SeckillConfigDO seckillConfig = SeckillConfigConvert.INSTANCE.convert(createReqVO);
        seckillConfigMapper.insert(seckillConfig);
        // 返回
        return seckillConfig.getId();
    }

    @Override
    public void updateSeckillConfig(SeckillConfigUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSeckillConfigExists(updateReqVO.getId());
        // 校验时间段是否冲突
        validateSeckillConfigConflict(updateReqVO.getId(), updateReqVO.getStartTime(), updateReqVO.getEndTime());
        // 更新
        SeckillConfigDO updateObj = SeckillConfigConvert.INSTANCE.convert(updateReqVO);
        seckillConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteSeckillConfig(Long id) {
        // 校验存在
        this.validateSeckillConfigExists(id);
        // 删除
        seckillConfigMapper.deleteById(id);
    }

    private void validateSeckillConfigExists(Long id) {
        if (seckillConfigMapper.selectById(id) == null) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
    }

    /**
     * 校验时间是否存在冲突
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private void validateSeckillConfigConflict(Long id, LocalTime startTime, LocalTime endTime) {
        //查询开始时间，结束时间，是否在别人的时间段内
        List<SeckillConfigDO> startTimeList = seckillConfigMapper.selectListByTime(startTime);
        List<SeckillConfigDO> endTimeList = seckillConfigMapper.selectListByTime(endTime);
        //查询自己时间段内是否有时间段
        List<SeckillConfigDO> startEndTimeList = seckillConfigMapper.selectListByTime(startTime, endTime);
        if (id != null) {
            //移除自己
            startTimeList.removeIf(seckillConfig -> Objects.equals(seckillConfig.getId(), id));
            endTimeList.removeIf(seckillConfig -> Objects.equals(seckillConfig.getId(), id));
            startEndTimeList.removeIf(seckillConfig -> Objects.equals(seckillConfig.getId(), id));
        }
        if (CollUtil.isNotEmpty(startTimeList) || CollUtil.isNotEmpty(endTimeList)
                || CollUtil.isNotEmpty(startEndTimeList)) {
            throw exception(SECKILL_TIME_CONFLICTS);
        }
    }

    @Override
    public SeckillConfigDO getSeckillConfig(Long id) {
        return seckillConfigMapper.selectById(id);
    }

    @Override
    public List<SeckillConfigDO> getSeckillConfigList() {
        return seckillConfigMapper.selectList();
    }

    @Override
    public void validateSeckillConfigExists(Collection<Long> timeIds) {
        if (CollUtil.isEmpty(timeIds)) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
        if (seckillConfigMapper.selectBatchIds(timeIds).size() != timeIds.size()) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
    }

    @Override
    public void seckillActivityCountIncr(Collection<Long> ids) {
        seckillConfigMapper.updateActivityCount(ids, "+", 1);
    }

    @Override
    public void seckillActivityCountDecr(Collection<Long> ids) {
        seckillConfigMapper.updateActivityCount(ids, "-", 1);
    }

    @Override
    public PageResult<SeckillConfigDO> getSeckillConfigPage(SeckillConfigPageReqVO pageVO) {
        return seckillConfigMapper.selectPage(pageVO);
    }

}
