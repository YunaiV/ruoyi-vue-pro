package cn.iocoder.yudao.module.promotion.service.seckill.seckillconfig;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillconfig.SeckillConfigConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillconfig.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillconfig.SeckillConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

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
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillConfig(SeckillConfigCreateReqVO createReqVO) {
        // 校验时间段是否冲突
        validateSeckillConfigConflict(createReqVO.getStartTime(), createReqVO.getEndTime(), null);

        // 插入
        SeckillConfigDO seckillConfig = SeckillConfigConvert.INSTANCE.convert(createReqVO);
        seckillConfigMapper.insert(seckillConfig);
        // 返回
        return seckillConfig.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillConfig(SeckillConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateSeckillConfigExists(updateReqVO.getId());
        // 校验时间段是否冲突
        validateSeckillConfigConflict(updateReqVO.getStartTime(), updateReqVO.getEndTime(), updateReqVO.getId());

        // 更新
        SeckillConfigDO updateObj = SeckillConfigConvert.INSTANCE.convert(updateReqVO);
        seckillConfigMapper.updateById(updateObj);
    }

    // TODO  @puhui999: 这个要不合并到更新操作里? 不单独有个操作咧; fix: 更新状态不用那么多必须的参数，更新的时候需要校验时间段
    @Override
    public void updateSeckillConfigStatus(Long id, Integer status) {
        // 校验秒杀时段是否存在
        validateSeckillConfigExists(id);

        // 更新状态
        seckillConfigMapper.updateById(new SeckillConfigDO().setId(id).setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckillConfig(Long id) {
        // 校验存在
        validateSeckillConfigExists(id);

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
    private void validateSeckillConfigConflict(String startTime, String endTime, Long seckillConfigId) {
        LocalTime startTime1 = LocalTime.parse(startTime);
        LocalTime endTime1 = LocalTime.parse(endTime);
        // 查询出所有的时段配置
        List<SeckillConfigDO> configDOs = seckillConfigMapper.selectList();
        // 更新时排除自己
        if (seckillConfigId != null) {
            configDOs.removeIf(item -> ObjectUtil.equal(item.getId(), seckillConfigId));
        }
        // 过滤出重叠的时段 ids
        boolean hasConflict = configDOs.stream().anyMatch(config -> {
            LocalTime startTime2 = LocalTime.parse(config.getStartTime());
            LocalTime endTime2 = LocalTime.parse(config.getEndTime());
            // 判断时间是否重叠
            return LocalDateTimeUtils.isOverlap(startTime1, endTime1, startTime2, endTime2);
        });

        if (hasConflict) {
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
    public void validateSeckillConfigExists(Collection<Long> configIds) {
        if (CollUtil.isEmpty(configIds)) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
        List<SeckillConfigDO> configDOs = seckillConfigMapper.selectBatchIds(configIds);
        if (CollUtil.isEmpty(configDOs)) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
        // 过滤出关闭的时段
        List<SeckillConfigDO> filterList = CollectionUtils.filterList(configDOs, item -> ObjectUtil.equal(item.getStatus(), CommonStatusEnum.DISABLE.getStatus()));
        if (CollUtil.isNotEmpty(filterList)) {
            throw exception(SECKILL_TIME_DISABLE);
        }
        if (configDOs.size() != configIds.size()) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<SeckillConfigDO> getSeckillConfigPage(SeckillConfigPageReqVO pageVO) {
        return seckillConfigMapper.selectPage(pageVO);
    }

    @Override
    public List<SeckillConfigDO> getSeckillConfigListByStatus(Integer status) {
        return seckillConfigMapper.selectListByStatus(status);
    }

}
