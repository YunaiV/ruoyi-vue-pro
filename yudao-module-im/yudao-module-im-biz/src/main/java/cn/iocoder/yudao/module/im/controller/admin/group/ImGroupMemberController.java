package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberRespVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberSaveReqVO;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 群成员")
@RestController
@RequestMapping("/im/group-member")
@Validated
public class ImGroupMemberController {

    @Resource
    private ImGroupMemberService imGroupMemberService;

    @Resource
    private AdminUserApi adminUserApi;

    // TODO @AI：add 加人；
    @PostMapping("/create")
    @Operation(summary = "创建群成员")
    @PreAuthorize("@ss.hasPermission('im:group-member:create')")
    public CommonResult<Long> createGroupMember(@Valid @RequestBody ImGroupMemberSaveReqVO createReqVO) {
        return success(imGroupMemberService.createGroupMember(createReqVO));
    }

    // TODO @AI：踢人；

    // TODO @AI：退群；放在这里；避免 group 里面逻辑太多；

    @PutMapping("/update")
    @Operation(summary = "更新群成员")
    @PreAuthorize("@ss.hasPermission('im:group-member:update')")
    public CommonResult<Boolean> updateGroupMember(@Valid @RequestBody ImGroupMemberSaveReqVO updateReqVO) {
        imGroupMemberService.updateGroupMember(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除群成员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('im:group-member:delete')")
    public CommonResult<Boolean> deleteGroupMember(@RequestParam("id") Long id) {
        imGroupMemberService.deleteGroupMember(id);
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

    @GetMapping("/page")
    @Operation(summary = "获得群成员分页")
    @PreAuthorize("@ss.hasPermission('im:group-member:query')")
    public CommonResult<PageResult<ImGroupMemberRespVO>> getGroupMemberPage(@Valid ImGroupMemberPageReqVO pageReqVO) {
        PageResult<ImGroupMemberDO> pageResult = imGroupMemberService.getGroupMemberPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImGroupMemberRespVO.class));
    }

    // ========== 用户侧接口（登录用户自己看群成员，无需后台权限） ==========

    @GetMapping("/list")
    @Operation(summary = "获得指定群的成员列表")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1024")
    public CommonResult<List<ImGroupMemberRespVO>> getGroupMemberList(@RequestParam("groupId") Long groupId) {
        List<ImGroupMemberDO> members = imGroupMemberService.getGroupMemberListByGroupId(groupId);
        if (members.isEmpty()) {
            return success(Collections.emptyList());
        }
        // 聚合 AdminUser 的昵称 + 头像
        List<Long> userIds = CollectionUtils.convertList(members, ImGroupMemberDO::getUserId);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        return success(CollectionUtils.convertList(members, m -> {
            ImGroupMemberRespVO vo = BeanUtils.toBean(m, ImGroupMemberRespVO.class);
            AdminUserRespDTO user = userMap.get(m.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            // showNickname 回落：优先群内备注 displayUserName，否则用用户昵称
            if (m.getDisplayUserName() != null && !m.getDisplayUserName().isEmpty()) {
                vo.setShowNickname(m.getDisplayUserName());
            } else if (user != null) {
                vo.setShowNickname(user.getNickname());
            }
            return vo;
        }));
    }

    // TODO @芋艿：【对齐】/group/members/remove 批量踢人接口第一期未实现。
    //  需要：校验群主权限、批量更新 im_group_member 为 DISABLE + 设置 quitTime、通过 WebSocket 推送给被踢成员。

}
