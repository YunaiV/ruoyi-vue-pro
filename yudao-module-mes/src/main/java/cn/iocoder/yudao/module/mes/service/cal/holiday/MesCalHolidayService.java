package cn.iocoder.yudao.module.mes.service.cal.holiday;

import cn.iocoder.yudao.module.mes.controller.admin.cal.holiday.vo.MesCalHolidaySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MES 假期设置 Service 接口
 *
 * @author 芋道源码
 */
public interface MesCalHolidayService {

    /**
     * 保存假期设置（含 upsert 逻辑：如果该日期已存在记录，则更新）
     *
     * @param saveReqVO 保存信息
     * @return 编号
     */
    Long saveHoliday(@Valid MesCalHolidaySaveReqVO saveReqVO);

    /**
     * 获得假期设置列表
     *
     * @param startDay 开始日期（可选）
     * @param endDay   结束日期（可选）
     * @return 假期设置列表
     */
    List<MesCalHolidayDO> getHolidayList(LocalDateTime startDay, LocalDateTime endDay);

    /**
     * 根据日期获得假期设置
     *
     * @param day 日期
     * @return 假期设置，不存在则返回 null
     */
    MesCalHolidayDO getHolidayByDay(LocalDateTime day);

}
