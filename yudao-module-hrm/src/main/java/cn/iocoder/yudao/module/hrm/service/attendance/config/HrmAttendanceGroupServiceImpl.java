package cn.iocoder.yudao.module.hrm.service.attendance.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceHolidayDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.config.HrmAttendanceGroupMapper;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceHolidayTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_GROUP_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_ATTENDANCE_GROUP_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_ATTENDANCE_GROUP_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_ATTENDANCE_GROUP_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_ATTENDANCE_GROUP_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_ATTENDANCE_GROUP_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_ATTENDANCE_GROUP_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_ATTENDANCE_GROUP_UPDATE_SUCCESS;

/**
 * HRM 考勤组 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmAttendanceGroupServiceImpl implements HrmAttendanceGroupService {

    @Resource
    private HrmAttendanceGroupMapper attendanceGroupMapper;

    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private DeptApi deptApi;
    @Resource
    private HrmAttendanceHolidayService attendanceHolidayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_ATTENDANCE_GROUP_TYPE, subType = HRM_ATTENDANCE_GROUP_CREATE_SUB_TYPE,
            bizNo = "{{#attendanceGroup.id}}", success = HRM_ATTENDANCE_GROUP_CREATE_SUCCESS)
    public Long createAttendanceGroup(HrmAttendanceGroupSaveReqVO createReqVO) {
        // 1.1 校验考勤组名称唯一
        validateAttendanceGroupNameUnique(null, createReqVO.getName());
        // 1.2 校验班次、特殊日期和适用范围
        validateAttendanceGroup(createReqVO);

        // 2. 创建考勤组
        HrmAttendanceGroupDO group = BeanUtils.toBean(createReqVO, HrmAttendanceGroupDO.class)
                .setSpecialDates(BeanUtils.toBean(createReqVO.getSpecialDates(),
                        HrmAttendanceGroupDO.SpecialDate.class, specialDate -> specialDate
                                .setDate(LocalDateTimeUtils.getDayBeginTime(specialDate.getDate()))))
                .setDefaultStatus(false);
        attendanceGroupMapper.insert(group);

        // 3. 接管考勤组适用范围
        transferAttendanceGroupScope(group.getId(), createReqVO);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("attendanceGroup", group);
        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_ATTENDANCE_GROUP_TYPE, subType = HRM_ATTENDANCE_GROUP_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = HRM_ATTENDANCE_GROUP_UPDATE_SUCCESS)
    public void updateAttendanceGroup(HrmAttendanceGroupSaveReqVO updateReqVO) {
        // 1.1 校验考勤组是否存在
        HrmAttendanceGroupDO oldGroup = validateAttendanceGroupExists(updateReqVO.getId());
        // 1.2 校验考勤组名称唯一
        validateAttendanceGroupNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 1.3 校验班次、特殊日期和适用范围
        validateAttendanceGroup(updateReqVO);

        // 2. 更新考勤组
        HrmAttendanceGroupDO group = BeanUtils.toBean(updateReqVO, HrmAttendanceGroupDO.class)
                .setSpecialDates(BeanUtils.toBean(updateReqVO.getSpecialDates(),
                        HrmAttendanceGroupDO.SpecialDate.class, specialDate -> specialDate
                                .setDate(LocalDateTimeUtils.getDayBeginTime(specialDate.getDate()))));
        attendanceGroupMapper.updateById(group);

        // 3. 接管考勤组适用范围
        transferAttendanceGroupScope(group.getId(), updateReqVO);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(oldGroup, HrmAttendanceGroupSaveReqVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_ATTENDANCE_GROUP_TYPE, subType = HRM_ATTENDANCE_GROUP_DELETE_SUB_TYPE,
            bizNo = "{{#attendanceGroup.id}}", success = HRM_ATTENDANCE_GROUP_DELETE_SUCCESS)
    public void deleteAttendanceGroup(Long id) {
        // 1. 校验考勤组是否存在且不是默认考勤组
        HrmAttendanceGroupDO group = validateAttendanceGroupExists(id);
        if (Boolean.TRUE.equals(group.getDefaultStatus())) {
            throw exception(ATTENDANCE_DEFAULT_CANNOT_DELETE);
        }

        // 2. 删除考勤组
        attendanceGroupMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("attendanceGroup", group);
    }

    @Override
    public HrmAttendanceGroupDO getAttendanceGroup(Long id) {
        return attendanceGroupMapper.selectById(id);
    }

    @Override
    public HrmAttendanceGroupDO getMyAttendanceGroup(Long employeeId) {
        // 1. 校验员工是否存在
        HrmEmployeeDO employee = employeeService.getEmployee(employeeId);
        if (employee == null) {
            return null;
        }

        // 2. 匹配员工考勤组
        Map<Long, List<Long>> deptHierarchyMap = buildDeptHierarchyMap(
                Collections.singleton(employee.getDeptId()));
        return matchAttendanceGroup(employee,
                attendanceGroupMapper.selectListOrderByDefaultStatusAndId(), deptHierarchyMap);
    }

    @Override
    public Map<Long, HrmAttendanceGroupDO> getAttendanceGroupMap(Collection<Long> employeeIds) {
        // 1. 获得员工 Map
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
        if (CollUtil.isEmpty(employeeMap)) {
            return Collections.emptyMap();
        }
        // 2. 匹配员工考勤组
        Map<Long, List<Long>> deptHierarchyMap = buildDeptHierarchyMap(
                convertSet(employeeMap.values(), HrmEmployeeDO::getDeptId));
        return matchAttendanceGroupMap(employeeMap,
                attendanceGroupMapper.selectListOrderByDefaultStatusAndId(), deptHierarchyMap);
    }

    @Override
    public Map<LocalDate, HrmAttendanceGroupDO.Shift> getAttendanceGroupShiftMap(
            HrmAttendanceGroupDO attendanceGroup, Collection<LocalDate> attendanceDates,
            Map<LocalDate, HrmAttendanceHolidayDO> holidayMap) {
        if (attendanceGroup == null || CollUtil.isEmpty(attendanceDates)) {
            return Collections.emptyMap();
        }

        // 1. 将考勤组特殊日期按自然日归集，便于优先匹配
        Map<LocalDate, HrmAttendanceGroupDO.SpecialDate> specialDateMap = convertMap(
                CollUtil.emptyIfNull(attendanceGroup.getSpecialDates()),
                specialDate -> specialDate.getDate().toLocalDate(), Function.identity(),
                (first, second) -> second);

        // 2. 逐日结合特殊日期、法定节假日和每周班次解析实际班次
        Map<LocalDate, HrmAttendanceGroupDO.Shift> result = new LinkedHashMap<>();
        for (LocalDate attendanceDate : attendanceDates) {
            if (attendanceDate == null) {
                continue;
            }
            HrmAttendanceHolidayDO holiday = holidayMap == null ? null : holidayMap.get(attendanceDate);
            HrmAttendanceGroupDO.Shift shift = resolveAttendanceGroupShift(
                    attendanceGroup, attendanceDate, holiday, specialDateMap.get(attendanceDate));
            result.put(attendanceDate, shift);
        }
        return result;
    }

    @Override
    public HrmAttendanceGroupDO.Shift getEmployeeAttendanceShift(
            Long employeeId, LocalDateTime attendanceTime) {
        LocalDate attendanceDate = attendanceTime.toLocalDate();
        HrmAttendanceGroupDO attendanceGroup = getMyAttendanceGroup(employeeId);
        Map<LocalDate, HrmAttendanceHolidayDO> holidayMap =
                attendanceHolidayService.getAttendanceHolidayMap(
                        LocalDateTimeUtils.getDateTimeRange(
                                attendanceDate, attendanceDate));
        return getAttendanceGroupShiftMap(attendanceGroup,
                Collections.singletonList(attendanceDate), holidayMap).get(attendanceDate);
    }

    @Override
    public PageResult<HrmAttendanceGroupDO> getAttendanceGroupPage(HrmAttendanceGroupPageReqVO pageReqVO) {
        return attendanceGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmAttendanceGroupDO> getAttendanceGroupList() {
        return attendanceGroupMapper.selectListOrderByDefaultStatusAndId();
    }

    @Override
    public HrmAttendanceGroupDO validateAttendanceGroupExists(Long id) {
        HrmAttendanceGroupDO group = attendanceGroupMapper.selectById(id);
        if (group == null) {
            throw exception(ATTENDANCE_GROUP_NOT_EXISTS);
        }
        return group;
    }

    /**
     * 解析指定日期实际执行的考勤班次：特殊日期优先于法定节假日，法定节假日优先于每周班次
     *
     * @param group 考勤组
     * @param attendanceDate 考勤日期
     * @param holiday 法定节假日
     * @param specialDate 考勤组特殊日期
     * @return 实际班次；休息日返回 {@code null}
     */
    private HrmAttendanceGroupDO.Shift resolveAttendanceGroupShift(
            HrmAttendanceGroupDO group, LocalDate attendanceDate,
            HrmAttendanceHolidayDO holiday, HrmAttendanceGroupDO.SpecialDate specialDate) {
        // 命中特殊日期时，以考勤组的工作日或休息日配置为准
        if (specialDate != null) {
            return Objects.equals(specialDate.getType(), HrmAttendanceHolidayTypeEnum.WORK.getType())
                    ? getWorkShift(group, attendanceDate) : null;
        }
        // 未配置特殊日期时，再按法定节假日决定休息或补班
        if (holiday != null) {
            if (Objects.equals(holiday.getType(), HrmAttendanceHolidayTypeEnum.REST.getType())
                    && Boolean.TRUE.equals(group.getRest())) {
                return null;
            }
            if (Objects.equals(holiday.getType(), HrmAttendanceHolidayTypeEnum.WORK.getType())) {
                return getWorkShift(group, attendanceDate);
            }
        }
        return getWeeklyShift(group, attendanceDate);
    }

    /**
     * 获得指定工作日的班次：优先使用当天配置的每周班次；补班日期未命中时，回退到考勤组第一个班次
     *
     * @param group 考勤组
     * @param attendanceDate 考勤日期
     * @return 工作日班次
     */
    private HrmAttendanceGroupDO.Shift getWorkShift(
            HrmAttendanceGroupDO group, LocalDate attendanceDate) {
        HrmAttendanceGroupDO.Shift shift = getWeeklyShift(group, attendanceDate);
        if (shift != null || CollUtil.isEmpty(group.getShifts())) {
            return shift;
        }
        return CollUtil.getFirst(group.getShifts());
    }

    /**
     * 获得指定日期配置的每周班次
     *
     * @param group 考勤组
     * @param attendanceDate 考勤日期
     * @return 每周班次；未配置时返回 {@code null}
     */
    private HrmAttendanceGroupDO.Shift getWeeklyShift(
            HrmAttendanceGroupDO group, LocalDate attendanceDate) {
        if (CollUtil.isEmpty(group.getShifts())) {
            return null;
        }
        int week = attendanceDate.getDayOfWeek().getValue();
        return CollUtil.findOne(group.getShifts(),
                shift -> shift != null && CollUtil.contains(shift.getWeeks(), week));
    }

    private void validateAttendanceGroupNameUnique(Long id, String name) {
        HrmAttendanceGroupDO group = attendanceGroupMapper.selectByName(name);
        if (group != null && ObjUtil.notEqual(group.getId(), id)) {
            throw exception(ATTENDANCE_NAME_DUPLICATE);
        }
    }

    private void validateAttendanceGroup(HrmAttendanceGroupSaveReqVO reqVO) {
        // 1. 校验适用部门存在
        deptApi.validateDeptList(reqVO.getDeptIds());
        // 2. 校验适用员工存在
        employeeService.validateEmployeeListExists(reqVO.getEmployeeIds());
    }

    /**
     * 接管考勤组适用范围：同一部门或员工只能归属一个考勤组
     *
     * @param attendanceGroupId 当前考勤组编号
     * @param reqVO 考勤组保存信息
     */
    private void transferAttendanceGroupScope(
            Long attendanceGroupId, HrmAttendanceGroupSaveReqVO reqVO) {
        // 1. 计算当前组接管的部门及员工范围
        Set<Long> deptIds = new HashSet<>(reqVO.getDeptIds());
        Set<Long> deptScope = new HashSet<>(deptIds);
        deptScope.addAll(convertSet(deptApi.getChildDeptList(deptIds), DeptRespDTO::getId));
        Set<Long> employeeIds = new HashSet<>(reqVO.getEmployeeIds());
        if (CollUtil.isNotEmpty(deptScope)) {
            employeeIds.addAll(convertSet(
                    employeeService.getEmployeeListByDeptIds(deptScope), HrmEmployeeDO::getId));
        }

        // 2. 从其它考勤组移除冲突范围
        for (HrmAttendanceGroupDO group : attendanceGroupMapper.selectListOrderByDefaultStatusAndId()) {
            if (Objects.equals(group.getId(), attendanceGroupId)) {
                continue;
            }
            List<Long> remainDeptIds = CollUtil.subtractToList(
                    CollUtil.emptyIfNull(group.getDeptIds()), deptScope);
            List<Long> remainEmployeeIds = CollUtil.subtractToList(
                    CollUtil.emptyIfNull(group.getEmployeeIds()), employeeIds);
            if (Objects.equals(remainDeptIds, group.getDeptIds())
                    && Objects.equals(remainEmployeeIds, group.getEmployeeIds())) {
                continue;
            }
            attendanceGroupMapper.updateById(new HrmAttendanceGroupDO().setId(group.getId())
                    .setDeptIds(remainDeptIds).setEmployeeIds(remainEmployeeIds));
        }
    }

    private Map<Long, HrmAttendanceGroupDO> matchAttendanceGroupMap(
            Map<Long, HrmEmployeeDO> employeeMap, List<HrmAttendanceGroupDO> groupList,
            Map<Long, List<Long>> deptHierarchyMap) {
        Map<Long, HrmAttendanceGroupDO> result = new HashMap<>();
        for (HrmEmployeeDO employee : employeeMap.values()) {
            HrmAttendanceGroupDO group = matchAttendanceGroup(employee, groupList, deptHierarchyMap);
            if (group != null) {
                result.put(employee.getId(), group);
            }
        }
        return result;
    }

    /**
     * 按显式员工、最近上级部门、默认考勤组的顺序匹配员工考勤组
     */
    private HrmAttendanceGroupDO matchAttendanceGroup(
            HrmEmployeeDO employee, List<HrmAttendanceGroupDO> groups,
            Map<Long, List<Long>> deptHierarchyMap) {
        // 1. 优先匹配显式员工
        for (HrmAttendanceGroupDO group : groups) {
            if (CollUtil.contains(group.getEmployeeIds(), employee.getId())) {
                return group;
            }
        }

        // 2. 其次按当前部门到父部门的顺序匹配
        if (employee.getDeptId() != null) {
            List<Long> deptHierarchy = deptHierarchyMap.getOrDefault(
                    employee.getDeptId(), Collections.singletonList(employee.getDeptId()));
            for (Long deptId : deptHierarchy) {
                for (HrmAttendanceGroupDO group : groups) {
                    if (CollUtil.contains(group.getDeptIds(), deptId)) {
                        return group;
                    }
                }
            }
        }

        // 3. 最后使用默认考勤组
        return CollUtil.findOne(groups, group -> Boolean.TRUE.equals(group.getDefaultStatus()));
    }

    /**
     * 批量构造部门自身到根部门的层级编号
     *
     * @param deptIds 部门编号集合
     * @return 部门层级 Map
     */
    private Map<Long, List<Long>> buildDeptHierarchyMap(Collection<Long> deptIds) {
        Set<Long> validDeptIds = convertSet(deptIds);
        validDeptIds.removeIf(deptId -> deptId <= 0);
        if (CollUtil.isEmpty(validDeptIds)) {
            return Collections.emptyMap();
        }

        Map<Long, List<Long>> result = new HashMap<>();
        for (Long deptId : validDeptIds) {
            List<Long> hierarchy = new ArrayList<>(Collections.singletonList(deptId));
            hierarchy.addAll(convertList(deptApi.getParentDeptList(deptId), DeptRespDTO::getId));
            result.put(deptId, hierarchy);
        }
        return result;
    }

}
