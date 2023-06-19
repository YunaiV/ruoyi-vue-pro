package cn.iocoder.yudao.module.promotion.service.seckill.seckillconfig;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
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
import java.util.Set;
import java.util.stream.Collectors;

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
    public Long createSeckillConfig(SeckillConfigCreateReqVO createReqVO) {
        // 校验时间段是否冲突
        validateSeckillConfigConflict(createReqVO.getStartTime(), createReqVO.getEndTime());

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
        validateSeckillConfigConflict(updateReqVO.getStartTime(), updateReqVO.getEndTime());

        // 更新
        SeckillConfigDO updateObj = SeckillConfigConvert.INSTANCE.convert(updateReqVO);
        seckillConfigMapper.updateById(updateObj);
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
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
    }

    /**
     * 校验时间是否存在冲突
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private void validateSeckillConfigConflict(String startTime, String endTime) {
        LocalTime startTime1 = LocalTime.parse(startTime);
        LocalTime endTime1 = LocalTime.parse(endTime);
        // 检查选择的时间是否相等
        if (startTime1.equals(endTime1)) {
            throw exception(SECKILL_TIME_EQUAL);
        }
        // 检查开始时间是否在结束时间之前
        if (startTime1.isAfter(endTime1)) {
            throw exception(SECKILL_START_TIME_BEFORE_END_TIME);
        }
        // 查询出所有的时段配置
        List<SeckillConfigDO> configDOs = seckillConfigMapper.selectList();
        // 过滤出重叠的时段 ids
        Set<Long> ids = configDOs.stream().filter((config) -> {
            LocalTime startTime2 = LocalTime.parse(config.getStartTime());
            LocalTime endTime2 = LocalTime.parse(config.getEndTime());
            // 判断时间是否重叠
            // 开始时间在已配置时段的结束时间之前 且 结束时间在已配置时段的开始时间之后 []
            return startTime1.isBefore(endTime2) && endTime1.isAfter(startTime2)
                    // 开始时间在已配置时段的开始时间之前 且 结束时间在已配置时段的开始时间之后 (] 或 ()
                    || startTime1.isBefore(startTime2) && endTime1.isAfter(startTime2)
                    // 开始时间在已配置时段的结束时间之前 且 结束时间在已配值时段的结束时间之后 [) 或 ()
                    || startTime1.isBefore(endTime2) && endTime1.isAfter(endTime2);
        }).map(SeckillConfigDO::getId).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(ids)) {
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
        if (seckillConfigMapper.selectBatchIds(configIds).size() != configIds.size()) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<SeckillConfigDO> getSeckillConfigPage(SeckillConfigPageReqVO pageVO) {
        return seckillConfigMapper.selectPage(pageVO);
    }

    @Override
    public List<SeckillConfigDO> getListAllSimple() {
        return seckillConfigMapper.selectList(SeckillConfigDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
    }

}
