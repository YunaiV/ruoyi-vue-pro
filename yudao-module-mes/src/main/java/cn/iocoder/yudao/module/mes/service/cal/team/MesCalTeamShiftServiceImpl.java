package cn.iocoder.yudao.module.mes.service.cal.team;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.shift.MesCalTeamShiftListReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamShiftDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.team.MesCalTeamShiftMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalHolidayTypeEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftMethodEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import cn.iocoder.yudao.module.mes.service.cal.holiday.MesCalHolidayService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanShiftService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanTeamService;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_REST_INVALID;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_SHIFT_COUNT_INVALID;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_SHIFT_METHOD_INVALID;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_SHIFT_NOT_ENOUGH;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_SHIFT_TIME_INVALID;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_TEAM_NOT_ENOUGH;

/**
 * MES 班组排班 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesCalTeamShiftServiceImpl implements MesCalTeamShiftService {

    /**
     * 班次时间格式：H:mm 或 HH:mm
     */
    private static final Pattern SHIFT_TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");

    @Resource
    private MesCalTeamShiftMapper teamShiftMapper;

    @Resource
    @Lazy
    private MesCalPlanService planService;
    @Resource
    @Lazy
    private MesCalPlanShiftService planShiftService;
    @Resource
    @Lazy
    private MesCalPlanTeamService planTeamService;
    @Resource
    @Lazy
    private MesCalHolidayService holidayService;
    @Resource
    @Lazy
    private MesCalTeamService teamService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateTeamShiftRecords(Long planId) {
        // 1.1 查询排班计划
        MesCalPlanDO plan = planService.getPlan(planId);
        // 1.2 查询计划中的班次列表
        List<MesCalPlanShiftDO> shifts = planShiftService.getPlanShiftListByPlanId(planId);
        // 1.3 查询计划中的班组列表
        List<MesCalPlanTeamDO> teams = planTeamService.getPlanTeamListByPlanId(planId);
        // 1.4 校验班组和班次数量是否满足班型要求
        MesCalShiftTypeEnum shiftTypeEnum = MesCalShiftTypeEnum.valueOf(plan.getShiftType());
        int requiredCount = shiftTypeEnum.getRequiredTeamCount();
        if (teams.size() < requiredCount) {
            throw exception(CAL_TEAM_SHIFT_GENERATE_TEAM_NOT_ENOUGH);
        }
        if (shifts.size() < requiredCount) {
            throw exception(CAL_TEAM_SHIFT_GENERATE_SHIFT_NOT_ENOUGH);
        }

        // 2.1 计算日期跨度，查询范围内的节假日
        LocalDate startDate = plan.getStartDate().toLocalDate();
        LocalDate endDate = plan.getEndDate().toLocalDate();
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        Set<LocalDate> holidayDates = getHolidayDates(startDate, endDate);
        // 2.2 遍历每天生成排班记录，最后统一写入数据库
        // 假期停班：节假日不生成排班，按天倒班只累计有效排班日（假期不消耗倒班天数）；按周/月/季度仍按日历周期换班 https://t.zsxq.com/XdcRW
        boolean dayMethod = MesCalShiftMethodEnum.DAY.getMethod().equals(plan.getShiftMethod());
        List<MesCalTeamShiftDO> allRecords = new ArrayList<>();
        int shiftIndex = 0;
        int workDayOffset = 0;
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            LocalDateTime currentDay = currentDate.atStartOfDay();
            boolean holiday = holidayDates.contains(currentDate);
            // 2.2.1 根据倒班方式计算 shiftIndex 单白班不倒班，跳过；周期起点恰逢假期时，也照常换班
            if (!MesCalShiftTypeEnum.SINGLE.getType().equals(plan.getShiftType()) && !(dayMethod && holiday)) {
                shiftIndex = calculateShiftIndex(shiftIndex, dayMethod ? workDayOffset : i, currentDate, startDate, plan);
            }
            if (holiday) {
                continue;
            }
            workDayOffset++;
            // 2.2.2 根据轮班方式生成排班记录，收集到列表
            buildRecordsForDay(allRecords, planId, currentDay, shiftIndex, plan.getShiftType(), teams, shifts);
        }
        // 2.3 校验同一班组相邻两次排班之间留有休息间隔 https://t.zsxq.com/XdcRW
        validateTeamShiftRest(allRecords, shifts);

        // 3. 批量插入排班记录
        if (CollUtil.isNotEmpty(allRecords)) {
            teamShiftMapper.insertBatch(allRecords);
        }
    }

    /**
     * 获得日期范围内的节假日（日期类型为节假日的，排班时停班）
     */
    private Set<LocalDate> getHolidayDates(LocalDate startDate, LocalDate endDate) {
        List<MesCalHolidayDO> holidays = holidayService.getHolidayList(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        return convertSet(holidays, holiday -> holiday.getDay().toLocalDate(),
                holiday -> MesCalHolidayTypeEnum.HOLIDAY.getType().equals(holiday.getType()));
    }

    /**
     * 校验同一班组相邻两次排班之间留有休息间隔
     *
     * 按全部排班的上班开始时刻排序，前一班的结束时刻不早于同一班组下一班的开始时刻，
     * 即上班时间重叠或首尾相接（例如夜班次日 08:00 下班后立即接 08:00 的白班），不允许生成
     */
    private void validateTeamShiftRest(List<MesCalTeamShiftDO> records, List<MesCalPlanShiftDO> shifts) {
        // 1. 解析班次的开始、结束时间
        Map<Long, MesCalPlanShiftDO> shiftMap = convertMap(shifts, MesCalPlanShiftDO::getId);
        Map<Long, LocalTime[]> shiftTimeMap = convertMap(shifts, MesCalPlanShiftDO::getId,
                shift -> new LocalTime[]{parseShiftTime(shift, shift.getStartTime(), false),
                        parseShiftTime(shift, shift.getEndTime(), true)});

        // 2. 按上班开始时刻排序，逐条与同一班组的上一次排班比较
        List<MesCalTeamShiftDO> sortedRecords = new ArrayList<>(records);
        sortedRecords.sort(Comparator.comparing(record -> buildShiftTimeRange(record, shiftTimeMap)[0]));
        Map<Long, MesCalTeamShiftDO> lastRecordMap = new HashMap<>();
        for (MesCalTeamShiftDO record : sortedRecords) {
            MesCalTeamShiftDO lastRecord = lastRecordMap.put(record.getTeamId(), record);
            if (lastRecord == null || buildShiftTimeRange(lastRecord, shiftTimeMap)[1]
                    .isBefore(buildShiftTimeRange(record, shiftTimeMap)[0])) {
                continue;
            }
            MesCalTeamDO team = teamService.getTeam(record.getTeamId());
            throw exception(CAL_TEAM_SHIFT_GENERATE_REST_INVALID, team != null ? team.getName() : record.getTeamId(),
                    lastRecord.getDay().toLocalDate(), shiftMap.get(lastRecord.getShiftId()).getName(),
                    record.getDay().toLocalDate(), shiftMap.get(record.getShiftId()).getName());
        }
    }

    /**
     * 计算排班记录的实际上班时间段
     *
     * 开始时间在排班日期当天；结束时间不晚于开始时间（例如 20:00~08:00、16:00~00:00）时，跨午夜到次日结束
     *
     * @return [开始时刻, 结束时刻]
     */
    private static LocalDateTime[] buildShiftTimeRange(MesCalTeamShiftDO record, Map<Long, LocalTime[]> shiftTimeMap) {
        LocalTime[] shiftTime = shiftTimeMap.get(record.getShiftId());
        LocalDate day = record.getDay().toLocalDate();
        LocalDateTime start = day.atTime(shiftTime[0]);
        LocalDateTime end = shiftTime[1].isAfter(shiftTime[0]) ? day.atTime(shiftTime[1])
                : day.plusDays(1).atTime(shiftTime[1]);
        return new LocalDateTime[]{start, end};
    }

    /**
     * 解析班次时间，兼容 H:mm（例如 8:00）
     *
     * 结束时间允许 24:00，表示当天 24:00，按 00:00 处理后由“结束时间不晚于开始时间时跨午夜到次日”规则得到次日 00:00 https://t.zsxq.com/XdcRW
     *
     * @param endTime 是否为结束时间
     */
    private static LocalTime parseShiftTime(MesCalPlanShiftDO shift, String time, boolean endTime) {
        Matcher matcher = time != null ? SHIFT_TIME_PATTERN.matcher(time.trim()) : null;
        if (matcher == null || !matcher.matches()) {
            throw exception(CAL_TEAM_SHIFT_GENERATE_SHIFT_TIME_INVALID, shift.getName());
        }
        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        if (endTime && hour == 24 && minute == 0) {
            return LocalTime.MIDNIGHT;
        }
        if (hour > 23 || minute > 59) {
            throw exception(CAL_TEAM_SHIFT_GENERATE_SHIFT_TIME_INVALID, shift.getName());
        }
        return LocalTime.of(hour, minute);
    }

    /**
     * 计算当天的 shiftIndex（根据倒班方式判断是否需要递增）
     */
    private int calculateShiftIndex(int shiftIndex, int dayOffset, LocalDate currentDate,
                                    LocalDate startDate, MesCalPlanDO plan) {
        if (dayOffset == 0) {
            return shiftIndex; // 第一天不递增
        }
        if (MesCalShiftMethodEnum.QUARTER.getMethod().equals(plan.getShiftMethod())) {
            // 按季度轮班：到了新季度的第一天
            LocalDate quarterStart = LocalDateTimeUtils.getQuarterStart(currentDate);
            LocalDate planQuarterStart = LocalDateTimeUtils.getQuarterStart(startDate);
            if (currentDate.equals(quarterStart) && !quarterStart.equals(planQuarterStart)) {
                return shiftIndex + 1;
            }
        } else if (MesCalShiftMethodEnum.MONTH.getMethod().equals(plan.getShiftMethod())) {
            // 按月轮班：到了月初
            LocalDate monthStart = currentDate.withDayOfMonth(1);
            LocalDate planMonthStart = startDate.withDayOfMonth(1);
            if (currentDate.equals(monthStart) && !monthStart.equals(planMonthStart)) {
                return shiftIndex + 1;
            }
        } else if (MesCalShiftMethodEnum.WEEK.getMethod().equals(plan.getShiftMethod())) {
            // 按周轮班：到了周一
            LocalDate weekStart = LocalDateTimeUtils.getWeekStart(currentDate);
            LocalDate planWeekStart = LocalDateTimeUtils.getWeekStart(startDate);
            if (currentDate.equals(weekStart) && !weekStart.equals(planWeekStart)) {
                return shiftIndex + 1;
            }
        } else if (MesCalShiftMethodEnum.DAY.getMethod().equals(plan.getShiftMethod())) {
            // 按天轮班：到了指定轮班天数的倍数
            Integer shiftCount = plan.getShiftCount();
            if (shiftCount == null || shiftCount <= 0) {
                throw exception(CAL_TEAM_SHIFT_GENERATE_SHIFT_COUNT_INVALID);
            }
            if (dayOffset % shiftCount == 0) {
                return shiftIndex + 1;
            }
        } else {
            throw exception(CAL_TEAM_SHIFT_GENERATE_SHIFT_METHOD_INVALID, plan.getShiftMethod());
        }
        return shiftIndex;
    }

    /**
     * 根据班型生成当天的排班记录，添加到 allRecords 列表
     */
    private void buildRecordsForDay(List<MesCalTeamShiftDO> allRecords, Long planId,
                                    LocalDateTime currentDay, int shiftIndex, Integer shiftType,
                                    List<MesCalPlanTeamDO> teams, List<MesCalPlanShiftDO> shifts) {
        if (MesCalShiftTypeEnum.SINGLE.getType().equals(shiftType)) {
            // 单白班：不需要倒班
            allRecords.add(MesCalTeamShiftDO.builder()
                    .planId(planId)
                    .day(currentDay)
                    .teamId(teams.get(0).getTeamId())
                    .shiftId(shifts.get(0).getId())
                    .sort(1).build());
        } else if (MesCalShiftTypeEnum.TWO.getType().equals(shiftType)) {
            // 两班倒
            if (shiftIndex % 2 == 0) {
                // A 组上白班
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(0).getTeamId())
                        .shiftId(shifts.get(0).getId())
                        .sort(1).build());
                // B 组上夜班
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(1).getTeamId())
                        .shiftId(shifts.get(1).getId())
                        .sort(2).build());
            } else {
                // A 组上夜班
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(0).getTeamId())
                        .shiftId(shifts.get(1).getId())
                        .sort(2).build());
                // B 组上白班
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(1).getTeamId())
                        .shiftId(shifts.get(0).getId())
                        .sort(1).build());
            }
        } else if (MesCalShiftTypeEnum.THREE.getType().equals(shiftType)) {
            // 三班倒：按 白班 → 夜班 → 中班 轮转。原 白班 → 中班 → 夜班 轮转时，换班当天中班 24:00 下班的班组会立即接 00:00 的夜班 https://t.zsxq.com/XdcRW
            if (shiftIndex % 3 == 0) {
                // A 组上白班、B 组上中班、C 组上夜班
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(0).getTeamId())
                        .shiftId(shifts.get(0).getId())
                        .sort(1).build());
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(1).getTeamId())
                        .shiftId(shifts.get(1).getId())
                        .sort(2).build());
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(2).getTeamId())
                        .shiftId(shifts.get(2).getId())
                        .sort(3).build());
            } else if (shiftIndex % 3 == 1) {
                // A 组上夜班、B 组上白班、C 组上中班
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(0).getTeamId())
                        .shiftId(shifts.get(2).getId())
                        .sort(3).build());
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(1).getTeamId())
                        .shiftId(shifts.get(0).getId())
                        .sort(1).build());
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(2).getTeamId())
                        .shiftId(shifts.get(1).getId())
                        .sort(2).build());
            } else {
                // A 组上中班、B 组上夜班、C 组上白班
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(0).getTeamId())
                        .shiftId(shifts.get(1).getId())
                        .sort(2).build());
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(1).getTeamId())
                        .shiftId(shifts.get(2).getId())
                        .sort(3).build());
                allRecords.add(MesCalTeamShiftDO.builder()
                        .planId(planId).day(currentDay)
                        .teamId(teams.get(2).getTeamId())
                        .shiftId(shifts.get(0).getId())
                        .sort(1).build());
            }
        }
    }

    @Override
    public List<MesCalTeamShiftDO> getTeamShiftList(MesCalTeamShiftListReqVO reqVO) {
        return teamShiftMapper.selectList(reqVO);
    }

    @Override
    public void deleteByPlanId(Long planId) {
        teamShiftMapper.deleteByPlanId(planId);
    }

    @Override
    public void deleteByTeamId(Long teamId) {
        teamShiftMapper.deleteByTeamId(teamId);
    }

}
