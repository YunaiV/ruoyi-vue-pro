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
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

    @GetMapping("/pull")
    @Operation(summary = "增量拉取当前用户的好友关系（重连 / 离线补偿）")
    @Parameters({
            @Parameter(name = "lastUpdateTime", description = "上次拉取到的最新更新时间（毫秒时间戳）；首次拉取不传"),
            @Parameter(name = "lastId", description = "上次拉取到的最后一条记录 id；首次拉取不传"),
            @Parameter(name = "limit", description = "单次拉取条数", required = true)
    })
    public CommonResult<List<ImFriendRespVO>> pullMyFriendList(
            @RequestParam(value = "lastUpdateTime", required = false) Long lastUpdateTime,
            @RequestParam(value = "lastId", required = false) Long lastId,
            @RequestParam("limit") @Min(1) @Max(200) Integer limit) {
        List<ImFriendDO> list = friendService.pullFriendList(getLoginUserId(), lastUpdateTime, lastId, limit);
        return success(buildFriendRespVOList(list));
    }

    @GetMapping("/get")
    @Operation(summary = "获得好友详情")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<ImFriendRespVO> getFriend(@RequestParam("friendUserId") Long friendUserId) {
        ImFriendDO friend = friendService.getFriend(getLoginUserId(), friendUserId);
        return success(buildFriendRespVO(friend));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除好友（单向软删除）")
    @Parameters({
            @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048"),
            @Parameter(name = "clear", description = "是否级联清理本端相关数据（如私聊会话）")
    })
    public CommonResult<Boolean> deleteFriend(
            @RequestParam("friendUserId") @NotNull(message = "好友用户编号不能为空") Long friendUserId,
            @RequestParam(value = "clear", required = false) Boolean clear) {
        friendService.deleteFriend(getLoginUserId(), friendUserId, clear);
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "更新好友单边属性（备注 / 免打扰 / 联系人置顶）")
    public CommonResult<Boolean> updateFriend(@Valid @RequestBody ImFriendUpdateReqVO reqVO) {
        friendService.updateFriend(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/block")
    @Operation(summary = "拉黑好友（必须先是好友；单边屏蔽对方私聊消息）")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<Boolean> blockFriend(
            @RequestParam("friendUserId") @NotNull(message = "好友用户编号不能为空") Long friendUserId) {
        friendService.blockFriend(getLoginUserId(), friendUserId);
        return success(true);
    }

    @PutMapping("/unblock")
    @Operation(summary = "移出黑名单")
    @Parameter(name = "friendUserId", description = "好友的用户编号", required = true, example = "2048")
    public CommonResult<Boolean> unblockFriend(
            @RequestParam("friendUserId") @NotNull(message = "好友用户编号不能为空") Long friendUserId) {
        friendService.unblockFriend(getLoginUserId(), friendUserId);
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
        return convertList(friends, friend -> {
            ImFriendRespVO vo = BeanUtils.toBean(friend, ImFriendRespVO.class);
            MapUtils.findAndThen(userMap, friend.getFriendUserId(), user ->
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
