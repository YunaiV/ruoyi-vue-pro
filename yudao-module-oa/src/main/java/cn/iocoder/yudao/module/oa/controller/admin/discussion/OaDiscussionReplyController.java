package cn.iocoder.yudao.module.oa.controller.admin.discussion;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyCreateReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionLikeDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;
import cn.iocoder.yudao.module.oa.service.discussion.OaDiscussionReplyService;
import cn.iocoder.yudao.module.oa.service.discussion.OaDiscussionLikeService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 讨论回复")
@RestController
@RequestMapping("/oa/discussion-reply")
@Validated
public class OaDiscussionReplyController {

    @Resource
    private OaDiscussionReplyService discussionReplyService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private OaDiscussionLikeService discussionLikeService;

    @PostMapping("/create")
    @Operation(summary = "创建讨论回复")
    public CommonResult<Long> createDiscussionReply(
            @Valid @RequestBody OaDiscussionReplyCreateReqVO createReqVO) {
        return success(discussionReplyService.createDiscussionReply(createReqVO, getLoginUserId()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除讨论回复")
    @Parameter(name = "id", description = "回复编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteDiscussionReply(@RequestParam("id") Long id) {
        discussionReplyService.deleteDiscussionReply(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得讨论回复分页")
    @PreAuthorize("@ss.hasPermission('oa:discussion:query')")
    public CommonResult<PageResult<OaDiscussionReplyRespVO>> getDiscussionReplyPage(
            @Valid OaDiscussionReplyPageReqVO pageReqVO) {
        PageResult<OaDiscussionReplyDO> pageResult = discussionReplyService.getDiscussionReplyPage(pageReqVO);
        // 1. 批量查询当前页楼层内的子回复
        List<OaDiscussionReplyDO> replies = new ArrayList<>(pageResult.getList());
        replies.addAll(discussionReplyService.getDiscussionReplyDescendantList(pageResult.getList()));

        // 2. 批量拼接展示字段，再按照父回复关系组织楼层
        List<OaDiscussionReplyRespVO> replyVOs = buildDiscussionReplyRespVOList(replies);
        Map<Long, OaDiscussionReplyRespVO> replyMap = convertMap(replyVOs, OaDiscussionReplyRespVO::getId);
        replyVOs.forEach(reply -> reply.setChildren(new ArrayList<>()));
        replyVOs.forEach(reply -> findAndThen(replyMap, reply.getParentId(),
                parent -> parent.getChildren().add(reply)));
        return success(new PageResult<>(convertList(pageResult.getList(),
                reply -> replyMap.get(reply.getId())), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接讨论回复响应列表
     *
     * @param replies 讨论回复列表
     * @return 讨论回复响应列表
     */
    private List<OaDiscussionReplyRespVO> buildDiscussionReplyRespVOList(List<OaDiscussionReplyDO> replies) {
        if (CollUtil.isEmpty(replies)) {
            return Collections.emptyList();
        }
        // 1.1 批量查询回复点赞
        Map<Long, List<OaDiscussionLikeDO>> replyLikeListMap = discussionLikeService.getReplyLikeListMap(
                convertSet(replies, OaDiscussionReplyDO::getId,
                        reply -> ObjectUtil.equal(reply.getParentId(), OaDiscussionReplyDO.PARENT_ID_ROOT)));
        // 1.2 批量查询回复人、被回复人和点赞人
        Set<Long> userIds = convertSetByFlatMap(replies,
                reply -> Stream.of(reply.getUserId(), reply.getReplyUserId()));
        userIds.addAll(convertSetByFlatMap(replyLikeListMap.values(),
                likes -> likes.stream().map(OaDiscussionLikeDO::getUserId)));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Long loginUserId = getLoginUserId();

        // 2. 转换并拼接回复展示字段
        return BeanUtils.toBean(replies, OaDiscussionReplyRespVO.class, reply -> {
            findAndThen(userMap, reply.getUserId(), user -> reply.setUserName(user.getNickname()));
            findAndThen(userMap, reply.getReplyUserId(), user -> reply.setReplyUserName(user.getNickname()));
            List<OaDiscussionLikeDO> likes = replyLikeListMap.getOrDefault(reply.getId(), Collections.emptyList());
            reply.setLikeCount(likes.size())
                    .setLiked(CollUtil.findOne(likes, like -> like.getUserId().equals(loginUserId)) != null)
                    .setLikeUserNames(convertList(likes, like -> {
                        AdminUserRespDTO user = userMap.get(like.getUserId());
                        return user != null ? user.getNickname() : null;
                    }));
        });
    }

}
