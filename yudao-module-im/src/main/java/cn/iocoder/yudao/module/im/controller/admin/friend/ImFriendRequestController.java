package cn.iocoder.yudao.module.im.controller.admin.friend;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.request.ImFriendRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.request.ImFriendRequestRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.service.friend.ImFriendRequestService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

/**
 * IM 好友申请记录 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IM 好友申请")
@RestController
@RequestMapping("/im/friend-request")
@Validated
public class ImFriendRequestController {

    @Resource
    private ImFriendRequestService friendRequestService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/apply")
    @Operation(summary = "发起好友申请")
    public CommonResult<Long> applyFriend(@Valid @RequestBody ImFriendRequestApplyReqVO reqVO) {
        ImFriendRequestDO request = friendRequestService.applyFriend(getLoginUserId(), reqVO);
        return success(request != null ? request.getId() : null);
    }

    @PutMapping("/agree")
    @Operation(summary = "同意好友申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    public CommonResult<Boolean> agreeFriendRequest(
            @RequestParam("id") @NotNull(message = "申请编号不能为空") Long id) {
        friendRequestService.agreeFriendRequest(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/refuse")
    @Operation(summary = "拒绝好友申请")
    public CommonResult<Boolean> refuseFriendRequest(
            @RequestParam("id") @NotNull(message = "申请编号不能为空") Long id,
            @RequestParam(value = "handleContent", required = false)
            @Size(max = 255, message = "处理理由最多 255 个字符") String handleContent) {
        friendRequestService.refuseFriendRequest(getLoginUserId(), id, handleContent);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "查询「我相关」的好友申请列表（含我发起的、别人加我的）")
    public CommonResult<List<ImFriendRequestRespVO>> getMyFriendRequestList() {
        List<ImFriendRequestDO> list = friendRequestService.getMyFriendRequestList(getLoginUserId());
        return success(buildList(list));
    }

    // ========== 私有方法：VO 组装 ==========

    private List<ImFriendRequestRespVO> buildList(List<ImFriendRequestDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 双向 OR 列表，userIds 取 from + to 两组并集
        Set<Long> userIds = convertSetByFlatMap(list,
                request -> Stream.of(request.getFromUserId(), request.getToUserId()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        return convertList(list, request -> {
            ImFriendRequestRespVO vo = BeanUtils.toBean(request, ImFriendRequestRespVO.class);
            MapUtils.findAndThen(userMap, request.getFromUserId(), user ->
                    vo.setFromNickname(user.getNickname()).setFromAvatar(user.getAvatar()));
            MapUtils.findAndThen(userMap, request.getToUserId(), user ->
                    vo.setToNickname(user.getNickname()).setToAvatar(user.getAvatar()));
            return vo;
        });
    }

}
