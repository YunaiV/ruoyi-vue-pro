package cn.iocoder.yudao.module.im.controller.admin.friend;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendRespVO;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

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
        return success(friendService.getMyFriendList(getLoginUserId()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得好友详情")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<ImFriendRespVO> getFriend(@RequestParam("friendUserId") Long friendUserId) {
        return success(friendService.getFriend(getLoginUserId(), friendUserId));
    }

    @PostMapping("/add")
    @Operation(summary = "添加好友（双向建立关系）")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<Boolean> addFriend(
            @RequestParam("friendUserId") @NotNull(message = "好友用户编号不能为空") Long friendUserId) {
        friendService.addFriend(getLoginUserId(), friendUserId);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除好友（双向软删除）")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<Boolean> deleteFriend(
            @RequestParam("friendUserId") @NotNull(message = "好友用户编号不能为空") Long friendUserId) {
        friendService.deleteFriend(getLoginUserId(), friendUserId);
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "更新好友信息（当前仅免打扰）")
    public CommonResult<Boolean> updateFriend(@Valid @RequestBody ImFriendUpdateReqVO reqVO) {
        friendService.updateFriend(getLoginUserId(), reqVO);
        return success(true);
    }

}
