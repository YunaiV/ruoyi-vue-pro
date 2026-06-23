package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupRequestService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    private ImGroupMemberService groupMemberService;

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

    @GetMapping("/unhandled-list")
    @Operation(summary = "查询「我管理的所有群」下的未处理加群申请列表（不分页）；前端 store 据此派生横幅红点 + Drawer 列表")
    public CommonResult<List<ImGroupRequestRespVO>> getUnhandledRequestList() {
        List<ImGroupRequestDO> list = groupRequestService.getUnhandledRequestListByOwnerOrAdmin(getLoginUserId());
        return success(buildVOList(list));
    }

    @GetMapping("/pull")
    @Operation(summary = "增量拉取「我管理的群」下的加群申请（重连 / 离线补偿）")
    @Parameters({
            @Parameter(name = "lastUpdateTime", description = "上次拉取到的最新更新时间（毫秒时间戳）；首次拉取不传"),
            @Parameter(name = "lastId", description = "上次拉取到的最后一条记录 id；首次拉取不传"),
            @Parameter(name = "limit", description = "单次拉取条数", required = true)
    })
    public CommonResult<List<ImGroupRequestRespVO>> pullMyGroupRequestList(
            @RequestParam(value = "lastUpdateTime", required = false) Long lastUpdateTime,
            @RequestParam(value = "lastId", required = false) Long lastId,
            @RequestParam("limit") @Min(1) @Max(200) Integer limit) {
        List<ImGroupRequestDO> list = groupRequestService.pullGroupRequestList(
                getLoginUserId(), lastUpdateTime, lastId, limit);
        return success(buildVOList(list));
    }

    @GetMapping("/list-by-group")
    @Operation(summary = "查询指定群下的全部加群申请（含已处理）；仅群主 / 管理员可查")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1024")
    public CommonResult<List<ImGroupRequestRespVO>> getGroupRequestListByGroupId(
            @RequestParam("groupId") @NotNull(message = "群编号不能为空") Long groupId) {
        List<ImGroupRequestDO> list = groupRequestService.getGroupRequestListByGroupId(getLoginUserId(), groupId);
        return success(buildVOList(list));
    }

    @GetMapping("/get")
    @Operation(summary = "按 id 单查申请记录（带越权过滤；WebSocket 通知到达后用）")
    @Parameter(name = "id", description = "申请记录编号", required = true)
    public CommonResult<ImGroupRequestRespVO> getGroupRequest(@RequestParam("id") Long id) {
        ImGroupRequestDO request = groupRequestService.getGroupRequest(id);
        if (request == null) {
            return success(null);
        }
        // 越权过滤：申请人 / 邀请人 / 群主 / 管理员之外，当不存在返回 null
        Long currentUserId = getLoginUserId();
        boolean canSee = ObjUtil.equal(request.getUserId(), currentUserId)
                || ObjUtil.equal(request.getInviterUserId(), currentUserId)
                || isGroupOwnerOrAdmin(request.getGroupId(), currentUserId);
        if (!canSee) {
            return success(null);
        }

        // 转换并返回
        return success(CollUtil.getFirst(buildVOList(Collections.singletonList(request))));
    }

    /**
     * 当前用户是否该群的有效群主 / 管理员
     */
    private boolean isGroupOwnerOrAdmin(Long groupId, Long userId) {
        ImGroupMemberDO member = groupMemberService.getGroupMember(groupId, userId);
        return member != null
                && !CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())
                && ImGroupMemberRoleEnum.isOwnerOrAdmin(member.getRole());
    }

    /** 申请记录列表批量转 VO + 关联回填用户 / 群信息 */
    private List<ImGroupRequestRespVO> buildVOList(List<ImGroupRequestDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 聚合 user / inviter 用户信息；convertSetByFlatMap 内部已过滤 null
        Set<Long> userIds = convertSetByFlatMap(list,
                request -> Stream.of(request.getUserId(), request.getInviterUserId()));
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
