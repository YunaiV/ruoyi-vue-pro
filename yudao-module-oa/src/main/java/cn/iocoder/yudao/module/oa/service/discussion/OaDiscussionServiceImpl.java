package cn.iocoder.yudao.module.oa.service.discussion;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion.OaDiscussionPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion.OaDiscussionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaDiscussionMapper;
import cn.iocoder.yudao.module.oa.enums.discussion.OaDiscussionTypeEnum;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_VOTE_INVALID;

/**
 * OA 讨论 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaDiscussionServiceImpl implements OaDiscussionService {

    @Resource
    private OaDiscussionMapper discussionMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaDiscussionReplyService discussionReplyService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaDiscussionLikeService discussionLikeService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaDiscussionVoteService discussionVoteService;

    @Resource
    private PermissionApi permissionApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDiscussion(OaDiscussionSaveReqVO createReqVO, Long userId) {
        // 1. 新增讨论
        OaDiscussionDO discussion = BeanUtils.toBean(createReqVO, OaDiscussionDO.class)
                .setUserId(userId).setVisitCount(0);
        clearVoteConfigIfNotVote(discussion);
        discussionMapper.insert(discussion);

        // 2. 新增投票选项
        discussionVoteService.createVoteOptionList(discussion.getId(), createReqVO.getVoteOptions());
        return discussion.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDiscussion(OaDiscussionSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验讨论属于当前用户
        OaDiscussionDO discussion = validateDiscussionOwner(updateReqVO.getId(), userId);
        // 1.2 投票结束时间必须晚于原开始时间
        if (ObjectUtil.equal(OaDiscussionTypeEnum.VOTE.getType(), discussion.getType())
                && (updateReqVO.getVoteEndTime() == null
                || !updateReqVO.getVoteEndTime().isAfter(discussion.getVoteStartTime()))) {
            throw exception(DISCUSSION_VOTE_INVALID);
        }

        // 2. 更新讨论；注意：发布后不允许修改类型以及已有投票的基础配置和选项
        OaDiscussionDO updateObj = BeanUtils.toBean(updateReqVO, OaDiscussionDO.class)
                .setUserId(null).setType(null);
        if (OaDiscussionTypeEnum.VOTE.getType().equals(discussion.getType())) {
            updateObj.setVoteMultiple(null).setVoteStartTime(null);
        } else {
            clearVoteConfigIfNotVote(updateObj);
        }
        discussionMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscussion(Long id, Long userId) {
        // 1. 校验讨论属于当前用户，或当前用户是超级管理员
        validateDiscussionDeletable(id, userId);

        // 2.1 删除讨论回复及回复点赞
        discussionReplyService.deleteDiscussionRepliesByDiscussionId(id);
        // 2.2 删除讨论点赞
        discussionLikeService.deleteDiscussionLikesByDiscussionId(id);
        // 2.3 删除投票记录及选项
        discussionVoteService.deleteDiscussionVotesByDiscussionId(id);
        // 2.4 删除讨论
        discussionMapper.deleteById(id);
    }

    @Override
    public OaDiscussionDO getDiscussion(Long id) {
        return discussionMapper.selectById(id);
    }

    @Override
    public PageResult<OaDiscussionDO> getDiscussionPage(OaDiscussionPageReqVO pageReqVO) {
        if (CollUtil.isEmpty(pageReqVO.getSortingFields())) {
            pageReqVO.setSortingFields(Arrays.asList(
                    PageUtils.buildSortingField(OaDiscussionDO::getType, SortingField.ORDER_ASC),
                    PageUtils.buildSortingField(OaDiscussionDO::getUpdateTime, SortingField.ORDER_DESC)));
        }
        return discussionMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<OaDiscussionDO> getDiscussionManagePage(OaDiscussionPageReqVO pageReqVO, Long userId) {
        // 1. 普通用户只能管理本人讨论，不接受请求指定的其他发布人
        if (!isSuperAdmin(userId)) {
            pageReqVO.setUserId(userId);
        }

        // 2. 复用讨论分页查询，管理员保留发布人筛选
        return getDiscussionPage(pageReqVO);
    }

    @Override
    public void increaseDiscussionVisitCount(Long id) {
        // 1. 校验讨论存在
        validateDiscussionExists(id);

        // 2. 增加访问次数
        discussionMapper.updateVisitCount(id);
    }

    /**
     * 校验讨论存在且由当前用户发布
     *
     * @param id 讨论编号
     * @param userId 当前用户编号
     * @return 讨论
     */
    private OaDiscussionDO validateDiscussionOwner(Long id, Long userId) {
        OaDiscussionDO discussion = validateDiscussionExists(id);
        if (ObjectUtil.notEqual(discussion.getUserId(), userId)) {
            throw exception(DISCUSSION_ACCESS_DENIED);
        }
        return discussion;
    }

    /**
     * 校验当前用户是讨论发布人或超级管理员
     *
     * @param id 讨论编号
     * @param userId 当前用户编号
     * @return 讨论
     */
    private OaDiscussionDO validateDiscussionDeletable(Long id, Long userId) {
        OaDiscussionDO discussion = validateDiscussionExists(id);
        if (ObjectUtil.notEqual(discussion.getUserId(), userId) && !isSuperAdmin(userId)) {
            throw exception(DISCUSSION_ACCESS_DENIED);
        }
        return discussion;
    }

    /**
     * 判断用户是否具有超级管理员角色
     *
     * @param userId 用户编号
     * @return 是否为超级管理员
     */
    private boolean isSuperAdmin(Long userId) {
        return permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());
    }

    @Override
    public OaDiscussionDO validateDiscussionExists(Long id) {
        OaDiscussionDO discussion = getDiscussion(id);
        if (discussion == null) {
            throw exception(DISCUSSION_NOT_EXISTS);
        }
        return discussion;
    }

    /**
     * 清理非投票讨论的投票配置，新增时不保存，修改时不覆盖原字段
     *
     * @param discussion 讨论数据
     */
    private void clearVoteConfigIfNotVote(OaDiscussionDO discussion) {
        if (OaDiscussionTypeEnum.VOTE.getType().equals(discussion.getType())) {
            return;
        }
        discussion.setVoteMultiple(null).setVoteStartTime(null).setVoteEndTime(null);
    }

}
