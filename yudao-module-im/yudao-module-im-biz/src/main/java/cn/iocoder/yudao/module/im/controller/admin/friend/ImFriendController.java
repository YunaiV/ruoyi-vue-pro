package cn.iocoder.yudao.module.im.controller.admin.friend;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendRespVO;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 好友")
@RestController
@RequestMapping("/im/friend")
@Validated
public class ImFriendController {

    @Resource
    private ImFriendService friendService;

    @GetMapping("/list")
    @Operation(summary = "获得当前登录用户的好友列表")
    public CommonResult<List<ImFriendRespVO>> getMyFriendList() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(friendService.getMyFriendList(userId));
    }

    @GetMapping("/get")
    @Operation(summary = "获得好友详情")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<ImFriendRespVO> getFriend(@RequestParam("friendUserId") Long friendUserId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(friendService.getFriend(userId, friendUserId));
    }

    @PostMapping("/add")
    @Operation(summary = "添加好友（双向建立关系）")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<Boolean> addFriend(
            @RequestParam("friendUserId") @NotNull(message = "好友用户编号不能为空") Long friendUserId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        friendService.addFriend(userId, friendUserId);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除好友（双向软删除）")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<Boolean> deleteFriend(
            @RequestParam("friendUserId") @NotNull(message = "好友用户编号不能为空") Long friendUserId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        friendService.deleteFriend(userId, friendUserId);
        return success(true);
    }

    // TODO @AI：增加一个 updateFriend 接口，目前暂时就 dnd（免打扰）功能，后续可以增加备注等功能

}
