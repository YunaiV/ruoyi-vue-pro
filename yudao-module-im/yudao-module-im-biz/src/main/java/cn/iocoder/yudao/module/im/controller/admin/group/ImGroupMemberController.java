package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberRespVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 群成员")
@RestController
@RequestMapping("/im/group-member")
@Validated
public class ImGroupMemberController {

    @Resource
    private ImGroupMemberService imGroupMemberService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/invite")
    @Operation(summary = "邀请用户加入群")
    @PreAuthorize("@ss.hasPermission('im:group-member:create')")
    public CommonResult<Long> inviteGroupMember(@Valid @RequestBody ImGroupMemberInviteReqVO inviteReqVO) {
        return success(imGroupMemberService.inviteGroupMember(inviteReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新群成员")
    @PreAuthorize("@ss.hasPermission('im:group-member:update')")
    public CommonResult<Boolean> updateGroupMember(@Valid @RequestBody ImGroupMemberUpdateReqVO updateReqVO) {
        imGroupMemberService.updateGroupMember(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/remove")
    @Operation(summary = "移除群成员（踢人）")
    @Parameter(name = "id", description = "群成员编号", required = true)
    @PreAuthorize("@ss.hasPermission('im:group-member:delete')")
    public CommonResult<Boolean> removeGroupMember(@RequestParam("id") Long id) {
        imGroupMemberService.removeGroupMember(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群成员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:group-member:query')")
    public CommonResult<ImGroupMemberRespVO> getGroupMember(@RequestParam("id") Long id) {
        ImGroupMemberDO groupMember = imGroupMemberService.getGroupMember(id);
        return success(BeanUtils.toBean(groupMember, ImGroupMemberRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得指定群的成员列表")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1024")
    public CommonResult<List<ImGroupMemberRespVO>> getGroupMemberList(@RequestParam("groupId") Long groupId) {
        List<ImGroupMemberDO> members = imGroupMemberService.getGroupMemberListByGroupId(groupId);
        // 批量聚合 AdminUser 信息（昵称 / 头像）
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertList(members, ImGroupMemberDO::getUserId));
        return success(convertList(members, m -> {
            ImGroupMemberRespVO vo = BeanUtils.toBean(m, ImGroupMemberRespVO.class);
            MapUtils.findAndThen(userMap, m.getUserId(), user ->
                    vo.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
            return vo;
        }));
    }

}
