package cn.iocoder.yudao.module.ai.controller.app.chat;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.app.chat.vo.*;
import cn.iocoder.yudao.module.ai.controller.app.vo.*;
import cn.iocoder.yudao.module.ai.service.ChatRoleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ai chat 角色
 *
 * @fansili
 * @since v1.0
 */
@RestController
@RequestMapping("/chat-role")
@AllArgsConstructor
public class AiChatRoleController {

    private final ChatRoleService chatRoleService;

    @Operation(summary = "chat角色 - 角色列表")
    @GetMapping("/list")
    public CommonResult<ChatRoleListRes> list(@Validated @ModelAttribute ChatRoleListReq req) {
        return chatRoleService.list(req);
    }

    @Operation(summary = "chat角色 - 添加")
    @PutMapping("/add")
    public CommonResult<Void> add(@Validated @RequestBody ChatRoleAddReq req) {
        chatRoleService.add(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改")
    @PostMapping("/update")
    public CommonResult<Void> update(@Validated @RequestBody ChatRoleUpdateReq req) {
        chatRoleService.update(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改可见性")
    @PostMapping("/update-visibility")
    public CommonResult<Void> updateVisibility(@Validated @RequestBody ChatRoleUpdateVisibilityReq req) {
        chatRoleService.updateVisibility(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改可见性")
    @DeleteMapping("/delete")
    public CommonResult<Void> delete(@RequestParam("chatRoleId") Long chatRoleId) {
        chatRoleService.delete(chatRoleId);
        return CommonResult.success(null);
    }
}
