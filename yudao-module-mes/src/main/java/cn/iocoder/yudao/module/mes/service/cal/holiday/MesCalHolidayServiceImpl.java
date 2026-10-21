package cn.iocoder.yudao.module.mes.service.cal.holiday;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.holiday.vo.MesCalHolidaySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.holiday.MesCalHolidayMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalHolidayTypeEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalPlanStatusEnum;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_HOLIDAY_PLAN_CONFIRMED;

/**
 * MES 假期设置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesCalHolidayServiceImpl implements MesCalHolidayService {

    @Resource
    private MesCalHolidayMapper holidayMapper;

    @Resource
    @Lazy
    private MesCalPlanService planService;

    @Override
    public Long saveHoliday(MesCalHolidaySaveReqVO saveReqVO) {
        // 1. 校验是否节假日的变化，不影响已确认的排班计划
        MesCalHolidayDO existing = holidayMapper.selectByDay(saveReqVO.getDay());
        validateHolidayChangeForConfirmedPlan(existing, saveReqVO);

        // 2.1 Upsert 逻辑：如果该日期已有记录，则更新
        if (existing != null) {
            MesCalHolidayDO updateObj = BeanUtils.toBean(saveReqVO, MesCalHolidayDO.class);
            updateObj.setId(existing.getId());
            holidayMapper.updateById(updateObj);
            return existing.getId();
        }

        // 2.2 插入新记录
        MesCalHolidayDO holiday = BeanUtils.toBean(saveReqVO, MesCalHolidayDO.class);
        holidayMapper.insert(holiday);
        return holiday.getId();
    }

    /**
     * 校验是否节假日的变化，不影响已确认的排班计划
     *
     * 排班计划确认时按“节假日停班”生成排班记录（按天倒班时还会顺延后续倒班），确认后不会重新生成。
     * 因此已确认计划范围内的日期，不允许在“节假日”与“非节假日”之间切换，避免与已生成的排班不一致 https://t.zsxq.com/XdcRW
     */
    private void validateHolidayChangeForConfirmedPlan(MesCalHolidayDO existing, MesCalHolidaySaveReqVO saveReqVO) {
        boolean oldHoliday = existing != null && MesCalHolidayTypeEnum.HOLIDAY.getType().equals(existing.getType());
        boolean newHoliday = MesCalHolidayTypeEnum.HOLIDAY.getType().equals(saveReqVO.getType());
        if (oldHoliday == newHoliday) {
            return;
        }
        List<MesCalPlanDO> plans = planService.getPlanListByStatusAndDay(
                MesCalPlanStatusEnum.CONFIRMED.getStatus(), saveReqVO.getDay().toLocalDate());
        if (CollUtil.isEmpty(plans)) {
            return;
        }
        throw exception(CAL_HOLIDAY_PLAN_CONFIRMED, saveReqVO.getDay().toLocalDate(),
                StrUtil.join("、", convertList(plans, MesCalPlanDO::getName)));
    }

    @Override
    public List<MesCalHolidayDO> getHolidayList(LocalDateTime startDay, LocalDateTime endDay) {
        return holidayMapper.selectList(startDay, endDay);
    }

    @Override
    public MesCalHolidayDO getHolidayByDay(LocalDateTime day) {
        return holidayMapper.selectByDay(day);
    }

}
