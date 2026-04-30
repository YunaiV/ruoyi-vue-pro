package cn.iocoder.yudao.module.im.controller.admin.friend;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendRespVO;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.singleton;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - IM 好友")
@RestController
@RequestMapping("/im/friend")
@Validated
public class ImFriendController {

    @Resource
    private ImFriendService friendService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/list")
    @Operation(summary = "获得当前登录用户的好友列表")
    public CommonResult<List<ImFriendRespVO>> getMyFriendList() {
        List<ImFriendDO> friends = friendService.getFriendList(getLoginUserId());
        return success(buildFriendRespVOList(friends));
    }

    @GetMapping("/get")
    @Operation(summary = "获得好友详情")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<ImFriendRespVO> getFriend(@RequestParam("friendUserId") Long friendUserId) {
        ImFriendDO friend = friendService.getFriend(getLoginUserId(), friendUserId);
        return success(buildFriendRespVO(friend));
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
    @Operation(summary = "更新好友信息")
    public CommonResult<Boolean> updateFriend(@Valid @RequestBody ImFriendUpdateReqVO reqVO) {
        friendService.updateFriend(getLoginUserId(), reqVO);
        return success(true);
    }

    // ========== 私有方法：VO 组装 ==========

    private List<ImFriendRespVO> buildFriendRespVOList(Collection<ImFriendDO> friends) {
        if (CollUtil.isEmpty(friends)) {
            return Collections.emptyList();
        }
        // 批量聚合 AdminUser 信息（昵称 / 头像），避免 N+1
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertList(friends, ImFriendDO::getFriendUserId));
        return convertList(friends, f -> {
            ImFriendRespVO vo = BeanUtils.toBean(f, ImFriendRespVO.class);
            MapUtils.findAndThen(userMap, f.getFriendUserId(), user ->
                    vo.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
            // 备注 / 昵称的拼音，给前端做字母分桶 + 拼音搜索
            vo.setDisplayNamePinyin(StrUtils.toPinyin(vo.getDisplayName()))
                    .setNicknamePinyin(StrUtils.toPinyin(vo.getNickname()));
            return vo;
        });
    }

    private ImFriendRespVO buildFriendRespVO(ImFriendDO friend) {
        if (friend == null) {
            return null;
        }
        return CollUtil.getFirst(buildFriendRespVOList(singleton(friend)));
    }

}
