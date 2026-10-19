package cn.iocoder.yudao.module.oa.service.discussion;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyCreateReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaDiscussionReplyMapper;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_REPLY_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_REPLY_NOT_EXISTS;

/**
 * OA 讨论回复 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaDiscussionReplyServiceImpl implements OaDiscussionReplyService {

    @Resource
    private OaDiscussionReplyMapper discussionReplyMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaDiscussionService discussionService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaDiscussionLikeService discussionLikeService;

    @Resource
    private PermissionApi permissionApi;

    @Override
    public Long createDiscussionReply(OaDiscussionReplyCreateReqVO createReqVO, Long userId) {
        // 1.1 校验讨论存在
        discussionService.validateDiscussionExists(createReqVO.getDiscussionId());
        // 1.2 校验父回复属于当前讨论，并获得被回复人
        Long parentId = ObjectUtil.defaultIfNull(createReqVO.getParentId(), OaDiscussionReplyDO.PARENT_ID_ROOT);
        Long replyUserId = null;
        if (ObjectUtil.notEqual(OaDiscussionReplyDO.PARENT_ID_ROOT, parentId)) {
            OaDiscussionReplyDO parentReply = validateDiscussionReplyExists(parentId);
            if (ObjectUtil.notEqual(parentReply.getDiscussionId(), createReqVO.getDiscussionId())) {
                throw exception(DISCUSSION_REPLY_NOT_EXISTS);
            }
            replyUserId = parentReply.getUserId();
        }

        // 2. 新增回复
        OaDiscussionReplyDO reply = BeanUtils.toBean(createReqVO, OaDiscussionReplyDO.class)
                .setUserId(userId).setParentId(parentId).setReplyUserId(replyUserId);
        discussionReplyMapper.insert(reply);
        return reply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscussionReply(Long id, Long userId) {
        // 1.1 校验回复存在
        OaDiscussionReplyDO reply = validateDiscussionReplyExists(id);
        // 1.2 校验所属讨论存在
        OaDiscussionDO discussion = discussionService.validateDiscussionExists(reply.getDiscussionId());
        // 1.3 校验当前用户是讨论发布人或超级管理员
        if (ObjectUtil.notEqual(discussion.getUserId(), userId) && !isSuperAdmin(userId)) {
            throw exception(DISCUSSION_REPLY_ACCESS_DENIED);
        }

        // 2. 查询当前回复及其全部子回复编号
        Set<Long> replyIds = getReplyDescendantIds(reply,
                discussionReplyMapper.selectListByDiscussionIds(Collections.singleton(reply.getDiscussionId())));

        // 3.1 删除回复点赞
        discussionLikeService.deleteDiscussionLikesByReplyIds(replyIds);
        // 3.2 删除当前回复及其全部子回复
        discussionReplyMapper.deleteByIds(replyIds);
    }

    @Override
    public List<OaDiscussionReplyDO> getDiscussionReplyList(Collection<Long> discussionIds) {
        return CollUtil.isEmpty(discussionIds) ? Collections.emptyList()
                : discussionReplyMapper.selectListByDiscussionIds(discussionIds);
    }

    @Override
    public PageResult<OaDiscussionReplyDO> getDiscussionReplyPage(OaDiscussionReplyPageReqVO pageReqVO) {
        // 1. 校验讨论存在
        discussionService.validateDiscussionExists(pageReqVO.getDiscussionId());

        // 2. 仅对主回复分页，子回复随所属楼层展示
        if (CollUtil.isEmpty(pageReqVO.getSortingFields())) {
            pageReqVO.setSortingFields(Collections.singletonList(PageUtils.buildSortingField(
                    OaDiscussionReplyDO::getCreateTime, SortingField.ORDER_ASC)));
        }
        return discussionReplyMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OaDiscussionReplyDO> getDiscussionReplyDescendantList(List<OaDiscussionReplyDO> replies) {
        if (CollUtil.isEmpty(replies)) {
            return Collections.emptyList();
        }
        // 1. 批量查询所属讨论的回复，不逐个楼层查询数据库
        List<OaDiscussionReplyDO> allReplies = getDiscussionReplyList(
                convertSet(replies, OaDiscussionReplyDO::getDiscussionId));
        // 2. 筛选当前页主回复的后代，保留原始时间顺序
        Set<Long> descendantIds = new HashSet<>();
        for (OaDiscussionReplyDO reply : replies) {
            descendantIds.addAll(getReplyDescendantIds(reply, allReplies));
            descendantIds.remove(reply.getId());
        }
        return convertList(allReplies, reply -> reply, reply -> descendantIds.contains(reply.getId()));
    }

    @Override
    public OaDiscussionReplyDO validateDiscussionReplyExists(Long id) {
        OaDiscussionReplyDO reply = discussionReplyMapper.selectById(id);
        if (reply == null) {
            throw exception(DISCUSSION_REPLY_NOT_EXISTS);
        }
        return reply;
    }

    /**
     * 获得当前回复及其全部子回复编号
     *
     * @param rootReply 当前回复
     * @param replies 所属讨论的回复列表
     * @return 回复编号集合
     */
    private static Set<Long> getReplyDescendantIds(OaDiscussionReplyDO rootReply,
                                                   List<OaDiscussionReplyDO> replies) {
        Map<Long, List<OaDiscussionReplyDO>> childReplyListMap = convertMultiMap(replies, OaDiscussionReplyDO::getParentId);
        Set<Long> replyIds = new HashSet<>();
        Deque<Long> pendingReplyIds = new ArrayDeque<>();
        pendingReplyIds.add(rootReply.getId());
        replyIds.add(rootReply.getId());
        for (int i = 0; i <= replies.size() && CollUtil.isNotEmpty(pendingReplyIds); i++) {
            Long replyId = pendingReplyIds.removeFirst();
            // 每个回复最多入队一次，避免脏数据形成循环关联
            for (OaDiscussionReplyDO childReply : childReplyListMap.getOrDefault(replyId, Collections.emptyList())) {
                if (replyIds.add(childReply.getId())) {
                    pendingReplyIds.add(childReply.getId());
                }
            }
        }
        return replyIds;
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscussionRepliesByDiscussionId(Long discussionId) {
        // 1. 查询讨论下的回复
        List<OaDiscussionReplyDO> replies = getDiscussionReplyList(Collections.singleton(discussionId));

        // 2.1 删除回复点赞
        discussionLikeService.deleteDiscussionLikesByReplyIds(convertSet(replies, OaDiscussionReplyDO::getId));
        // 2.2 删除讨论下的回复
        discussionReplyMapper.deleteByDiscussionId(discussionId);
    }

}
