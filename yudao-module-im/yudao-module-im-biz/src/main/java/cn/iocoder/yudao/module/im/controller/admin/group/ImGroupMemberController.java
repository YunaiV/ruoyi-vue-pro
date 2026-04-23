package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.GROUP_MEMBER_NOT_IN_GROUP;

@Tag(name = "管理后台 - 群成员")
@RestController
@RequestMapping("/im/group-member")
@Validated
public class ImGroupMemberController {

    @Resource
    private ImGroupMemberService groupMemberService;

    @Resource
    private AdminUserApi adminUserApi;

    @PutMapping("/update")
    @Operation(summary = "更新群成员")
    @PreAuthorize("@ss.hasPermission('im:group-member:update')")
    public CommonResult<Boolean> updateGroupMember(@Valid @RequestBody ImGroupMemberUpdateReqVO updateReqVO) {
        groupMemberService.updateGroupMember(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群成员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:group-member:query')")
    public CommonResult<ImGroupMemberRespVO> getGroupMember(@RequestParam("id") Long id) {
        ImGroupMemberDO groupMember = groupMemberService.getGroupMember(id);
        return success(BeanUtils.toBean(groupMember, ImGroupMemberRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得指定群的成员列表")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1024")
    public CommonResult<List<ImGroupMemberRespVO>> getGroupMemberList(@RequestParam("groupId") Long groupId) {
        // 1.1 查询群成员列表（包含 DISABLE 已退群的成员，不按时间过滤）
        // 说明：保留已退群成员，是为了前端展示历史消息时，仍能通过该接口拿到已退群成员的昵称 / 头像信息，避免显示为空
        List<ImGroupMemberDO> members = groupMemberService.getGroupMemberListByGroupId(groupId);
        // 1.2 校验当前登录用户是否为群的有效成员，非成员不可查看
        Long loginUserId = getLoginUserId();
        if (CollUtil.findOne(members, m -> loginUserId.equals(m.getUserId())
                && CommonStatusEnum.ENABLE.getStatus().equals(m.getStatus())) == null) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }

        // 2.批量聚合 AdminUser 信息（昵称 / 头像）
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
