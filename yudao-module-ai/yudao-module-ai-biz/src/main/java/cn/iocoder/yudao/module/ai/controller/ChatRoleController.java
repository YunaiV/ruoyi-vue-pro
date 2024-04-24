package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.service.ChatRoleService;
import cn.iocoder.yudao.module.ai.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ai chat 角色
 *
 * @fansili
 * @since v1.0
 */
@Tag(name = "A4-chat角色")
@RestController
@RequestMapping("/ai/chat")
@Slf4j
@AllArgsConstructor
public class ChatRoleController {

    private final ChatRoleService chatRoleService;

    @Operation(summary = "chat角色 - 角色列表")
    @GetMapping("/role/list")
    public PageResult<ChatRoleListRes> list(@Validated @ModelAttribute ChatRoleListReq req) {
        return chatRoleService.list(req);
    }

    @Operation(summary = "chat角色 - 添加")
    @PutMapping("/role")
    public CommonResult<Void> add(@Validated @RequestBody ChatRoleAddReq req) {
        chatRoleService.add(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改")
    @PostMapping("/role/{id}")
    public CommonResult<Void> update(@PathVariable("id") Long id,
                                     @Validated @RequestBody ChatRoleUpdateReq req) {
        chatRoleService.update(id, req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改可见性")
    @PostMapping("/role/update-visibility")
    public CommonResult<Void> updateVisibility(@Validated @RequestBody ChatRoleUpdateVisibilityReq req) {
        chatRoleService.updateVisibility(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改可见性")
    @DeleteMapping("/role")
    public CommonResult<Void> delete(@RequestParam("chatRoleId") Long chatRoleId) {
        chatRoleService.delete(chatRoleId);
        return CommonResult.success(null);
    }
}
