package cn.iocoder.yudao.module.oa.service.meetingroom;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.*;
import cn.iocoder.yudao.module.oa.dal.mysql.meetingroom.OaMeetingRoomBookingMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.meetingroom.*;
import cn.iocoder.yudao.module.oa.enums.meetingroom.OaMeetingRoomUseStatusEnum;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getMaxValue;
import static cn.iocoder.yudao.module.oa.enums.BpmModelConstants.MEETING_ROOM_BOOKING;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.oa.enums.MessageTemplateConstants.MEETING_ROOM_BOOKING_REMINDER;

/**
 * 会议室预定 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class OaMeetingRoomBookingServiceImpl implements OaMeetingRoomBookingService {

    @Resource
    private OaMeetingRoomBookingMapper meetingRoomBookingMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaMeetingRoomService meetingRoomService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private OaNoRedisDAO noRedisDAO;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public Long createMeetingRoomBooking(OaMeetingRoomBookingSaveReqVO createReqVO, Long userId) {
        // 1.1 校验房间、时间和会议人员
        validateMeetingRoomBooking(createReqVO);
        // 1.2 查询申请人所属部门
        AdminUserRespDTO user = adminUserApi.validateUser(userId);
        // 1.3 生成单号并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.MEETING_ROOM_BOOKING_NO_PREFIX);
        if (meetingRoomBookingMapper.selectByNo(no) != null) {
            throw exception(MEETING_ROOM_BOOKING_NO_DUPLICATE);
        }

        // 2. 创建草稿
        OaMeetingRoomBookingDO booking = BeanUtils.toBean(createReqVO, OaMeetingRoomBookingDO.class)
                .setNo(no).setReminded(false)
                .setDeptId(user.getDeptId()).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus()).setUseStatus(OaMeetingRoomUseStatusEnum.PENDING.getStatus());
        meetingRoomBookingMapper.insert(booking);
        return booking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMeetingRoomBooking(OaMeetingRoomBookingSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验本人草稿
        OaMeetingRoomBookingDO booking = validateBookingExists(updateReqVO.getId());
        validateBookingOwner(booking, userId);
        validateBookingDraft(booking);
        // 1.2 校验会议内容
        validateMeetingRoomBooking(updateReqVO);

        // 2. 更新草稿
        meetingRoomBookingMapper.updateById(BeanUtils.toBean(updateReqVO, OaMeetingRoomBookingDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMeetingRoomBooking(Long id, Long userId) {
        // 1.1 校验申请归属
        OaMeetingRoomBookingDO booking = validateBookingExists(id);
        validateBookingOwner(booking, userId);
        // 1.2 校验草稿状态
        validateBookingDraft(booking);

        // 2. 删除草稿
        meetingRoomBookingMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitMeetingRoomBooking(Long id, Long userId) {
        // 1.1 校验预定和会议室存在
        OaMeetingRoomBookingDO booking = validateBookingExists(id);
        OaMeetingRoomDO room = meetingRoomService.validateMeetingRoomExists(booking.getRoomId());
        // 1.2 校验申请归属与草稿状态，阻止重复提交
        validateBookingOwner(booking, userId);
        validateBookingDraft(booking);
        // 1.3 校验房间状态与预定成员范围
        if (!Boolean.TRUE.equals(room.getAllowBooking())
                || ObjUtil.notEqual(room.getStatus(), OaMeetingRoomStatusEnum.NORMAL.getStatus())
                || (ObjUtil.equal(room.getBookingScope(), OaMeetingRoomBookingScopeEnum.SPECIFIED.getScope())
                    && !CollUtil.contains(room.getBookingUserIds(), userId))) {
            throw exception(MEETING_ROOM_NOT_AVAILABLE);
        }
        // 1.4 提交时校验时间及人员仍有效
        validateMeetingRoomBooking(BeanUtils.toBean(booking, OaMeetingRoomBookingSaveReqVO.class));
        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw exception(MEETING_ROOM_BOOKING_TIME_INVALID);
        }
        // 1.5 检查有效预约，采用左闭右开区间，相邻会议不冲突
        if (CollUtil.isNotEmpty(meetingRoomBookingMapper.selectListByRoomIdAndTimeOverlap(room.getId(),
                booking.getStartTime(), booking.getEndTime()))) {
            throw exception(MEETING_ROOM_BOOKING_TIME_CONFLICT);
        }

        // 2. 更新审批状态，免审批直接通过
        boolean needApproval = Boolean.TRUE.equals(room.getNeedApproval());
        meetingRoomBookingMapper.updateById(new OaMeetingRoomBookingDO().setId(id).setNeedApproval(needApproval)
                .setStatus(needApproval ? BpmProcessInstanceStatusEnum.RUNNING.getStatus() : BpmProcessInstanceStatusEnum.APPROVE.getStatus()).setUseStatus(OaMeetingRoomUseStatusEnum.PENDING.getStatus()));
        if (!needApproval) {
            return null;
        }

        // 3.1 发起审批
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(MEETING_ROOM_BOOKING)
                        .setBusinessKey(id.toString()).setVariables(new HashMap<>()));
        // 3.2 绑定流程编号
        meetingRoomBookingMapper.updateById(new OaMeetingRoomBookingDO().setId(id).setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMeetingRoomBooking(Long id, Long userId) {
        // 1. 校验本人预约
        OaMeetingRoomBookingDO booking = validateBookingExists(id);
        validateBookingOwner(booking, userId);

        // 2.1 审批中取消 BPM 流程
        if (ObjUtil.equal(booking.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            processInstanceApi.cancelProcessInstanceByStartUser(userId, booking.getProcessInstanceId(), "申请人取消会议室预定");
            return;
        }
        // 2.2 已通过且待使用时取消使用，保留审批结果
        if (ObjUtil.equal(booking.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                && ObjUtil.equal(booking.getUseStatus(), OaMeetingRoomUseStatusEnum.PENDING.getStatus())) {
            meetingRoomBookingMapper.updateById(new OaMeetingRoomBookingDO().setId(id).setUseStatus(OaMeetingRoomUseStatusEnum.CANCELLED.getStatus()));
            return;
        }
        throw exception(MEETING_ROOM_BOOKING_STATUS_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startMeetingRoomBooking(Long id, Long userId) {
        // 1.1 校验申请人或主持人可操作
        OaMeetingRoomBookingDO booking = validateBookingOperator(id, userId);
        // 1.2 校验审批通过、待使用及当前处于会议时段
        LocalDateTime now = LocalDateTime.now();
        if (ObjUtil.notEqual(booking.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                || ObjUtil.notEqual(booking.getUseStatus(), OaMeetingRoomUseStatusEnum.PENDING.getStatus())
                || now.isBefore(booking.getStartTime()) || !now.isBefore(booking.getEndTime())) {
            throw exception(MEETING_ROOM_BOOKING_STATUS_INVALID);
        }
        // 1.3 开始使用前确认会议室仍可用，审批期间可能已停用或维修
        OaMeetingRoomDO room = meetingRoomService.validateMeetingRoomExists(booking.getRoomId());
        if (Boolean.FALSE.equals(room.getAllowBooking())
                || ObjUtil.notEqual(room.getStatus(), OaMeetingRoomStatusEnum.NORMAL.getStatus())) {
            throw exception(MEETING_ROOM_NOT_AVAILABLE);
        }

        // 2. 开始使用
        meetingRoomBookingMapper.updateById(new OaMeetingRoomBookingDO().setId(id).setUseStatus(OaMeetingRoomUseStatusEnum.IN_USE.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishMeetingRoomBooking(Long id, Long userId) {
        // 1.1 校验操作人
        OaMeetingRoomBookingDO booking = validateBookingOperator(id, userId);
        // 1.2 校验使用中
        if (ObjUtil.notEqual(booking.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                || ObjUtil.notEqual(booking.getUseStatus(), OaMeetingRoomUseStatusEnum.IN_USE.getStatus())) {
            throw exception(MEETING_ROOM_BOOKING_STATUS_INVALID);
        }

        // 2. 完成使用并释放剩余时段
        meetingRoomBookingMapper.updateById(new OaMeetingRoomBookingDO().setId(id).setUseStatus(OaMeetingRoomUseStatusEnum.COMPLETED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMeetingRoomBookingStatus(Long id, Integer status) {
        // 1. 校验申请存在，已处理的审批结果不再重复更新业务状态
        OaMeetingRoomBookingDO booking = validateBookingExists(id);
        if (ObjUtil.notEqual(booking.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            return;
        }

        // 2. 回写结果，驳回、取消或已过结束时间的会议不再待使用
        meetingRoomBookingMapper.updateById(new OaMeetingRoomBookingDO().setId(id).setStatus(status)
                .setUseStatus(ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                        && booking.getEndTime().isAfter(LocalDateTime.now())
                        ? OaMeetingRoomUseStatusEnum.PENDING.getStatus() : OaMeetingRoomUseStatusEnum.CANCELLED.getStatus()));
    }

    @Override
    public OaMeetingRoomBookingDO getMeetingRoomBooking(Long id) {
        return meetingRoomBookingMapper.selectById(id);
    }

    @Override
    public PageResult<OaMeetingRoomBookingDO> getMeetingRoomBookingPage(Long userId, OaMeetingRoomBookingPageReqVO reqVO) {
        // 1. 查询关联字段，空匹配直接返回，防止空集合导致筛选失效
        List<Long> roomIds = null;
        if (StrUtil.isNotBlank(reqVO.getRoomName())) {
            roomIds = convertList(meetingRoomService.getMeetingRoomListByName(reqVO.getRoomName()), OaMeetingRoomDO::getId);
            if (CollUtil.isEmpty(roomIds)) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }
        List<Long> moderatorIds = null;
        if (StrUtil.isNotBlank(reqVO.getModeratorName())) {
            moderatorIds = convertList(adminUserApi.getUserListByNickname(reqVO.getModeratorName()), AdminUserRespDTO::getId);
            if (CollUtil.isEmpty(moderatorIds)) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }
        // 2. 查询本人预定
        return meetingRoomBookingMapper.selectPage(userId, reqVO, roomIds, moderatorIds);
    }

    @Override
    public List<OaMeetingRoomBookingDO> getMeetingRoomBookingListBySchedule(OaMeetingRoomBookingScheduleReqVO reqVO) {
        // 1. 校验房间和查询区间
        meetingRoomService.validateMeetingRoomExists(reqVO.getRoomId());
        validateMeetingTime(reqVO.getStartTime(), reqVO.getEndTime());
        // 2. 查询相交时段的预约
        return meetingRoomBookingMapper.selectListByRoomIdAndTimeOverlap(reqVO.getRoomId(), reqVO.getStartTime(), reqVO.getEndTime());
    }

    @Override
    public int cancelExpiredMeetingRoomBookings() {
        return meetingRoomBookingMapper.updateUseStatusByExpired(LocalDateTime.now());
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public int sendMeetingRoomBookingReminders() {
        // 1. 查询最长提前时间内的待提醒会议，再按各会议的提醒选项判断
        LocalDateTime remindTime = LocalDateTime.now();
        // 不提醒类型没有提前分钟数，不参与扫描窗口计算。
        List<OaMeetingRoomReminderTypeEnum> reminderTypes = filterList(
                Arrays.asList(OaMeetingRoomReminderTypeEnum.values()), type -> type.getMinutes() != null);
        if (CollUtil.isEmpty(reminderTypes)) {
            return 0;
        }
        // 一次查询覆盖所有可用提醒选项，具体发送时再检查各会议的提前时间。
        List<OaMeetingRoomBookingDO> bookings = meetingRoomBookingMapper.selectListByReminderTypesAndStartTime(
                convertList(reminderTypes, OaMeetingRoomReminderTypeEnum::getType), remindTime,
                remindTime.plusMinutes(getMaxValue(reminderTypes, OaMeetingRoomReminderTypeEnum::getMinutes)));

        // 2. 每个会议使用独立事务，单条失败不影响其他会议，下轮扫描重试
        int count = 0;
        for (OaMeetingRoomBookingDO booking : bookings) {
            try {
                count += getSelf().sendMeetingRoomBookingReminder(booking.getId(), remindTime) ? 1 : 0;
            } catch (Exception ex) {
                log.error("[sendMeetingRoomBookingReminders][会议 ({}) 提醒失败]", booking.getId(), ex);
            }
        }
        return count;
    }

    /**
     * 向申请人、主持人和参会人员发送会议开始提醒
     *
     * 保留 public 供自身代理调用，确保发送和提醒标记处于同一事务。
     *
     * @param id 预定编号
     * @param remindTime 提醒基准时间
     * @return 是否完成提醒，未到提醒时间或已处理时返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendMeetingRoomBookingReminder(Long id, LocalDateTime remindTime) {
        // 1. 校验提醒窗口，开始后不补发；刚好进入提前时间时允许提醒
        OaMeetingRoomBookingDO booking = meetingRoomBookingMapper.selectById(id);
        if (booking == null || Boolean.TRUE.equals(booking.getReminded())
                || ObjUtil.notEqual(booking.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                || ObjUtil.notEqual(booking.getUseStatus(), OaMeetingRoomUseStatusEnum.PENDING.getStatus())) {
            return false;
        }
        OaMeetingRoomReminderTypeEnum reminderType = OaMeetingRoomReminderTypeEnum.valueOf(booking.getReminderType());
        if (reminderType == null || reminderType.getMinutes() == null
                || !booking.getStartTime().isAfter(remindTime)
                || booking.getStartTime().isAfter(remindTime.plusMinutes(reminderType.getMinutes()))) {
            return false;
        }

        // 2.1 合并并去重接收人
        Set<Long> userIds = new LinkedHashSet<>();
        userIds.add(NumberUtils.parseLong(booking.getCreator()));
        userIds.add(booking.getModeratorUserId());
        CollUtil.addAll(userIds, booking.getAttendeeUserIds());
        userIds.remove(null);
        // 2.2 组装会议消息并发送站内信
        OaMeetingRoomDO room = meetingRoomService.validateMeetingRoomExists(booking.getRoomId());
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("title", booking.getTitle());
        templateParams.put("roomName", room.getName());
        templateParams.put("location", room.getLocation());
        templateParams.put("startTime", LocalDateTimeUtil.format(booking.getStartTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
        for (Long userId : userIds) {
            notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                    .setUserId(userId).setTemplateCode(MEETING_ROOM_BOOKING_REMINDER).setTemplateParams(templateParams));
        }

        // 3. 标记已通知
        meetingRoomBookingMapper.updateById(new OaMeetingRoomBookingDO().setId(id).setReminded(true));
        return true;
    }

    /**
     * 获得自身代理，使单条会议提醒的事务生效
     *
     * @return 当前 Service 的 Spring 代理
     */
    private OaMeetingRoomBookingServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public boolean hasActiveMeetingRoomBooking(Long roomId) {
        return meetingRoomBookingMapper.selectCountByRoomIdAndEndTimeAfter(roomId, LocalDateTime.now()) > 0;
    }

    /**
     * 校验预定存在
     *
     * @param id 预定编号
     * @return 预定
     */
    private OaMeetingRoomBookingDO validateBookingExists(Long id) {
        OaMeetingRoomBookingDO booking = meetingRoomBookingMapper.selectById(id);
        if (booking == null) {
            throw exception(MEETING_ROOM_BOOKING_NOT_EXISTS);
        }
        return booking;
    }

    /**
     * 校验会议内容
     *
     * @param reqVO 预定信息
     */
    private void validateMeetingRoomBooking(OaMeetingRoomBookingSaveReqVO reqVO) {
        // 1. 校验房间存在
        meetingRoomService.validateMeetingRoomExists(reqVO.getRoomId());
        // 2. 校验时间先后顺序
        validateMeetingTime(reqVO.getStartTime(), reqVO.getEndTime());
        // 3. 校验主持人与参会人员有效
        adminUserApi.validateUser(reqVO.getModeratorUserId());
        if (CollUtil.isNotEmpty(reqVO.getAttendeeUserIds())) {
            adminUserApi.validateUserList(reqVO.getAttendeeUserIds());
        }
    }

    /**
     * 校验会议时间
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    private void validateMeetingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw exception(MEETING_ROOM_BOOKING_TIME_INVALID);
        }
    }

    /**
     * 校验预定归属
     *
     * @param booking 预定
     * @param userId 用户编号
     */
    private void validateBookingOwner(OaMeetingRoomBookingDO booking, Long userId) {
        if (ObjUtil.notEqual(booking.getCreator(), userId.toString())) {
            throw exception(MEETING_ROOM_BOOKING_NOT_OWNER);
        }
    }

    /**
     * 校验预定草稿
     *
     * @param booking 预定
     */
    private void validateBookingDraft(OaMeetingRoomBookingDO booking) {
        if (ObjUtil.notEqual(booking.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(MEETING_ROOM_BOOKING_STATUS_INVALID);
        }
    }

    /**
     * 校验开始和完成使用的操作人
     *
     * @param id 预定编号
     * @param userId 用户编号
     * @return 预定
     */
    private OaMeetingRoomBookingDO validateBookingOperator(Long id, Long userId) {
        OaMeetingRoomBookingDO booking = validateBookingExists(id);
        if (ObjUtil.notEqual(booking.getCreator(), userId.toString()) && ObjUtil.notEqual(booking.getModeratorUserId(), userId)) {
            throw exception(MEETING_ROOM_BOOKING_NOT_OWNER);
        }
        return booking;
    }
}
