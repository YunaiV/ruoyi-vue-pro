package cn.iocoder.yudao.module.mes.controller.admin.cal.holiday;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.holiday.vo.MesCalHolidayRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.holiday.vo.MesCalHolidaySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import cn.iocoder.yudao.module.mes.service.cal.holiday.MesCalHolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 假期设置")
@RestController
@RequestMapping("/mes/cal/holiday")
@Validated
public class MesCalHolidayController {

    @Resource
    private MesCalHolidayService holidayService;

    @PostMapping("/save")
    @Operation(summary = "保存假期设置", description = "如果该日期已存在记录，则更新")
    @PreAuthorize("@ss.hasPermission('mes:cal-holiday:create')")
    public CommonResult<Long> saveHoliday(@Valid @RequestBody MesCalHolidaySaveReqVO saveReqVO) {
        return success(holidayService.saveHoliday(saveReqVO));
    }

    @GetMapping("/get-by-day")
    @Operation(summary = "根据日期获得假期设置")
    @Parameter(name = "day", description = "日期", required = true)
    @PreAuthorize("@ss.hasPermission('mes:cal-holiday:query')")
    public CommonResult<MesCalHolidayRespVO> getHolidayByDay(
            @RequestParam("day") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime day) {
        MesCalHolidayDO holiday = holidayService.getHolidayByDay(day);
        return success(BeanUtils.toBean(holiday, MesCalHolidayRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得假期设置列表", description = "支持可选日期范围过滤，不传则返回全量数据")
    @PreAuthorize("@ss.hasPermission('mes:cal-holiday:query')")
    public CommonResult<List<MesCalHolidayRespVO>> getHolidayList(
            @RequestParam(value = "startDay", required = false)
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startDay,
            @RequestParam(value = "endDay", required = false)
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endDay) {
        List<MesCalHolidayDO> list = holidayService.getHolidayList(startDay, endDay);
        return success(BeanUtils.toBean(list, MesCalHolidayRespVO.class));
    }

}
