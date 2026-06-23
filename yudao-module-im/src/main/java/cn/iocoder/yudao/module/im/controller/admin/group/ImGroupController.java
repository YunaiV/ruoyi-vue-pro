package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.*;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberRemoveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 群")
@RestController
@RequestMapping("/im/group")
@Validated
public class ImGroupController {

    @Resource
    private ImGroupService groupService;
    @Resource
    private ImGroupMemberService groupMemberService;
    @Resource
    private ImGroupMessageService groupMessageService;

    // ==================== 群的写操作 ====================

    @PostMapping("/create")
    @Operation(summary = "创建群")
    public CommonResult<ImGroupRespVO> createGroup(@Valid @RequestBody ImGroupCreateReqVO createReqVO) {
        ImGroupDO group = groupService.createGroup(createReqVO, getLoginUserId());
        // 新建群必无 pinnedMessages，跳过关联回填
        return success(BeanUtils.toBean(group, ImGroupRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "更新群")
    public CommonResult<ImGroupRespVO> updateGroup(@Valid @RequestBody ImGroupUpdateReqVO updateReqVO) {
        ImGroupDO group = groupService.updateGroup(updateReqVO, getLoginUserId());
        return success(buildGroupRespVO(group, getLoginUserId()));
    }

    @DeleteMapping("/dissolve")
    @Operation(summary = "解散群")
    @Parameter(name = "id", description = "群编号", required = true)
    public CommonResult<Boolean> dissolveGroup(@RequestParam("id") Long id) {
        groupService.dissolveGroup(id, getLoginUserId());
        return success(true);
    }

    // ==================== 群的读操作 ====================

    @GetMapping("/get")
    @Operation(summary = "获得群")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<ImGroupRespVO> getGroup(@RequestParam("id") Long id) {
        ImGroupDO group = groupService.getGroup(id);
        return success(buildGroupRespVO(group, getLoginUserId()));
    }

    @GetMapping("/list")
    @Operation(summary = "获得当前登录用户的群列表")
    public CommonResult<List<ImGroupRespVO>> getMyGroupList() {
        Long loginUserId = getLoginUserId();
        List<ImGroupDO> groups = groupService.getMyGroupList(loginUserId);
        return success(buildGroupRespVOList(groups, loginUserId));
    }

    // ==================== 群成员的写操作 ====================

    @PostMapping("/invite")
    @Operation(summary = "邀请用户加入群")
    public CommonResult<Boolean> inviteGroupMember(@Valid @RequestBody ImGroupMemberInviteReqVO inviteReqVO) {
        groupService.inviteGroupMember(getLoginUserId(), inviteReqVO);
        return success(true);
    }

    @DeleteMapping("/quit")
    @Operation(summary = "退出群")
    @Parameter(name = "groupId", description = "群编号", required = true)
    public CommonResult<Boolean> quitGroup(@RequestParam("groupId") Long groupId) {
        groupService.quitGroup(groupId, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/kicking")
    @Operation(summary = "移除群成员")
    public CommonResult<Boolean> removeGroupMember(@Valid @RequestBody ImGroupMemberRemoveReqVO removeReqVO) {
        groupService.removeGroupMember(getLoginUserId(), removeReqVO);
        return success(true);
    }

    @PutMapping("/add-admin")
    @Operation(summary = "添加群管理员")
    public CommonResult<Boolean> addGroupAdmin(@Valid @RequestBody ImGroupAdminAddReqVO reqVO) {
        groupService.addGroupAdmin(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/remove-admin")
    @Operation(summary = "撤销群管理员")
    public CommonResult<Boolean> removeGroupAdmin(@Valid @RequestBody ImGroupAdminRemoveReqVO reqVO) {
        groupService.removeGroupAdmin(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/transfer-owner")
    @Operation(summary = "转让群主")
    public CommonResult<Boolean> transferGroupOwner(@Valid @RequestBody ImGroupTransferOwnerReqVO transferReqVO) {
        groupService.transferGroupOwner(getLoginUserId(), transferReqVO);
        return success(true);
    }

    // ==================== 群消息置顶 ====================

    @PutMapping("/pin-message")
    @Operation(summary = "置顶群消息（群主 / 管理员）")
    public CommonResult<Boolean> pinGroupMessage(@Valid @RequestBody ImGroupMessagePinReqVO reqVO) {
        groupService.pinGroupMessage(getLoginUserId(), reqVO.getId(), reqVO.getMessageId());
        return success(true);
    }

    @PutMapping("/unpin-message")
    @Operation(summary = "取消置顶群消息（群主 / 管理员）")
    public CommonResult<Boolean> unpinGroupMessage(@Valid @RequestBody ImGroupMessagePinReqVO reqVO) {
        groupService.unpinGroupMessage(getLoginUserId(), reqVO.getId(), reqVO.getMessageId());
        return success(true);
    }

    // ==================== 群禁言 ====================

    @PutMapping("/mute-all")
    @Operation(summary = "全群禁言 / 取消（群主 / 管理员）")
    public CommonResult<Boolean> muteAll(@Valid @RequestBody ImGroupMuteAllReqVO reqVO) {
        groupService.muteAll(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/mute-member")
    @Operation(summary = "禁言成员")
    public CommonResult<Boolean> muteMember(@Valid @RequestBody ImGroupMuteMemberReqVO reqVO) {
        groupService.muteMember(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/cancel-mute-member")
    @Operation(summary = "取消成员禁言")
    public CommonResult<Boolean> cancelMuteMember(@Valid @RequestBody ImGroupCancelMuteMemberReqVO reqVO) {
        groupService.cancelMuteMember(getLoginUserId(), reqVO);
        return success(true);
    }

    /** 单群转 VO + 关联回填 pinnedMessages（仅当登录用户是该群有效成员） */
    private ImGroupRespVO buildGroupRespVO(ImGroupDO group, Long loginUserId) {
        if (group == null) {
            return null;
        }
        return buildGroupRespVOList(Collections.singletonList(group), loginUserId).get(0);
    }

    /**
     * 群列表批量转 VO + 关联回填 pinnedMessages
     * <p>
     * 仅当登录用户是某群的有效成员时才回填该群的 pinnedMessages，避免非成员 / 已退群用户越权拿到置顶消息内容
     */
    private List<ImGroupRespVO> buildGroupRespVOList(List<ImGroupDO> groups, Long loginUserId) {
        if (CollUtil.isEmpty(groups)) {
            return Collections.emptyList();
        }
        // 仅当前用户是有效成员的群才允许回填置顶消息
        Map<Long, ImGroupMemberDO> memberMap = convertMap(
                groupMemberService.getGroupMemberListByUserId(loginUserId), ImGroupMemberDO::getGroupId);
        Set<Long> activeGroupIds = convertSet(memberMap.values(), ImGroupMemberDO::getGroupId,
                member -> CommonStatusEnum.ENABLE.getStatus().equals(member.getStatus()));
        Set<Long> allMessageIds = convertSetByFlatMap(groups, group -> activeGroupIds.contains(group.getId())
                ? CollUtil.emptyIfNull(group.getPinnedMessageIds()).stream() : Stream.empty());
        Map<Long, ImGroupMessageDO> messageMap = groupMessageService.getGroupMessageMap(allMessageIds);
        // 转换输出
        return convertList(groups, group -> {
            ImGroupRespVO groupVO = BeanUtils.toBean(group, ImGroupRespVO.class);
            // 标记登录用户在该群的成员状态，供前端区分当前群与历史退群
            boolean joined = activeGroupIds.contains(group.getId());
            groupVO.setJoinStatus(joined ? CommonStatusEnum.ENABLE.getStatus() : CommonStatusEnum.DISABLE.getStatus());
            MapUtils.findAndThen(memberMap, group.getId(), member ->
                    groupVO.setGroupRemark(member.getGroupRemark()).setSilent(Boolean.TRUE.equals(member.getSilent())));
            if (!joined || CollUtil.isEmpty(group.getPinnedMessageIds())) {
                return groupVO;
            }
            // 按 pin 顺序输出，已被删除的消息（messageMap 没命中）跳过
            List<ImGroupMessageDO> pinnedMesages = convertList(group.getPinnedMessageIds(), messageMap::get);
            return groupVO.setPinnedMessages(BeanUtils.toBean(pinnedMesages, ImGroupMessageRespVO.class));
        });
    }

}
