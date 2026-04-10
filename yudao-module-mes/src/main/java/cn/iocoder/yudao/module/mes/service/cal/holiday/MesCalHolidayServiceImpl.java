package cn.iocoder.yudao.module.mes.service.cal.holiday;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.holiday.vo.MesCalHolidaySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.holiday.MesCalHolidayMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public Long saveHoliday(MesCalHolidaySaveReqVO saveReqVO) {
        // Upsert 逻辑：如果该日期已有记录，则更新
        MesCalHolidayDO existing = holidayMapper.selectByDay(saveReqVO.getDay());
        if (existing != null) {
            MesCalHolidayDO updateObj = BeanUtils.toBean(saveReqVO, MesCalHolidayDO.class);
            updateObj.setId(existing.getId());
            holidayMapper.updateById(updateObj);
            return existing.getId();
        }

        // 插入新记录
        MesCalHolidayDO holiday = BeanUtils.toBean(saveReqVO, MesCalHolidayDO.class);
        holidayMapper.insert(holiday);
        return holiday.getId();
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
