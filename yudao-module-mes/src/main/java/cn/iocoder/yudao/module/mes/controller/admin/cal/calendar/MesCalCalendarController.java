package cn.iocoder.yudao.module.mes.controller.admin.cal.calendar;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.mes.controller.admin.cal.calendar.vo.MesCalCalendarListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.calendar.vo.MesCalCalendarRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.shift.MesCalTeamShiftListReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamMemberDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamShiftDO;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalHolidayTypeEnum;
import cn.iocoder.yudao.module.mes.service.cal.holiday.MesCalHolidayService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanShiftService;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamMemberService;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamService;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - MES 排班日历")
@RestController
@RequestMapping("/mes/cal/calendar")
@Validated
public class MesCalCalendarController {

    @Resource
    private MesCalTeamShiftService teamShiftService;
    @Resource
    private MesCalTeamService teamService;
    @Resource
    private MesCalPlanShiftService planShiftService;
    @Resource
    private MesCalPlanService planService;
    @Resource
    private MesCalTeamMemberService teamMemberService;
    @Resource
    private MesCalHolidayService holidayService;

    @GetMapping("/list")
    @Operation(summary = "查询排班日历")
    @PreAuthorize("@ss.hasPermission('mes:cal-team-shift:query')")
    public CommonResult<List<MesCalCalendarRespVO>> getCalendarList(@Valid MesCalCalendarListReqVO reqVO) {
        // 1.1 根据查询类型获取班组排班记录
        List<MesCalTeamShiftDO> teamShifts = getTeamShifts(reqVO);
        if (CollUtil.isEmpty(teamShifts)) {
            return success(Collections.emptyList());
        }
        // 1.2 按日期范围查询假期，构建节假日日期集合
        Set<String> holidaySet = buildHolidaySet(reqVO.getStartDay(), reqVO.getEndDay());
        // 1.3 批量查询关联数据：班组、班次、排班计划
        Map<Long, MesCalTeamDO> teamMap = teamService.getTeamMap(
                convertSet(teamShifts, MesCalTeamShiftDO::getTeamId));
        Map<Long, MesCalPlanShiftDO> shiftMap = planShiftService.getPlanShiftMap(
                convertSet(teamShifts, MesCalTeamShiftDO::getShiftId));
        Map<Long, MesCalPlanDO> planMap = planService.getPlanMap(
                convertSet(teamShifts, MesCalTeamShiftDO::getPlanId));

        // 2. 按 day 分组聚合
        Map<String, List<MesCalTeamShiftDO>> dayGroupMap = convertMultiMap(teamShifts,
                teamShift -> LocalDateTimeUtil.format(teamShift.getDay(), DatePattern.NORM_DATE_PATTERN));

        // 3. 遍历分组，过滤假期，构建日历响应
        List<MesCalCalendarRespVO> result = new ArrayList<>();
        for (Map.Entry<String, List<MesCalTeamShiftDO>> entry : dayGroupMap.entrySet()) {
            String dayStr = entry.getKey();
            // 3.1 过滤节假日
            if (holidaySet.contains(dayStr)) {
                continue;
            }
            List<MesCalTeamShiftDO> dayShifts = entry.getValue();
            dayShifts.sort(Comparator.comparing(ts -> ts.getSort() != null ? ts.getSort() : 0));
            // 3.2 获取轮班方式（取第一条记录关联的排班计划）
            Integer shiftType = null;
            MesCalTeamShiftDO first = dayShifts.get(0);
            if (first.getPlanId() != null) {
                MesCalPlanDO plan = planMap.get(first.getPlanId());
                if (plan != null) {
                    shiftType = plan.getShiftType();
                }
            }
            // 3.3 构建班组排班项列表
            List<MesCalCalendarRespVO.TeamShiftItem> items = convertList(dayShifts,
                    teamShift -> buildTeamShiftItem(teamShift, teamMap, shiftMap));

            // 3.4 构建日历项，添加到结果列表
            MesCalCalendarRespVO calendarVO = MesCalCalendarRespVO.builder()
                    .day(LocalDateTimeUtil.parseDate(dayStr, DatePattern.NORM_DATE_FORMATTER).atStartOfDay())
                    .shiftType(shiftType)
                    .teamShifts(items)
                    .build();
            result.add(calendarVO);
        }
        return success(result);
    }

    /**
     * 根据查询类型获取班组排班记录
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private List<MesCalTeamShiftDO> getTeamShifts(MesCalCalendarListReqVO reqVO) {
        LocalDateTime startDay = reqVO.getStartDay();
        LocalDateTime endDay = reqVO.getEndDay();
        switch (reqVO.getQueryType()) {
            case MesCalCalendarListReqVO.QUERY_TYPE_TYPE:
                return getTeamShiftsByCalendarType(reqVO.getCalendarType(), startDay, endDay);
            case MesCalCalendarListReqVO.QUERY_TYPE_TEAM:
                return getTeamShiftsByTeamId(reqVO.getTeamId(), startDay, endDay);
            case MesCalCalendarListReqVO.QUERY_TYPE_USER:
                return getTeamShiftsByUserId(reqVO.getUserId(), startDay, endDay);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 构建单条 TeamShiftItem
     */
    private MesCalCalendarRespVO.TeamShiftItem buildTeamShiftItem(MesCalTeamShiftDO ts,
                                                                  Map<Long, MesCalTeamDO> teamMap,
                                                                  Map<Long, MesCalPlanShiftDO> shiftMap) {
        MesCalTeamDO team = teamMap.get(ts.getTeamId());
        MesCalPlanShiftDO shift = shiftMap.get(ts.getShiftId());
        return MesCalCalendarRespVO.TeamShiftItem.builder()
                .teamId(ts.getTeamId()).teamName(team != null ? team.getName() : null)
                .shiftId(ts.getShiftId()).shiftName(shift != null ? shift.getName() : null)
                .sort(ts.getSort()).build();
    }

    /**
     * 按班组类型查询排班记录
     */
    private List<MesCalTeamShiftDO> getTeamShiftsByCalendarType(Integer calendarType,
                                                                  LocalDateTime startDay, LocalDateTime endDay) {
        if (calendarType == null) {
            return Collections.emptyList();
        }
        // 1. 查询指定类型的所有班组
        List<MesCalTeamDO> teams = teamService.getTeamList().stream()
                .filter(t -> calendarType.equals(t.getCalendarType()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(teams)) {
            return Collections.emptyList();
        }
        // 2. 一次 IN 查询这些班组在日期范围内的排班记录
        MesCalTeamShiftListReqVO reqVO = new MesCalTeamShiftListReqVO()
                .setTeamIds(convertSet(teams, MesCalTeamDO::getId))
                .setStartDay(startDay).setEndDay(endDay);
        return teamShiftService.getTeamShiftList(reqVO);
    }

    /**
     * 按班组编号查询排班记录
     */
    private List<MesCalTeamShiftDO> getTeamShiftsByTeamId(Long teamId, LocalDateTime startDay, LocalDateTime endDay) {
        if (teamId == null) {
            return Collections.emptyList();
        }
        MesCalTeamShiftListReqVO reqVO = new MesCalTeamShiftListReqVO()
                .setTeamId(teamId).setStartDay(startDay).setEndDay(endDay);
        return teamShiftService.getTeamShiftList(reqVO);
    }

    /**
     * 按用户编号查询排班记录（先查用户所属班组，再查班组排班）
     */
    private List<MesCalTeamShiftDO> getTeamShiftsByUserId(Long userId,
                                                            LocalDateTime startDay, LocalDateTime endDay) {
        if (userId == null) {
            return Collections.emptyList();
        }
        // 1. 查询用户所属的班组（一个用户只属于一个班组）
        MesCalTeamMemberDO member = teamMemberService.getTeamMemberByUserId(userId);
        if (member == null) {
            return Collections.emptyList();
        }
        // 2. 查询该班组在日期范围内的排班记录
        MesCalTeamShiftListReqVO reqVO = new MesCalTeamShiftListReqVO()
                .setTeamId(member.getTeamId()).setStartDay(startDay).setEndDay(endDay);
        return teamShiftService.getTeamShiftList(reqVO);
    }

    /**
     * 按日期范围查询假期，构建节假日日期集合（yyyy-MM-dd 格式）
     */
    private Set<String> buildHolidaySet(LocalDateTime startDay, LocalDateTime endDay) {
        List<MesCalHolidayDO> holidays = holidayService.getHolidayList(startDay, endDay);
        return convertSet(holidays,
                holiday -> LocalDateTimeUtil.format(holiday.getDay(), DatePattern.NORM_DATE_PATTERN),
                holiday -> MesCalHolidayTypeEnum.HOLIDAY.getType().equals(holiday.getType()));
    }

}
