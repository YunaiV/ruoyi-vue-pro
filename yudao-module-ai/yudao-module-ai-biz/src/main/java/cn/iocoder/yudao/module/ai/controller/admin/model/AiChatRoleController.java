package cn.iocoder.yudao.module.ai.controller.admin.model;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.*;
import cn.iocoder.yudao.module.ai.service.AiChatRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// TODO @fan：调整下接口；相关 vo 的命名等等；modal => model
/**
 * ai chat 角色
 *
 * @fansili
 * @since v1.0
 */
@Tag(name = "A4-chat角色")
@RestController
@RequestMapping("/ai/chat/role")
@Slf4j
@AllArgsConstructor
public class AiChatRoleController {

    private final AiChatRoleService chatRoleService;

    @Operation(summary = "chat角色 - 角色列表")
    @GetMapping("/list")
    public PageResult<AiChatRoleListRes> list(@Validated @ModelAttribute AiChatRoleListReq req) {
        return chatRoleService.list(req);
    }

    @Operation(summary = "chat角色 - 添加")
    @PutMapping("/add")
    public CommonResult<Void> add(@Validated @RequestBody AiChatRoleAddReqVO req) {
        chatRoleService.add(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改")
    @PostMapping("/update")
    public CommonResult<Void> update(@Validated @RequestBody AiChatRoleUpdateReqVO req) {
        chatRoleService.update(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 修改可见性")
    @PostMapping("/update-public-status")
    public CommonResult<Void> updatePublicStatus(@Validated @RequestBody AiChatRoleUpdatePublicStatusReqVO req) {
        chatRoleService.updatePublicStatus(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "chat角色 - 删除")
    @DeleteMapping("/delete")
    public CommonResult<Void> delete(@RequestParam("id") Long id) {
        chatRoleService.delete(id);
        return CommonResult.success(null);
    }
}
