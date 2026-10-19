package cn.iocoder.yudao.module.oa.service.workreport;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.workreport.OaWorkReportDO;
import cn.iocoder.yudao.module.oa.dal.mysql.workreport.OaWorkReportMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportStatusEnum;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.WORK_REPORT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.WORK_REPORT_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.WORK_REPORT_NO_DUPLICATE;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.WORK_REPORT_STATUS_INVALID;

/**
 * OA 工作汇报 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaWorkReportServiceImpl implements OaWorkReportService {

    /**
     * 工作汇报统计权限
     */
    private static final String WORK_REPORT_STATISTICS_PERMISSION = "oa:work-report:statistics";

    @Resource
    private OaWorkReportMapper workReportMapper;
    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private PermissionApi permissionApi;

    @Override
    public Long createWorkReport(OaWorkReportSaveReqVO createReqVO, Long userId) {
        // 1. 查询汇报人信息
        AdminUserRespDTO user = adminUserApi.validateUser(userId);
        // 1.2 生成单号并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.WORK_REPORT_NO_PREFIX);
        if (workReportMapper.selectByNo(no) != null) {
            throw exception(WORK_REPORT_NO_DUPLICATE);
        }

        // 2. 创建工作汇报草稿
        OaWorkReportDO workReport = BeanUtils.toBean(createReqVO, OaWorkReportDO.class)
                .setNo(no)
                .setTitle(buildTitle(createReqVO.getTitle(), createReqVO.getType(), createReqVO.getStartTime()))
                .setStatus(OaWorkReportStatusEnum.DRAFT.getStatus())
                .setDeptId(user != null ? user.getDeptId() : null);
        workReportMapper.insert(workReport);
        return workReport.getId();
    }

    @Override
    public void updateWorkReport(OaWorkReportSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验工作汇报属于当前用户
        OaWorkReportDO workReport = validateWorkReportOwner(updateReqVO.getId(), userId);
        // 1.2 校验工作汇报处于草稿状态
        validateWorkReportStatus(workReport, OaWorkReportStatusEnum.DRAFT);

        // 2. 更新工作汇报草稿
        workReportMapper.updateById(BeanUtils.toBean(updateReqVO, OaWorkReportDO.class)
                .setTitle(buildTitle(updateReqVO.getTitle(), updateReqVO.getType(), updateReqVO.getStartTime())));
    }

    @Override
    public void deleteWorkReport(Long id, Long userId) {
        // 1.1 校验工作汇报属于当前用户
        OaWorkReportDO workReport = validateWorkReportOwner(id, userId);
        // 1.2 校验工作汇报处于草稿状态
        validateWorkReportStatus(workReport, OaWorkReportStatusEnum.DRAFT);

        // 2. 删除工作汇报草稿
        workReportMapper.deleteById(id);
    }

    @Override
    public void submitWorkReport(Long id, Long userId) {
        // 1.1 校验工作汇报属于当前用户
        OaWorkReportDO workReport = validateWorkReportOwner(id, userId);
        // 1.2 校验工作汇报处于草稿状态
        validateWorkReportStatus(workReport, OaWorkReportStatusEnum.DRAFT);

        // 2. 提交工作汇报
        workReportMapper.updateById(new OaWorkReportDO().setId(id)
                .setStatus(OaWorkReportStatusEnum.SUBMITTED.getStatus()));
    }

    @Override
    public void cancelWorkReport(Long id, Long userId) {
        // 1.1 校验工作汇报属于当前用户
        OaWorkReportDO workReport = validateWorkReportOwner(id, userId);
        // 1.2 校验工作汇报处于已提交状态
        validateWorkReportStatus(workReport, OaWorkReportStatusEnum.SUBMITTED);

        // 2. 取消提交工作汇报
        workReportMapper.updateById(new OaWorkReportDO().setId(id)
                .setStatus(OaWorkReportStatusEnum.DRAFT.getStatus()));
    }

    @Override
    public OaWorkReportDO getWorkReport(Long id, Long userId) {
        // 1. 校验工作汇报存在
        OaWorkReportDO workReport = validateWorkReportExists(id);
        if (ObjUtil.equal(workReport.getCreator(), userId.toString())) {
            return workReport;
        }

        // 2. 管理范围只开放已提交的汇报，不公开员工草稿
        List<AdminUserRespDTO> subordinateUsers = adminUserApi.getUserListBySubordinate(userId);
        Set<Long> subordinateUserIds = convertSet(subordinateUsers, AdminUserRespDTO::getId);
        if (!permissionApi.hasAnyPermissions(userId, WORK_REPORT_STATISTICS_PERMISSION)
                || ObjUtil.notEqual(workReport.getStatus(), OaWorkReportStatusEnum.SUBMITTED.getStatus())
                || !subordinateUserIds.contains(NumberUtils.parseLong(workReport.getCreator()))) {
            throw exception(WORK_REPORT_ACCESS_DENIED);
        }
        return workReport;
    }

    @Override
    public PageResult<OaWorkReportDO> getWorkReportPage(OaWorkReportPageReqVO pageReqVO, Long userId) {
        return workReportMapper.selectPage(pageReqVO, userId);
    }

    @Override
    public OaWorkReportStatisticsRespVO getWorkReportStatistics(OaWorkReportStatisticsReqVO reqVO, Long userId) {
        // 1.1 按所选日期范围生成应填周期，包含范围内的未来日期
        List<String> expectedPeriodKeys = buildPeriodKeys(reqVO.getType(), reqVO.getStartTime(), reqVO.getEndTime());
        // 1.2 查询管理范围内的员工
        List<AdminUserRespDTO> users = getStatisticsUsers(reqVO, userId);

        // 2. 按完整自然周期查询，避免统计从周中或月中开始时漏掉汇报
        List<OaWorkReportDO> workReports = CollUtil.isEmpty(users)
                ? Collections.emptyList()
                : workReportMapper.selectListByCreatorsAndTypeAndStatusAndStartTimeBetween(
                        convertList(users, user -> user.getId().toString()), reqVO.getType(),
                        OaWorkReportStatusEnum.SUBMITTED.getStatus(),
                        reqVO.getQueryStartTime(), reqVO.getQueryEndTime());

        // 3. 汇总每位员工的已填、未填及汇报明细
        Map<Long, List<OaWorkReportDO>> userWorkReportMap = convertMultiMap(workReports,
                report -> NumberUtils.parseLong(report.getCreator()));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(users, AdminUserRespDTO::getDeptId));
        List<OaWorkReportStatisticsRespVO.UserStatistics> userStatisticsList = convertList(users, user ->
                buildUserStatistics(user, deptMap.get(user.getDeptId()), userWorkReportMap.getOrDefault(user.getId(), Collections.emptyList()), expectedPeriodKeys));

        // 4. 汇总整体统计
        int expectedCount = userStatisticsList.stream().mapToInt(OaWorkReportStatisticsRespVO.UserStatistics::getExpectedCount).sum();
        int submittedCount = userStatisticsList.stream().mapToInt(OaWorkReportStatisticsRespVO.UserStatistics::getSubmittedCount).sum();
        return new OaWorkReportStatisticsRespVO()
                .setUserCount(users.size()).setExpectedCount(expectedCount)
                .setSubmittedCount(submittedCount).setMissingCount(Math.max(0, expectedCount - submittedCount))
                .setUsers(userStatisticsList);
    }

    /**
     * 获得汇报统计范围内的员工
     *
     * @param reqVO 统计条件
     * @param userId 当前用户编号
     * @return 管理范围内符合部门和启用状态条件的员工列表
     */
    private List<AdminUserRespDTO> getStatisticsUsers(OaWorkReportStatisticsReqVO reqVO, Long userId) {
        // 1. 查询所选部门及子部门范围，未选部门时不限制部门
        Set<Long> deptIds = null;
        if (reqVO.getDeptId() != null) {
            List<DeptRespDTO> childDepts = deptApi.getChildDeptList(reqVO.getDeptId());
            deptIds = convertSet(childDepts, DeptRespDTO::getId);
            deptIds.add(reqVO.getDeptId());
        }

        // 2.1 查询当前用户管理范围内的员工
        List<AdminUserRespDTO> subordinateUsers = adminUserApi.getUserListBySubordinate(userId);
        // 2.2 保留启用且属于所选部门的员工，按用户编号稳定排序
        Set<Long> finalDeptIds = deptIds;
        List<AdminUserRespDTO> users = filterList(subordinateUsers, user ->
                CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus())
                        && (finalDeptIds == null || finalDeptIds.contains(user.getDeptId())));
        users.sort(Comparator.comparing(AdminUserRespDTO::getId));
        return users;
    }

    /**
     * 创建员工汇报统计
     *
     * @param user 员工信息
     * @param dept 部门信息
     * @param workReports 员工已提交的汇报列表
     * @param expectedPeriodKeys 应填周期列表
     * @return 员工汇报统计及明细
     */
    private OaWorkReportStatisticsRespVO.UserStatistics buildUserStatistics(AdminUserRespDTO user, DeptRespDTO dept,
                                                                             List<OaWorkReportDO> workReports,
                                                                             List<String> expectedPeriodKeys) {
        // 1. 按周期去重统计已填数量，并计算未填周期
        Set<String> submittedPeriodKeys = convertSet(workReports, report -> OaWorkReportTypeEnum.valueOf(report.getType())
                .formatPeriod(report.getStartTime()));
        List<String> missingPeriodKeys = filterList(expectedPeriodKeys, periodKey -> !submittedPeriodKeys.contains(periodKey));
        int submittedCount = expectedPeriodKeys.size() - missingPeriodKeys.size();

        // 2. 转换已提交汇报明细，同一周期的多份汇报均保留展示
        List<OaWorkReportStatisticsRespVO.Report> submittedReports = convertList(workReports, workReport ->
                new OaWorkReportStatisticsRespVO.Report().setId(workReport.getId()).setNo(workReport.getNo())
                        .setTitle(workReport.getTitle()).setStatus(workReport.getStatus())
                        .setStartTime(workReport.getStartTime()).setCreateTime(workReport.getCreateTime())
                        .setPeriodKey(OaWorkReportTypeEnum.valueOf(workReport.getType()).formatPeriod(workReport.getStartTime())));

        // 3. 拼接员工、部门及统计数量
        return new OaWorkReportStatisticsRespVO.UserStatistics()
                .setUserId(user.getId()).setUserName(user.getNickname())
                .setDeptId(user.getDeptId()).setDeptName(dept != null ? dept.getName() : null)
                .setExpectedCount(expectedPeriodKeys.size()).setSubmittedCount(submittedCount)
                .setMissingCount(Math.max(0, expectedPeriodKeys.size() - submittedCount))
                .setSubmittedReports(submittedReports).setMissingPeriodKeys(missingPeriodKeys);
    }

    /**
     * 生成统计范围内的周期标识
     *
     * @param type 汇报类型
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @return 去重后的应填周期列表，日报不包含周末
     */
    private List<String> buildPeriodKeys(Integer type, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 按自然日统一到零点，避免时分秒不同导致末日漏计
        LocalDateTime dayStartTime = LocalDateTimeUtils.getDayBeginTime(startTime);
        LocalDateTime dayEndTime = LocalDateTimeUtils.getDayBeginTime(endTime);
        if (dayStartTime.isAfter(dayEndTime)) {
            return Collections.emptyList();
        }

        // 2. 遍历自然日，日报排除周末，周报、月报按所属周期去重
        Set<String> periodKeys = new LinkedHashSet<>();
        for (LocalDateTime dateTime = dayStartTime; !dateTime.isAfter(dayEndTime); dateTime = dateTime.plusDays(1)) {
            if (OaWorkReportTypeEnum.DAILY.getType().equals(type)) {
                if (dateTime.getDayOfWeek() != DayOfWeek.SATURDAY && dateTime.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    periodKeys.add(dateTime.toLocalDate().toString());
                }
            } else if (OaWorkReportTypeEnum.WEEKLY.getType().equals(type)) {
                periodKeys.add(buildWeekPeriodKey(dateTime));
            } else {
                periodKeys.add(YearMonth.from(dateTime).toString());
            }
        }
        return new ArrayList<>(periodKeys);
    }

    /**
     * 生成工作汇报的自然年周次
     *
     * @param dateTime 汇报时间
     * @return 自然年周次
     */
    private String buildWeekPeriodKey(LocalDateTime dateTime) {
        return OaWorkReportTypeEnum.WEEKLY.formatPeriod(dateTime);
    }

    /**
     * 生成默认汇报标题
     *
     * @param title 自定义标题
     * @param type 汇报类型
     * @param startTime 汇报开始时间
     * @return 自定义标题，未填写时返回所属周期及汇报类型
     */
    private String buildTitle(String title, Integer type, LocalDateTime startTime) {
        if (StrUtil.isNotBlank(title)) {
            return title;
        }
        OaWorkReportTypeEnum typeEnum = OaWorkReportTypeEnum.valueOf(type);
        return typeEnum.formatPeriod(startTime) + " 工作" + typeEnum.getName();
    }

    /**
     * 校验工作汇报属于指定用户
     *
     * @param id 汇报编号
     * @param userId 用户编号
     * @return 工作汇报
     */
    private OaWorkReportDO validateWorkReportOwner(Long id, Long userId) {
        OaWorkReportDO workReport = validateWorkReportExists(id);
        if (ObjUtil.notEqual(workReport.getCreator(), userId.toString())) {
            throw exception(WORK_REPORT_ACCESS_DENIED);
        }
        return workReport;
    }

    /**
     * 校验工作汇报存在
     *
     * @param id 汇报编号
     * @return 工作汇报
     */
    private OaWorkReportDO validateWorkReportExists(Long id) {
        OaWorkReportDO workReport = workReportMapper.selectById(id);
        if (workReport == null) {
            throw exception(WORK_REPORT_NOT_EXISTS);
        }
        return workReport;
    }

    /**
     * 校验工作汇报状态
     *
     * @param workReport 工作汇报
     * @param status 预期状态
     */
    private void validateWorkReportStatus(OaWorkReportDO workReport, OaWorkReportStatusEnum status) {
        if (ObjUtil.notEqual(workReport.getStatus(), status.getStatus())) {
            throw exception(WORK_REPORT_STATUS_INVALID);
        }
    }

}
