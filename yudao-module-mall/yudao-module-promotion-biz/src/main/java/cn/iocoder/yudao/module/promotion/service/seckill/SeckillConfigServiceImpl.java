package cn.iocoder.yudao.module.promotion.service.seckill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.SeckillConfigConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillconfig.SeckillConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.findFirst;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isBetween;
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
    public void updateSeckillConfig(SeckillConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateSeckillConfigExists(updateReqVO.getId());
        // 校验时间段是否冲突
        validateSeckillConfigConflict(updateReqVO.getStartTime(), updateReqVO.getEndTime(), updateReqVO.getId());

        // 更新
        SeckillConfigDO updateObj = SeckillConfigConvert.INSTANCE.convert(updateReqVO);
        seckillConfigMapper.updateById(updateObj);
    }

    @Override
    public void updateSeckillConfigStatus(Long id, Integer status) {
        // 校验秒杀时段是否存在
        validateSeckillConfigExists(id);

        // 更新状态
        seckillConfigMapper.updateById(new SeckillConfigDO().setId(id).setStatus(status));
    }

    @Override
    public SeckillConfigDO getCurrentSeckillConfig() {
        List<SeckillConfigDO> list = seckillConfigMapper.selectList(SeckillConfigDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
        return findFirst(list, config -> isBetween(config.getStartTime(), config.getEndTime()));
    }

    @Override
    public void deleteSeckillConfig(Long id) {
        // 校验存在
        validateSeckillConfigExists(id);

        // 删除
        seckillConfigMapper.deleteById(id);
    }

    private void validateSeckillConfigExists(Long id) {
        if (seckillConfigMapper.selectById(id) == null) {
            throw exception(SECKILL_CONFIG_NOT_EXISTS);
        }
    }

    /**
     * 校验时间是否存在冲突
     *
     * @param startTimeStr 开始时间
     * @param endTimeStr   结束时间
     */
    private void validateSeckillConfigConflict(String startTimeStr, String endTimeStr, Long id) {
        // 1. 查询出所有的时段配置
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);
        List<SeckillConfigDO> configs = seckillConfigMapper.selectList();
        // 更新时排除自己
        if (id != null) {
            configs.removeIf(item -> ObjectUtil.equal(item.getId(), id));
        }

        // 2. 判断是否有重叠的时间
        boolean hasConflict = configs.stream().anyMatch(config -> LocalDateTimeUtils.isOverlap(startTime, endTime,
                LocalTime.parse(config.getStartTime()), LocalTime.parse(config.getEndTime())));
        if (hasConflict) {
            throw exception(SECKILL_CONFIG_TIME_CONFLICTS);
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
    public void validateSeckillConfigExists(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 如果有数量不匹配，说明有不存在的，则抛出 SECKILL_CONFIG_NOT_EXISTS 业务异常
        List<SeckillConfigDO> configs = seckillConfigMapper.selectBatchIds(ids);
        if (configs.size() != ids.size()) {
            throw exception(SECKILL_CONFIG_NOT_EXISTS);
        }

        // 2. 如果存在关闭，则抛出 SECKILL_CONFIG_DISABLE 业务异常
        configs.forEach(config -> {
            if (ObjectUtil.equal(config.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
                throw exception(SECKILL_CONFIG_DISABLE);
            }
        });
    }

    @Override
    public PageResult<SeckillConfigDO> getSeckillConfigPage(SeckillConfigPageReqVO pageVO) {
        return seckillConfigMapper.selectPage(pageVO);
    }

    @Override
    public List<SeckillConfigDO> getSeckillConfigListByStatus(Integer status) {
        List<SeckillConfigDO> list = seckillConfigMapper.selectListByStatus(status);
        list.sort(Comparator.comparing(SeckillConfigDO::getStartTime));
        return list;
    }

}
