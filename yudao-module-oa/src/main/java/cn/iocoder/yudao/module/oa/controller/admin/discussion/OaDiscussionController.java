package cn.iocoder.yudao.module.oa.controller.admin.discussion;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion.OaDiscussionPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion.OaDiscussionRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion.OaDiscussionSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.vote.OaVoteOptionRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionLikeDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteOptionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteRecordDO;
import cn.iocoder.yudao.module.oa.service.discussion.OaDiscussionService;
import cn.iocoder.yudao.module.oa.service.discussion.OaDiscussionReplyService;
import cn.iocoder.yudao.module.oa.service.discussion.OaDiscussionLikeService;
import cn.iocoder.yudao.module.oa.service.discussion.OaDiscussionVoteService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - OA 讨论")
@RestController
@RequestMapping("/oa/discussion")
@Validated
public class OaDiscussionController {

    @Resource
    private OaDiscussionService discussionService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private OaDiscussionLikeService discussionLikeService;
    @Resource
    private OaDiscussionReplyService discussionReplyService;
    @Resource
    private OaDiscussionVoteService discussionVoteService;

    @PostMapping("/create")
    @Operation(summary = "创建讨论")
    @PreAuthorize("@ss.hasPermission('oa:discussion:create')")
    public CommonResult<Long> createDiscussion(@Valid @RequestBody OaDiscussionSaveReqVO createReqVO) {
        return success(discussionService.createDiscussion(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新讨论")
    @PreAuthorize("@ss.hasPermission('oa:discussion:update')")
    public CommonResult<Boolean> updateDiscussion(@Valid @RequestBody OaDiscussionSaveReqVO updateReqVO) {
        discussionService.updateDiscussion(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除讨论")
    @Parameter(name = "id", description = "讨论编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:discussion:delete')")
    public CommonResult<Boolean> deleteDiscussion(@RequestParam("id") Long id) {
        discussionService.deleteDiscussion(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得讨论详情")
    @Parameter(name = "visit", description = "是否记录本次访问，默认不记录", example = "true")
    @Parameter(name = "id", description = "讨论编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:discussion:query')")
    public CommonResult<OaDiscussionRespVO> getDiscussion(@RequestParam("id") Long id,
                                                          @RequestParam(value = "visit", defaultValue = "false") Boolean visit) {
        // 1.1 查询讨论
        OaDiscussionDO discussion = discussionService.validateDiscussionExists(id);
        // 1.2 按需记录本次访问
        if (Boolean.TRUE.equals(visit)) {
            discussionService.increaseDiscussionVisitCount(id);
            discussion.setVisitCount(discussion.getVisitCount() + 1);
        }

        // 2. 拼接讨论详情
        return success(buildDiscussionRespVO(discussion));
    }

    @GetMapping("/page")
    @Operation(summary = "获得讨论分页")
    @PreAuthorize("@ss.hasPermission('oa:discussion:query')")
    public CommonResult<PageResult<OaDiscussionRespVO>> getDiscussionPage(
            @Valid OaDiscussionPageReqVO pageReqVO) {
        PageResult<OaDiscussionDO> pageResult = discussionService.getDiscussionPage(pageReqVO);
        return success(new PageResult<>(buildDiscussionRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/manage-page")
    @Operation(summary = "获得管理范围内的讨论分页")
    @PreAuthorize("@ss.hasPermission('oa:discussion:query')")
    public CommonResult<PageResult<OaDiscussionRespVO>> getDiscussionManagePage(
            @Valid OaDiscussionPageReqVO pageReqVO) {
        PageResult<OaDiscussionDO> pageResult = discussionService.getDiscussionManagePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildDiscussionRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接讨论详情
     *
     * @param discussion 讨论
     * @return 讨论响应
     */
    private OaDiscussionRespVO buildDiscussionRespVO(OaDiscussionDO discussion) {
        if (discussion == null) {
            return null;
        }
        return CollUtil.getFirst(buildDiscussionRespVOList(Collections.singletonList(discussion)));
    }

    /**
     * 拼接讨论响应列表
     *
     * @param discussions 讨论列表
     * @return 讨论响应列表
     */
    private List<OaDiscussionRespVO> buildDiscussionRespVOList(List<OaDiscussionDO> discussions) {
        if (CollUtil.isEmpty(discussions)) {
            return Collections.emptyList();
        }
        // 1.1 批量查询回复和点赞
        Set<Long> discussionIds = convertSet(discussions, OaDiscussionDO::getId);
        Map<Long, List<OaDiscussionReplyDO>> replyListMap = convertMultiMap(
                discussionReplyService.getDiscussionReplyList(discussionIds), OaDiscussionReplyDO::getDiscussionId);
        List<OaDiscussionLikeDO> discussionLikes = discussionLikeService.getDiscussionLikeList(discussionIds);
        Map<Long, List<OaDiscussionLikeDO>> likeListMap = convertMultiMap(discussionLikes,
                OaDiscussionLikeDO::getDiscussionId);
        // 1.2 批量查询投票选项和记录
        Map<Long, List<OaVoteOptionDO>> voteOptionListMap = convertMultiMap(
                discussionVoteService.getVoteOptionList(discussionIds), OaVoteOptionDO::getDiscussionId);
        List<OaVoteRecordDO> voteRecords = discussionVoteService.getVoteRecordList(discussionIds);
        Map<Long, List<OaVoteRecordDO>> voteRecordListMap = convertMultiMap(voteRecords,
                OaVoteRecordDO::getDiscussionId);
        // 1.3 批量查询发布人、点赞人和投票人
        Set<Long> userIds = convertSet(discussions, OaDiscussionDO::getUserId);
        userIds.addAll(convertSet(discussionLikes, OaDiscussionLikeDO::getUserId));
        userIds.addAll(convertSet(voteRecords, OaVoteRecordDO::getUserId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Long loginUserId = getLoginUserId();
        // 2. 转换并拼接讨论展示字段
        return BeanUtils.toBean(discussions, OaDiscussionRespVO.class, discussion -> {
            findAndThen(userMap, discussion.getUserId(), user -> discussion.setUserName(user.getNickname()));
            discussion.setReplyCount(CollUtil.size(replyListMap.get(discussion.getId())));
            List<OaDiscussionLikeDO> likes = likeListMap.getOrDefault(discussion.getId(), Collections.emptyList());
            discussion.setLikeCount(likes.size());
            discussion.setLiked(CollUtil.findOne(likes, like -> like.getUserId().equals(loginUserId)) != null);
            discussion.setLikeUserNames(convertList(likes,
                    like -> getUserNickname(userMap, like.getUserId())));
            discussion.setVoteOptions(buildVoteOptionRespVOList(
                    voteOptionListMap.getOrDefault(discussion.getId(), Collections.emptyList()),
                    voteRecordListMap.getOrDefault(discussion.getId(), Collections.emptyList()), userMap, loginUserId));
        });
    }

    /**
     * 拼接投票选项响应列表
     *
     * @param voteOptions 投票选项列表
     * @param voteRecords 投票记录列表
     * @param userMap 用户 Map
     * @param loginUserId 当前登录用户编号
     * @return 投票选项响应列表
     */
    private List<OaVoteOptionRespVO> buildVoteOptionRespVOList(List<OaVoteOptionDO> voteOptions,
                                                               List<OaVoteRecordDO> voteRecords,
                                                               Map<Long, AdminUserRespDTO> userMap,
                                                               Long loginUserId) {
        Map<Long, List<OaVoteRecordDO>> optionVoteRecordListMap = convertMultiMap(voteRecords,
                OaVoteRecordDO::getOptionId);
        return BeanUtils.toBean(voteOptions, OaVoteOptionRespVO.class, voteOption -> {
            voteOption.setVoteCount(0).setVoted(false).setVoterUserNames(Collections.emptyList());
            findAndThen(optionVoteRecordListMap, voteOption.getId(), records ->
                    voteOption.setVoteCount(records.size())
                            .setVoted(CollUtil.findOne(records, record -> record.getUserId().equals(loginUserId)) != null)
                            .setVoterUserNames(convertList(records, record -> getUserNickname(userMap, record.getUserId()))));
        });
    }

    /**
     * 获得用户昵称
     *
     * @param userMap 用户 Map
     * @param userId 用户编号
     * @return 用户昵称
     */
    private static String getUserNickname(Map<Long, AdminUserRespDTO> userMap, Long userId) {
        AdminUserRespDTO user = userMap.get(userId);
        return user != null ? user.getNickname() : null;
    }

}
