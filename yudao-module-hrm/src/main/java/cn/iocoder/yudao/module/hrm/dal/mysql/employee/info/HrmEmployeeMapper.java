package cn.iocoder.yudao.module.hrm.dal.mysql.employee.info;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeContractDO;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusTabEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeSurveyTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTodoTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeArchiveFieldEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Mapper
public interface HrmEmployeeMapper extends BaseMapperX<HrmEmployeeDO> {

    default HrmEmployeeDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(HrmEmployeeDO::getId, id);
    }

    default PageResult<HrmEmployeeDO> selectPage(HrmEmployeePageReqVO reqVO) {
        MPJLambdaWrapperX<HrmEmployeeDO> query = buildQueryWrapper(reqVO);
        appendStatusCategory(query, reqVO);
        appendSurveyType(query, reqVO);
        appendTodoType(query, reqVO);
        PageUtils.buildDefaultSortingField(reqVO, HrmEmployeeDO::getId);
        return selectPage(reqVO, query);
    }

    default List<HrmEmployeeDO> selectList(HrmEmployeePageReqVO reqVO) {
        MPJLambdaWrapperX<HrmEmployeeDO> query = buildQueryWrapper(reqVO);
        appendStatusCategory(query, reqVO);
        appendSurveyType(query, reqVO);
        appendTodoType(query, reqVO);
        return selectList(query.orderByDesc(HrmEmployeeDO::getId));
    }

    default HrmEmployeeDO selectByJobNumber(String jobNumber) {
        return selectLastOne(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getJobNumber, jobNumber)
                .orderByAsc(HrmEmployeeDO::getId));
    }

    default List<HrmEmployeeDO> selectListByEntryStatus(Collection<Integer> entryStatuses) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .in(HrmEmployeeDO::getEntryStatus, entryStatuses)
                .orderByDesc(HrmEmployeeDO::getId));
    }

    default List<HrmEmployeeDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .in(HrmEmployeeDO::getDeptId, deptIds)
                .orderByDesc(HrmEmployeeDO::getId));
    }

    default List<HrmEmployeeDO> selectListByLeaderEmployeeId(Long leaderEmployeeId) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getLeaderEmployeeId, leaderEmployeeId)
                .orderByAsc(HrmEmployeeDO::getName)
                .orderByDesc(HrmEmployeeDO::getId));
    }

    default HrmEmployeeDO selectByUserId(Long userId) {
        return selectLastOne(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getUserId, userId)
                .orderByAsc(HrmEmployeeDO::getId));
    }

    default List<HrmEmployeeDO> selectListWithUserId() {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .select(HrmEmployeeDO::getUserId)
                .isNotNull(HrmEmployeeDO::getUserId));
    }

    default HrmEmployeeDO selectByMobile(String mobile) {
        return selectLastOne(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getMobile, mobile)
                .orderByAsc(HrmEmployeeDO::getId));
    }

    default HrmEmployeeDO selectByCandidateId(Long candidateId) {
        return selectLastOne(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getCandidateId, candidateId)
                .orderByAsc(HrmEmployeeDO::getId));
    }

    default List<HrmEmployeeDO> selectListByCandidateIds(Collection<Long> candidateIds) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .in(HrmEmployeeDO::getCandidateId, candidateIds));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateChannelIdByChannelId(Long channelId, Long newChannelId) {
        return update(new LambdaUpdateWrapper<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getChannelId, channelId)
                .set(HrmEmployeeDO::getChannelId, newChannelId));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateLeaveTimeById(Long id, LocalDateTime leaveTime) {
        return update(new LambdaUpdateWrapper<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getId, id)
                .set(HrmEmployeeDO::getLeaveTime, leaveTime));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updatePositionById(Long id, Long deptId, String postName, String postLevel,
                                   String workAddress, Long leaderEmployeeId) {
        return update(new LambdaUpdateWrapper<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getId, id)
                .set(HrmEmployeeDO::getDeptId, deptId)
                .set(HrmEmployeeDO::getPostName, postName)
                .set(HrmEmployeeDO::getPostLevel, postLevel)
                .set(HrmEmployeeDO::getWorkAddress, workAddress)
                .set(HrmEmployeeDO::getLeaderEmployeeId, leaderEmployeeId));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateForEntryById(HrmEmployeeDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<HrmEmployeeDO>()
                .eq(HrmEmployeeDO::getId, updateObj.getId())
                .set(updateObj.getDeptId() == null, HrmEmployeeDO::getDeptId, null)
                .set(updateObj.getPostName() == null, HrmEmployeeDO::getPostName, null)
                .set(updateObj.getPostLevel() == null, HrmEmployeeDO::getPostLevel, null)
                .set(updateObj.getWorkAddress() == null, HrmEmployeeDO::getWorkAddress, null)
                .set(updateObj.getLeaderEmployeeId() == null, HrmEmployeeDO::getLeaderEmployeeId, null)
                .set(updateObj.getRegularTime() == null, HrmEmployeeDO::getRegularTime, null)
                .set(HrmEmployeeDO::getLeaveTime, null));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateProfile(HrmEmployeeDO updateObj, Set<String> updateFields) {
        return update(new LambdaUpdateWrapper<HrmEmployeeDO>()
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.NAME.getName()), HrmEmployeeDO::getName, updateObj.getName())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.MOBILE.getName()), HrmEmployeeDO::getMobile, updateObj.getMobile())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.COUNTRY.getName()), HrmEmployeeDO::getCountry, updateObj.getCountry())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.NATION.getName()), HrmEmployeeDO::getNation, updateObj.getNation())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.ID_TYPE.getName()), HrmEmployeeDO::getIdType, updateObj.getIdType())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.ID_NUMBER.getName()), HrmEmployeeDO::getIdNumber, updateObj.getIdNumber())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.SEX.getName()), HrmEmployeeDO::getSex, updateObj.getSex())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.EMAIL.getName()), HrmEmployeeDO::getEmail, updateObj.getEmail())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.NATIVE_PLACE.getName()), HrmEmployeeDO::getNativePlace, updateObj.getNativePlace())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.BIRTHDAY.getName()), HrmEmployeeDO::getBirthday, updateObj.getBirthday())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.AGE.getName()), HrmEmployeeDO::getAge, updateObj.getAge())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.ADDRESS.getName()), HrmEmployeeDO::getAddress, updateObj.getAddress())
                .set(updateFields.contains(HrmEmployeeArchiveFieldEnum.HIGHEST_EDUCATION.getName()), HrmEmployeeDO::getHighestEducation, updateObj.getHighestEducation())
                .eq(HrmEmployeeDO::getId, updateObj.getId()));
    }

    default List<Map<String, Object>> selectCountListByStatus(HrmEmployeePageReqVO reqVO) {
        MPJLambdaWrapperX<HrmEmployeeDO> query = buildQueryWrapper(reqVO);
        appendSurveyType(query, reqVO);
        appendTodoType(query, reqVO);
        return selectMaps(query.selectAs(HrmEmployeeDO::getStatus, "status")
                .selectAs(HrmEmployeeDO::getEntryStatus, "entryStatus")
                .selectFunc("COUNT(DISTINCT %s)",
                        arg -> arg.accept(HrmEmployeeDO::getId), "count")
                .groupBy(HrmEmployeeDO::getStatus, HrmEmployeeDO::getEntryStatus));
    }

    default List<Map<String, Object>> selectCountListByDeptIdAndType(Collection<Integer> entryStatuses) {
        return selectMaps(new MPJLambdaWrapperX<HrmEmployeeDO>()
                .selectAs(HrmEmployeeDO::getDeptId, "deptId")
                .selectAs(HrmEmployeeDO::getType, "type")
                .selectCount(HrmEmployeeDO::getId, "count")
                .in(HrmEmployeeDO::getEntryStatus, entryStatuses)
                .groupBy(HrmEmployeeDO::getDeptId, HrmEmployeeDO::getType));
    }

    default List<Map<String, Object>> selectCountListByEntryStatusAndEntryTimeBetween(
            LocalDateTime[] entryTimes) {
        MPJLambdaWrapperX<HrmEmployeeDO> query = new MPJLambdaWrapperX<>();
        query.selectAs(HrmEmployeeDO::getEntryStatus, "entryStatus")
                .selectCount(HrmEmployeeDO::getId, "count")
                .isNotNull(HrmEmployeeDO::getEntryStatus);
        query.betweenIfPresent(HrmEmployeeDO::getEntryTime, entryTimes);
        query.groupBy(HrmEmployeeDO::getEntryStatus);
        return selectMaps(query);
    }

    default List<Map<String, Object>> selectCountListByEntryStatusAndLeaveTimeBetween(
            LocalDateTime[] leaveTimes) {
        MPJLambdaWrapperX<HrmEmployeeDO> query = new MPJLambdaWrapperX<>();
        query.selectAs(HrmEmployeeDO::getEntryStatus, "entryStatus")
                .selectCount(HrmEmployeeDO::getId, "count")
                .in(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.LEFT.getStatus(), HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus());
        query.betweenIfPresent(HrmEmployeeDO::getLeaveTime, leaveTimes);
        query.groupBy(HrmEmployeeDO::getEntryStatus);
        return selectMaps(query);
    }

    default List<Map<String, Object>> selectCountListByChangeTypeAndEffectTimeBetween(
            LocalDateTime[] effectTimes) {
        MPJLambdaWrapperX<HrmEmployeeDO> query = new MPJLambdaWrapperX<>();
        query.selectAs(HrmEmployeeChangeRecordDO::getType, "changeType")
                .selectFunc("COUNT(DISTINCT %s)",
                        arg -> arg.accept(HrmEmployeeDO::getId), "count")
                .innerJoin(HrmEmployeeChangeRecordDO.class, HrmEmployeeChangeRecordDO::getEmployeeId,
                        HrmEmployeeDO::getId)
                .in(HrmEmployeeChangeRecordDO::getType,
                        HrmEmployeeChangeTypeEnum.REGULAR.getType(),
                        HrmEmployeeChangeTypeEnum.TRANSFER.getType());
        query.betweenIfPresent(HrmEmployeeChangeRecordDO::getEffectTime, effectTimes);
        query.groupBy(HrmEmployeeChangeRecordDO::getType);
        return selectMaps(query);
    }

    default List<HrmEmployeeDO> selectListByRegularTimeBeforeOrEqual(LocalDateTime deadlineTime) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeDO>()
                .le(HrmEmployeeDO::getRegularTime, deadlineTime)
                .in(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES)
                .eq(HrmEmployeeDO::getStatus, HrmEmployeeStatusEnum.PROBATION.getStatus())
                .orderByAsc(HrmEmployeeDO::getRegularTime)
                .orderByAsc(HrmEmployeeDO::getId));
    }

    /**
     * 追加员工状态筛选条件。状态页签包含入职状态与员工状态的组合语义；
     * 未选择页签时，使用请求中的原始入职状态和员工状态条件。
     */
    default void appendStatusCategory(MPJLambdaWrapperX<HrmEmployeeDO> queryWrapper, HrmEmployeePageReqVO reqVO) {
        Integer statusCategory = reqVO.getStatusCategory();
        if (statusCategory == null) {
            queryWrapper.eqIfPresent(HrmEmployeeDO::getEntryStatus, reqVO.getEntryStatus())
                    .eqIfPresent(HrmEmployeeDO::getStatus, reqVO.getStatus());
            return;
        }
        HrmEmployeeStatusTabEnum statusTab = HrmEmployeeStatusTabEnum.valueOf(statusCategory);
        if (HrmEmployeeStatusTabEnum.ACTIVE == statusTab) {
            queryWrapper.in(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES);
        } else if (HrmEmployeeStatusTabEnum.FULL_TIME == statusTab) {
            queryWrapper.in(HrmEmployeeDO::getStatus, HrmEmployeeStatusEnum.FULL_TIME_STATUSES)
                    .in(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES);
        } else if (HrmEmployeeStatusTabEnum.PENDING_ENTRY == statusTab) {
            queryWrapper.eq(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus());
        } else if (HrmEmployeeStatusTabEnum.PENDING_LEAVE == statusTab) {
            queryWrapper.eq(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus());
        } else if (HrmEmployeeStatusTabEnum.LEFT == statusTab) {
            queryWrapper.eq(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.LEFT.getStatus());
        } else {
            queryWrapper.eq(HrmEmployeeDO::getStatus, statusCategory)
                    .in(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES);
        }
    }

    /**
     * 追加当月员工待办筛选条件。
     */
    default void appendTodoType(MPJLambdaWrapperX<HrmEmployeeDO> query, HrmEmployeePageReqVO reqVO) {
        Integer todoType = reqVO.getTodoType();
        if (todoType == null) {
            return;
        }
        LocalDate now = LocalDate.now();
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(
                now.getYear(), now.getMonthValue());
        if (HrmEmployeeTodoTypeEnum.PENDING_LEAVE.getType().equals(todoType)) {
            query.betweenIfPresent(HrmEmployeeDO::getLeaveTime, monthTimes);
        } else if (HrmEmployeeTodoTypeEnum.CONTRACT_EXPIRE.getType().equals(todoType)) {
            query.innerJoin(HrmEmployeeContractDO.class, HrmEmployeeContractDO::getEmployeeId,
                    HrmEmployeeDO::getId).distinct();
            query.betweenIfPresent(HrmEmployeeContractDO::getEndTime, monthTimes);
        } else if (HrmEmployeeTodoTypeEnum.REGULAR.getType().equals(todoType)) {
            query.eq(HrmEmployeeDO::getStatus, HrmEmployeeStatusEnum.PROBATION.getStatus());
            query.betweenIfPresent(HrmEmployeeDO::getRegularTime, monthTimes);
        } else if (HrmEmployeeTodoTypeEnum.PENDING_ENTRY.getType().equals(todoType)) {
            query.betweenIfPresent(HrmEmployeeDO::getEntryTime, monthTimes);
        } else if (HrmEmployeeTodoTypeEnum.BIRTHDAY.getType().equals(todoType)) {
            // MySQL 和 H2 MySQL 模式均支持 MONTH，用于跨年筛选当月生日。
            query.apply("MONTH(birthday) = {0}", now.getMonthValue());
            if (now.getMonthValue() == 2 && !now.isLeapYear()) {
                // 非闰年不存在 2 月 29 日，与首页日历和年龄计算保持一致。
                query.apply("DAYOFMONTH(birthday) <> 29");
            }
        }
    }

    /**
     * 追加当月首页员工概览筛选条件。
     */
    default void appendSurveyType(MPJLambdaWrapperX<HrmEmployeeDO> query, HrmEmployeePageReqVO reqVO) {
        Integer surveyType = reqVO.getSurveyType();
        if (surveyType == null) {
            return;
        }
        LocalDate now = LocalDate.now();
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(
                now.getYear(), now.getMonthValue());
        if (HrmEmployeeSurveyTypeEnum.ENTRY.getType().equals(surveyType)) {
            query.ne(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus());
            query.betweenIfPresent(HrmEmployeeDO::getEntryTime, monthTimes);
        } else if (HrmEmployeeSurveyTypeEnum.LEAVE.getType().equals(surveyType)) {
            query.eq(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.LEFT.getStatus());
            query.betweenIfPresent(HrmEmployeeDO::getLeaveTime, monthTimes);
        } else if (HrmEmployeeSurveyTypeEnum.REGULAR.getType().equals(surveyType)) {
            appendChangeRecordSurvey(query, HrmEmployeeChangeTypeEnum.REGULAR.getType(), monthTimes);
        } else if (HrmEmployeeSurveyTypeEnum.TRANSFER.getType().equals(surveyType)) {
            appendChangeRecordSurvey(query, HrmEmployeeChangeTypeEnum.TRANSFER.getType(), monthTimes);
        } else if (HrmEmployeeSurveyTypeEnum.PENDING_ENTRY.getType().equals(surveyType)) {
            query.eq(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus());
            query.betweenIfPresent(HrmEmployeeDO::getEntryTime, monthTimes);
        } else if (HrmEmployeeSurveyTypeEnum.PENDING_LEAVE.getType().equals(surveyType)) {
            query.eq(HrmEmployeeDO::getEntryStatus, HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus());
            query.betweenIfPresent(HrmEmployeeDO::getLeaveTime, monthTimes);
        }
    }

    /**
     * 根据当月生效的指定异动记录筛选员工。
     */
    default void appendChangeRecordSurvey(MPJLambdaWrapperX<HrmEmployeeDO> query, Integer changeType,
                                          LocalDateTime[] monthTimes) {
        query.innerJoin(HrmEmployeeChangeRecordDO.class, HrmEmployeeChangeRecordDO::getEmployeeId,
                        HrmEmployeeDO::getId)
                .eq(HrmEmployeeChangeRecordDO::getType, changeType)
                .distinct();
        query.betweenIfPresent(HrmEmployeeChangeRecordDO::getEffectTime, monthTimes);
    }

    default MPJLambdaWrapperX<HrmEmployeeDO> buildQueryWrapper(HrmEmployeePageReqVO reqVO) {
        MPJLambdaWrapperX<HrmEmployeeDO> query = new MPJLambdaWrapperX<HrmEmployeeDO>()
                .inIfPresent(HrmEmployeeDO::getId, reqVO.getIds())
                .likeIfPresent(HrmEmployeeDO::getName, reqVO.getName())
                .likeIfPresent(HrmEmployeeDO::getJobNumber, reqVO.getJobNumber())
                .likeIfPresent(HrmEmployeeDO::getMobile, reqVO.getMobile())
                .eqIfPresent(HrmEmployeeDO::getSex, reqVO.getSex())
                .eqIfPresent(HrmEmployeeDO::getDeptId, reqVO.getDeptId())
                .inIfPresent(HrmEmployeeDO::getDeptId, reqVO.getDeptIds())
                .eqIfPresent(HrmEmployeeDO::getLeaderEmployeeId, reqVO.getLeaderEmployeeId())
                .likeIfPresent(HrmEmployeeDO::getPostName, reqVO.getPostName())
                .likeIfPresent(HrmEmployeeDO::getWorkAddress, reqVO.getWorkAddress())
                .eqIfPresent(HrmEmployeeDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(HrmEmployeeDO::getType, reqVO.getType());
        if (CollUtil.isNotEmpty(reqVO.getExcludeIds())) {
            query.notIn(HrmEmployeeDO::getId, reqVO.getExcludeIds());
        }
        query.betweenIfPresent(HrmEmployeeDO::getEntryTime, reqVO.getEntryTime());
        query.betweenIfPresent(HrmEmployeeDO::getRegularTime, reqVO.getRegularTime());
        query.leIfPresent(HrmEmployeeDO::getEntryTime, ArrayUtils.get(reqVO.getActiveTime(), 1));
        LocalDateTime activeBeginTime = ArrayUtils.get(reqVO.getActiveTime(), 0);
        if (activeBeginTime != null) {
            query.and(wrapper -> wrapper.isNull(HrmEmployeeDO::getLeaveTime)
                    .or().ge(HrmEmployeeDO::getLeaveTime, activeBeginTime));
        }
        if (StrUtil.isNotBlank(reqVO.getSearch())) {
            query.and(wrapper -> wrapper.like(HrmEmployeeDO::getName, reqVO.getSearch())
                    .or().like(HrmEmployeeDO::getJobNumber, reqVO.getSearch())
                    .or().like(HrmEmployeeDO::getMobile, reqVO.getSearch()));
        }
        return query;
    }

}
