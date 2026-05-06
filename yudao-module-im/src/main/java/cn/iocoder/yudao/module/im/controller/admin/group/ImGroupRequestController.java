package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupRequestService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

/**
 * IM 加群申请 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IM 加群申请")
@RestController
@RequestMapping("/im/group-request")
@Validated
public class ImGroupRequestController {

    @Resource
    private ImGroupRequestService groupRequestService;
    @Resource
    private ImGroupService groupService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/apply")
    @Operation(summary = "申请加群")
    public CommonResult<Long> applyJoinGroup(@Valid @RequestBody ImGroupRequestApplyReqVO reqVO) {
        ImGroupRequestDO request = groupRequestService.applyJoinGroup(getLoginUserId(), reqVO);
        return success(request != null ? request.getId() : null);
    }

    @PutMapping("/agree")
    @Operation(summary = "同意加群申请（群主或管理员）")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    public CommonResult<Boolean> agreeGroupRequest(
            @RequestParam("id") @NotNull(message = "申请编号不能为空") Long id) {
        groupRequestService.agreeGroupRequest(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/refuse")
    @Operation(summary = "拒绝加群申请（群主或管理员）")
    public CommonResult<Boolean> refuseGroupRequest(
            @RequestParam("id") @NotNull(message = "申请编号不能为空") Long id,
            @RequestParam(value = "handleContent", required = false)
            @Size(max = 255, message = "处理理由最多 255 个字符") String handleContent) {
        groupRequestService.refuseGroupRequest(getLoginUserId(), id, handleContent);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "查询「我相关」的加群申请列表（含我主动申请、我被邀请待审）；游标分页")
    public CommonResult<List<ImGroupRequestRespVO>> getMyGroupRequestList(
            @Parameter(description = "当前列表最旧记录的 id；首页不传")
            @RequestParam(value = "lastRequestId", required = false) Long lastRequestId,
            @Parameter(description = "单次拉取条数", required = true)
            @RequestParam("limit") @Min(1) @Max(200) Integer limit) {
        List<ImGroupRequestDO> list = groupRequestService.getMyGroupRequestList(getLoginUserId(), lastRequestId, limit);
        return success(buildList(list));
    }

    // TODO @AI：pending-list；
    @GetMapping("/list-pending")
    @Operation(summary = "查询指定群的未处理加群申请列表（仅群主或管理员可调）；游标分页")
    public CommonResult<List<ImGroupRequestRespVO>> getPendingGroupRequestList(
            @RequestParam("groupId") @NotNull(message = "群编号不能为空") Long groupId,
            @Parameter(description = "当前列表最旧记录的 id；首页不传")
            @RequestParam(value = "lastRequestId", required = false) Long lastRequestId,
            @Parameter(description = "单次拉取条数", required = true)
            @RequestParam("limit") @Min(1) @Max(200) Integer limit) {
        List<ImGroupRequestDO> list = groupRequestService.getPendingGroupRequestList(
                getLoginUserId(), groupId, lastRequestId, limit);
        return success(buildList(list));
    }

    // TODO @AI：群管理里，也有个地方，可以查询这个群所有的 list；这样上面的 list、list-pending 有点重复。看看怎么组合。可以和我讨论下；

    // TODO @AI：方法上，不用 my；因为这个方法，都是查询我的；
    @GetMapping("/get")
    @Operation(summary = "按 id 单查「我相关」的申请记录（带越权过滤；WebSocket 通知到达后用）")
    @Parameter(name = "id", description = "申请记录编号", required = true)
    public CommonResult<ImGroupRequestRespVO> getMyGroupRequest(@RequestParam("id") Long id) {
        ImGroupRequestDO request = groupRequestService.getGroupRequest(id);
        // 越权过滤：申请人 / 被邀请人 / 邀请人 / 群主 / 管理员之外，当不存在返回 null
        if (request == null) {
            return success(null);
        }
        Long currentUserId = getLoginUserId();
        boolean canSee = ObjUtil.equal(request.getUserId(), currentUserId)
                || ObjUtil.equal(request.getInviterUserId(), currentUserId);
        if (!canSee) {
            // 群主 / 管理员可见；通过 list-pending 校验路径
            // TODO @AI：不要 try catch；
            try {
                // TODO @AI：这个方法的实现，不要里面抛出异常；应该调用查询是否为管理员；而不应该这么搞哈。这样，是不是可以上面 || 后面在加一个条件？
                groupRequestService.getPendingGroupRequestList(currentUserId, request.getGroupId(), null, 1);
                canSee = true;
            } catch (Exception ignored) {
                // 非 owner / admin
            }
        }
        if (!canSee) {
            return success(null);
        }
        return success(CollUtil.getFirst(buildList(Collections.singletonList(request))));
    }

    /** 申请记录列表批量转 VO + 关联回填用户 / 群信息 */
    private List<ImGroupRequestRespVO> buildList(List<ImGroupRequestDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 聚合 user / inviter 用户信息
        // TODO @AI：.filter(Objects::nonNull) 内部已经过滤了；
        Set<Long> userIds = convertSetByFlatMap(list,
                request -> Stream.of(request.getUserId(), request.getInviterUserId())
                        .filter(Objects::nonNull));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 2. 聚合群信息（封禁 / 解散群也要回填，便于前端展示历史）
        Set<Long> groupIds = convertSet(list, ImGroupRequestDO::getGroupId);
        Map<Long, ImGroupDO> groupMap = groupService.getGroupMap(groupIds);
        return convertList(list, request -> {
            ImGroupRequestRespVO vo = BeanUtils.toBean(request, ImGroupRequestRespVO.class);
            MapUtils.findAndThen(userMap, request.getUserId(), user ->
                    vo.setUserNickname(user.getNickname()).setUserAvatar(user.getAvatar()));
            MapUtils.findAndThen(userMap, request.getInviterUserId(), user ->
                    vo.setInviterNickname(user.getNickname()).setInviterAvatar(user.getAvatar()));
            MapUtils.findAndThen(groupMap, request.getGroupId(), group ->
                    vo.setGroupName(group.getName()).setGroupAvatar(group.getAvatar()));
            return vo;
        });
    }

}
