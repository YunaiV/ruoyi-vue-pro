package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupRespVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 群")
@RestController
@RequestMapping("/im/group")
@Validated
public class ImGroupController {

    @Resource
    private ImGroupService imGroupService;

    @PostMapping("/create")
    @Operation(summary = "创建群")
    @PreAuthorize("@ss.hasPermission('im:group:create')")
    public CommonResult<Long> createGroup(@Valid @RequestBody ImGroupSaveReqVO createReqVO) {
        return success(imGroupService.createGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新群")
    @PreAuthorize("@ss.hasPermission('im:group:update')")
    public CommonResult<Boolean> updateGroup(@Valid @RequestBody ImGroupSaveReqVO updateReqVO) {
        imGroupService.updateGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除群")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('im:group:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        imGroupService.deleteGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:group:query')")
    public CommonResult<ImGroupRespVO> getGroup(@RequestParam("id") Long id) {
        ImGroupDO group = imGroupService.getGroup(id);
        return success(BeanUtils.toBean(group, ImGroupRespVO.class));
    }

    // TODO @AI：去掉这个接口；
    @GetMapping("/page")
    @Operation(summary = "获得群分页")
    @PreAuthorize("@ss.hasPermission('im:group:query')")
    public CommonResult<PageResult<ImGroupRespVO>> getGroupPage(@Valid ImGroupPageReqVO pageReqVO) {
        PageResult<ImGroupDO> pageResult = imGroupService.getGroupPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImGroupRespVO.class));
    }

    // ========== 用户侧接口（登录用户自己的群，无需后台权限） ==========

    // TODO @AI：直接 list；
    @GetMapping("/list-my")
    @Operation(summary = "获得当前登录用户的群列表")
    public CommonResult<List<ImGroupRespVO>> getMyGroupList() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<ImGroupDO> groups = imGroupService.getMyGroupList(userId);
        return success(BeanUtils.toBean(groups, ImGroupRespVO.class));
    }

    // TODO @芋艿：【对齐】/group/create、/group/modify、/group/invite、/group/quit、/group/delete/{id}
    //  这些用户侧接口第一期暂未实现。需要：
    //  1. createGroup(vo)：需同时插入 im_group + 批量插入 im_group_member；还要给群成员推送 FRIEND_INVITE 类消息
    //  2. modifyGroup(vo)：允许修改 name / notice / avatar / remarkGroupName / remarkNickName
    //     （其中 remarkGroupName / remarkNickName 是 im_group_member 的字段，不是 im_group 的）
    //  3. invite(groupId, userIds)：批量插入 im_group_member 为 ENABLE；给被邀请人推送入群通知
    //  4. quitGroup(groupId)：校验非群主才能退，然后把自己的 im_group_member 标为 DISABLE + quitTime
    //  5. removeGroupMembers(groupId, userIds)：群主权限校验 + 把成员标为 DISABLE
    //  以上都需要 WebSocket 推送对应事件（FRIEND_INVITE / MEMBER_QUIT / MEMBER_REMOVE 等），
    //  涉及 `ImWsEventType` 扩展 + `ImWebSocketPusher` 新增事件推送方法，待统一设计后补上。

}
