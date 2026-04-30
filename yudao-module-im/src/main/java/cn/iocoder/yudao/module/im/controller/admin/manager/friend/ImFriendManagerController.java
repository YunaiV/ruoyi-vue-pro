package cn.iocoder.yudao.module.im.controller.admin.manager.friend;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerRespVO;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 好友管理")
@RestController
@RequestMapping("/im/manager/friend")
@Validated
public class ImFriendManagerController {

    @Resource
    private ImFriendService friendService;

    @GetMapping("/page")
    @Operation(summary = "获得好友关系分页")
    @PreAuthorize("@ss.hasPermission('im:manager:friend:query')")
    public CommonResult<PageResult<ImFriendManagerRespVO>> getFriendPage(
            @Valid ImFriendManagerPageReqVO pageReqVO) {
        return success(friendService.getFriendPage(pageReqVO));
    }

}
