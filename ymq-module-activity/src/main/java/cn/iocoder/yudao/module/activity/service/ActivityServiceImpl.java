package cn.iocoder.yudao.module.activity.service;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.activity.controller.app.vo.AppActivityCreateReqVO;
import cn.iocoder.yudao.module.activity.controller.app.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.activity.dal.dataobject.ActivityDO;
import cn.iocoder.yudao.module.activity.dal.dataobject.ActivityMemberDO;
import cn.iocoder.yudao.module.activity.dal.mysql.ActivityMapper;
import cn.iocoder.yudao.module.activity.dal.mysql.ActivityMemberMapper;
import cn.iocoder.yudao.module.activity.enums.ActivityMemberStatusEnum;
import cn.iocoder.yudao.module.activity.enums.ActivityStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ActivityServiceImpl implements ActivityService {

    private static final String SHORT_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去掉 0/O/1/I/l 避免误读
    private static final int SHORT_CODE_LEN = 6;
    private static final int SHORT_CODE_RETRY = 5;

    private final SecureRandom random = new SecureRandom();

    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityMemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createActivity(AppActivityCreateReqVO reqVO) {
        if (reqVO.getEndTime().isBefore(reqVO.getStartTime())) {
            throw new ServiceException(400, "结束时间必须晚于开始时间");
        }
        ActivityDO activity = ActivityDO.builder()
                .shortCode(generateUniqueShortCode())
                .title(reqVO.getTitle())
                .creatorId(reqVO.getCreatorId())
                .venueName(reqVO.getVenueName())
                .venueAddress(reqVO.getVenueAddress())
                .venueLat(reqVO.getVenueLat())
                .venueLng(reqVO.getVenueLng())
                .startTime(reqVO.getStartTime())
                .endTime(reqVO.getEndTime())
                .courtCount(reqVO.getCourtCount())
                .plannedCount(reqVO.getPlannedCount())
                .currentCount(1) // 召集人算 1 个
                .mode(reqVO.getMode())
                .rotation(reqVO.getRotation())
                .matchDuration(reqVO.getMatchDuration())
                .aaAmount(reqVO.getAaAmount())
                .remark(reqVO.getRemark())
                .isPublic(0)
                .visibleRadiusKm(3)
                .acceptStrangers(1)
                .needReview(1)
                .limitGender(0)
                .limitStrangersCount(1)
                .acceptedStrangersCount(0)
                .status(ActivityStatusEnum.RECRUITING)
                .build();
        activityMapper.insert(activity);

        memberMapper.insert(ActivityMemberDO.builder()
                .activityId(activity.getId())
                .userId(reqVO.getCreatorId())
                .gender(0)
                .joinSource("INVITE")
                .status(ActivityMemberStatusEnum.JOINED)
                .build());

        return activity.getId();
    }

    @Override
    public AppActivityRespVO getActivityDetail(Long id) {
        ActivityDO activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new ServiceException(404, "活动不存在");
        }
        AppActivityRespVO vo = BeanUtils.toBean(activity, AppActivityRespVO.class);
        List<ActivityMemberDO> members = memberMapper.selectListByActivity(id);
        vo.setMembers(members.stream().map(m -> {
            AppActivityRespVO.MemberItem item = new AppActivityRespVO.MemberItem();
            item.setUserId(m.getUserId());
            item.setPlaceholderName(m.getPlaceholderName());
            item.setGender(m.getGender());
            item.setLevelAtJoin(m.getLevelAtJoin());
            item.setStatus(m.getStatus());
            item.setJoinSource(m.getJoinSource());
            item.setJoinedAt(m.getJoinedAt());
            return item;
        }).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int signup(Long activityId, Long userId) {
        ActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException(404, "活动不存在");
        }
        if (!ActivityStatusEnum.RECRUITING.equals(activity.getStatus())) {
            throw new ServiceException(400, "活动已不在接龙阶段");
        }
        // 已报过名（含 QUIT 状态也要查，方便复活）
        ActivityMemberDO existing = memberMapper.selectByActivityAndUser(activityId, userId);
        if (existing != null) {
            if (ActivityMemberStatusEnum.JOINED.equals(existing.getStatus())) {
                throw new ServiceException(400, "你已经报名过了");
            }
            // 之前 QUIT 了，复活
            existing.setStatus(ActivityMemberStatusEnum.JOINED);
            existing.setJoinedAt(null); // 由 db 默认覆盖会失败，因这是 update，joinedAt 沿用旧值即可
            memberMapper.updateById(existing);
        } else {
            // 超员检查
            if (activity.getCurrentCount() >= activity.getPlannedCount()) {
                throw new ServiceException(400, "活动已满员，候补功能 Sprint 1 第 2 周支持");
            }
            memberMapper.insert(ActivityMemberDO.builder()
                    .activityId(activityId)
                    .userId(userId)
                    .gender(0)
                    .joinSource("INVITE")
                    .status(ActivityMemberStatusEnum.JOINED)
                    .build());
        }
        int newCount = activity.getCurrentCount() + 1;
        activity.setCurrentCount(newCount);
        activityMapper.updateById(activity);
        return newCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSignup(Long activityId, Long userId) {
        ActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException(404, "活动不存在");
        }
        ActivityMemberDO member = memberMapper.selectByActivityAndUser(activityId, userId);
        if (member == null || !ActivityMemberStatusEnum.JOINED.equals(member.getStatus())) {
            throw new ServiceException(400, "你没有报名");
        }
        if (member.getUserId() != null && member.getUserId().equals(activity.getCreatorId())) {
            throw new ServiceException(400, "召集人不能取消报名，请取消整个活动");
        }
        member.setStatus(ActivityMemberStatusEnum.QUIT);
        memberMapper.updateById(member);

        activity.setCurrentCount(Math.max(0, activity.getCurrentCount() - 1));
        activityMapper.updateById(activity);
    }

    @Override
    public PageResult<ActivityDO> pageMyCreated(Long userId, PageParam pageParam) {
        return activityMapper.selectPageByCreator(userId, pageParam);
    }

    @Override
    public PageResult<AppActivityRespVO> pageMyJoined(Long userId, PageParam pageParam) {
        PageResult<ActivityMemberDO> memberPage = memberMapper.selectPageByUser(userId, pageParam);
        List<AppActivityRespVO> activityVos = memberPage.getList().stream()
                .map(m -> activityMapper.selectById(m.getActivityId()))
                .filter(a -> a != null)
                .map(a -> BeanUtils.toBean(a, AppActivityRespVO.class))
                .collect(Collectors.toList());
        return new PageResult<>(activityVos, memberPage.getTotal());
    }

    /** 6 位短码，碰撞重试。容量 32^6 ≈ 1B，单库百万级几乎无碰撞 */
    private String generateUniqueShortCode() {
        for (int i = 0; i < SHORT_CODE_RETRY; i++) {
            String code = randomCode();
            if (activityMapper.selectByShortCode(code) == null) {
                return code;
            }
        }
        throw new ServiceException(500, "活动短码生成失败，请重试");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LEN);
        for (int i = 0; i < SHORT_CODE_LEN; i++) {
            sb.append(SHORT_CODE_ALPHABET.charAt(random.nextInt(SHORT_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

}
