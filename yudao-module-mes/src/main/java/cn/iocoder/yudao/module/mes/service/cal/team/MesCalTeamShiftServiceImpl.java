package cn.iocoder.yudao.module.mes.service.cal.team;

import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.shift.MesCalTeamShiftListReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamShiftDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.team.MesCalTeamShiftMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftMethodEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_SHIFT_NOT_ENOUGH;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_TEAM_NOT_ENOUGH;

/**
 * MES 班组排班 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesCalTeamShiftServiceImpl implements MesCalTeamShiftService {

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

        // 2. 计算日期跨度，遍历每天生成排班记录，最后统一写入数据库
        LocalDate startDate = plan.getStartDate().toLocalDate();
        LocalDate endDate = plan.getEndDate().toLocalDate();
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<MesCalTeamShiftDO> allRecords = new ArrayList<>();
        int shiftIndex = 0;
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            LocalDateTime currentDay = currentDate.atStartOfDay();
            // 2.2.1 根据倒班方式计算 shiftIndex
            shiftIndex = calculateShiftIndex(shiftIndex, i, currentDate, startDate, plan);
            // 2.2.2 根据轮班方式生成排班记录，收集到列表
            buildRecordsForDay(allRecords, planId, currentDay, shiftIndex, plan.getShiftType(), teams, shifts);
        }

        // 3. 批量插入排班记录
        teamShiftMapper.insertBatch(allRecords);
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
            if (dayOffset % plan.getShiftCount() == 0) {
                return shiftIndex + 1;
            }
        } else {
            throw new IllegalArgumentException("不支持的倒班方式: " + plan.getShiftMethod());
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
            // 三班倒
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
            } else {
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
