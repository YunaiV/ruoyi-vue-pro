package cn.iocoder.yudao.module.oa.service.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaSchedulePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaScheduleSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleParticipantDO;
import cn.iocoder.yudao.module.oa.dal.mysql.schedule.OaScheduleMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.schedule.OaScheduleParticipantMapper;
import cn.iocoder.yudao.module.oa.framework.config.OaProperties;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.SCHEDULE_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.SCHEDULE_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.MessageTemplateConstants.SCHEDULE_REMINDER;

/**
 * OA 日程 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class OaScheduleServiceImpl implements OaScheduleService {

    @Resource
    private OaProperties properties;

    @Resource
    private OaScheduleMapper scheduleMapper;
    @Resource
    private OaScheduleParticipantMapper scheduleParticipantMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSchedule(OaScheduleSaveReqVO createReqVO, Long userId) {
        // 1. 校验参与人
        adminUserApi.validateUserList(createReqVO.getParticipantUserIds());

        // 2. 新增日程
        OaScheduleDO schedule = BeanUtils.toBean(createReqVO, OaScheduleDO.class)
                .setReminded(false);
        scheduleMapper.insert(schedule);

        // 3. 新增参与人
        createScheduleParticipantList(schedule.getId(), createReqVO.getParticipantUserIds());
        return schedule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchedule(OaScheduleSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验日程属于当前用户
        validateScheduleOwner(updateReqVO.getId(), userId);
        // 1.2 校验参与人存在
        adminUserApi.validateUserList(updateReqVO.getParticipantUserIds());

        // 2. 修改日程
        scheduleMapper.updateById(BeanUtils.toBean(updateReqVO, OaScheduleDO.class).setReminded(false));
        // 编辑保存后重新进入提醒周期，不清空阅读回执

        // 3.1 查询参与关系并计算差异
        List<OaScheduleParticipantDO> oldParticipants = scheduleParticipantMapper.selectListByScheduleId(updateReqVO.getId());
        List<OaScheduleParticipantDO> newParticipants = convertList(CollUtil.distinct(updateReqVO.getParticipantUserIds()),
                user -> new OaScheduleParticipantDO().setScheduleId(updateReqVO.getId()).setUserId(user).setReadStatus(false));
        List<List<OaScheduleParticipantDO>> diffParticipants = diffList(oldParticipants, newParticipants,
                (oldParticipant, newParticipant) -> ObjectUtil.equal(oldParticipant.getUserId(), newParticipant.getUserId()));
        // 3.2 新增参与关系
        if (CollUtil.isNotEmpty(diffParticipants.get(0))) {
            scheduleParticipantMapper.insertBatch(diffParticipants.get(0));
        }
        // 3.3 删除取消的参与关系
        if (CollUtil.isNotEmpty(diffParticipants.get(2))) {
            scheduleParticipantMapper.deleteByScheduleIdAndUserIds(updateReqVO.getId(),
                    convertList(diffParticipants.get(2), OaScheduleParticipantDO::getUserId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchedule(Long id, Long userId) {
        // 1. 校验日程
        validateScheduleOwner(id, userId);

        // 2.1 删除日程参与人
        scheduleParticipantMapper.deleteByScheduleId(id);
        // 2.2 删除日程
        scheduleMapper.deleteById(id);
    }

    @Override
    public OaScheduleDO getSchedule(Long id, Long userId) {
        // 1. 校验日程存在
        OaScheduleDO schedule = validateScheduleExists(id);

        // 2. 创建人可直接访问
        if (ObjectUtil.equal(NumberUtils.parseLong(schedule.getCreator()), userId)) {
            return schedule;
        }

        // 3. 校验当前用户是日程参与人
        List<OaScheduleParticipantDO> participants = scheduleParticipantMapper.selectListByScheduleIds(
                Collections.singleton(id));
        if (!convertSet(participants, OaScheduleParticipantDO::getUserId).contains(userId)) {
            throw exception(SCHEDULE_ACCESS_DENIED);
        }
        return schedule;
    }

    @Override
    public PageResult<OaScheduleDO> getMySchedulePage(OaSchedulePageReqVO pageReqVO, Long userId) {
        return scheduleMapper.selectMyPage(pageReqVO, userId);
    }

    @Override
    public PageResult<OaScheduleDO> getSchedulePage(OaSchedulePageReqVO pageReqVO, Long userId) {
        // 1.1 未选择本人日程时，只查询共享日程或返回空列表
        if (Boolean.FALSE.equals(pageReqVO.getIncludeMine())) {
            return Boolean.TRUE.equals(pageReqVO.getIncludeReceived())
                    ? getReceivedSchedulePage(pageReqVO, userId) : PageResult.empty();
        }
        // 1.2 仅选择本人日程时，复用本人分页查询
        if (Boolean.FALSE.equals(pageReqVO.getIncludeReceived())) {
            return getMySchedulePage(pageReqVO, userId);
        }

        // 2. 查询本人参与关系，没有共享日程时只查本人日程
        List<OaScheduleParticipantDO> participants = scheduleParticipantMapper.selectListByUserId(userId);
        if (CollUtil.isEmpty(participants)) {
            return getMySchedulePage(pageReqVO, userId);
        }

        // 3. 按创建人或参与关系统一分页，同一日程只返回一次
        return scheduleMapper.selectPageByCreatorOrIds(pageReqVO, userId,
                convertList(participants, OaScheduleParticipantDO::getScheduleId));
    }

    @Override
    public void updateScheduleReadStatus(Long id, Long userId) {
        // 1.1 校验当前用户可访问日程
        getSchedule(id, userId);
        // 1.2 查询本人参与关系，创建人未参与或已经阅读时无需更新
        OaScheduleParticipantDO participant = scheduleParticipantMapper.selectByScheduleIdAndUserId(id, userId);
        if (participant == null || Boolean.TRUE.equals(participant.getReadStatus())) {
            return;
        }

        // 2. 标记本人已读，记录阅读时间
        scheduleParticipantMapper.updateById(new OaScheduleParticipantDO().setId(participant.getId())
                .setReadStatus(true).setReadTime(LocalDateTime.now()));
    }

    @Override
    public List<OaScheduleParticipantDO> getScheduleParticipantList(Long id) {
        return scheduleParticipantMapper.selectListByScheduleId(id);
    }

    @Override
    public PageResult<OaScheduleDO> getReceivedSchedulePage(OaSchedulePageReqVO pageReqVO, Long userId) {
        // 1. 查询当前用户的参与关系
        List<OaScheduleParticipantDO> participants = scheduleParticipantMapper.selectListByUserId(userId);
        if (CollUtil.isEmpty(participants)) {
            return PageResult.empty();
        }
        // 2. 查询共享给本人的日程
        List<Long> scheduleIds = convertList(participants, OaScheduleParticipantDO::getScheduleId);
        return scheduleMapper.selectReceivedPage(pageReqVO, scheduleIds);
    }

    @Override
    public int sendScheduleReminders() {
        // 1. 查询待提醒日程
        LocalDateTime remindTime = LocalDateTime.now();
        List<OaScheduleDO> schedules = scheduleMapper.selectListByRemindAndRemindedAndStartTime(true, false,
                new LocalDateTime[]{remindTime, remindTime.plusHours(properties.getSchedule().getRemindBeforeHours())});

        // 2. 逐条发送，通过代理调用保证每条日程独立事务，失败不影响其他日程
        int count = 0;
        for (OaScheduleDO schedule : schedules) {
            try {
                count += getSelf().sendScheduleReminder(schedule.getId(), remindTime) ? 1 : 0;
            } catch (Exception ex) {
                log.error("[sendScheduleReminders][日程 ({}) 提醒失败]", schedule.getId(), ex);
            }
        }
        return count;
    }

    /**
     * 向创建人和参与人发送单条日程提醒
     *
     * 保留 public 供自身代理调用，使单条日程的事务生效，不对外暴露 Service 接口。
     *
     * @param id 日程编号
     * @param remindTime 提醒基准时间
     * @return 是否完成本日程的提醒，已提醒或不符合条件时返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendScheduleReminder(Long id, LocalDateTime remindTime) {
        // 1. 校验日程当前仍需提醒，编辑或其他执行已处理时不重复发送
        OaScheduleDO schedule = validateScheduleExists(id);
        if (Boolean.FALSE.equals(schedule.getRemind()) || Boolean.TRUE.equals(schedule.getReminded())
                || !schedule.getStartTime().isAfter(remindTime)
                || !schedule.getStartTime().isBefore(
                        remindTime.plusHours(properties.getSchedule().getRemindBeforeHours()))) {
            return false;
        }

        // 2.1 查询参与人并合并创建人，同一用户只接收一次提醒
        List<OaScheduleParticipantDO> participants = scheduleParticipantMapper.selectListByScheduleId(id);
        List<Long> userIds = convertList(participants, OaScheduleParticipantDO::getUserId);
        userIds.add(NumberUtils.parseLong(schedule.getCreator()));
        Map<String, Object> templateParams = Maps.newHashMapWithExpectedSize(3);
        templateParams.put("scheduleTitle", schedule.getTitle());
        templateParams.put("startTime", LocalDateTimeUtil.format(schedule.getStartTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
        templateParams.put("route", "/oa/schedule/calendar");
        // 2.2 逐个通知
        for (Long userId : CollUtil.distinct(userIds)) {
            notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                    .setUserId(userId).setTemplateCode(SCHEDULE_REMINDER).setTemplateParams(templateParams));
        }
        scheduleMapper.updateById(new OaScheduleDO().setId(id).setReminded(true));
        return true;
    }

    /**
     * 获得自身代理，保证内部调用的事务生效
     *
     * @return 当前 Service 的 Spring 代理
     */
    private OaScheduleServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public List<OaScheduleParticipantDO> getScheduleParticipantList(Collection<Long> scheduleIds) {
        if (CollUtil.isEmpty(scheduleIds)) {
            return Collections.emptyList();
        }
        return scheduleParticipantMapper.selectListByScheduleIds(scheduleIds);
    }

    /**
     * 校验日程存在且属于当前用户
     *
     * @param id 日程编号
     * @param userId 用户编号
     * @return 日程
     */
    private OaScheduleDO validateScheduleOwner(Long id, Long userId) {
        OaScheduleDO schedule = validateScheduleExists(id);
        if (ObjectUtil.notEqual(NumberUtils.parseLong(schedule.getCreator()), userId)) {
            throw exception(SCHEDULE_ACCESS_DENIED);
        }
        return schedule;
    }

    /**
     * 校验日程存在
     *
     * @param id 日程编号
     * @return 日程
     */
    private OaScheduleDO validateScheduleExists(Long id) {
        OaScheduleDO schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw exception(SCHEDULE_NOT_EXISTS);
        }
        return schedule;
    }

    /**
     * 创建日程参与关系
     *
     * @param scheduleId 日程编号
     * @param participantUserIds 参与人编号集合
     */
    private void createScheduleParticipantList(Long scheduleId, Collection<Long> participantUserIds) {
        if (CollUtil.isEmpty(participantUserIds)) {
            return;
        }
        List<OaScheduleParticipantDO> participants = convertList(CollUtil.distinct(participantUserIds),
                participantUserId -> new OaScheduleParticipantDO().setScheduleId(scheduleId).setUserId(participantUserId)
                        .setReadStatus(false));
        scheduleParticipantMapper.insertBatch(participants);
    }

}
