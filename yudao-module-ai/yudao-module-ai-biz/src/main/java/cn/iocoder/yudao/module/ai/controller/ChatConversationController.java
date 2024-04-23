package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.service.ChatConversationService;
import cn.iocoder.yudao.module.ai.vo.ChatConversationCreateRoleReq;
import cn.iocoder.yudao.module.ai.vo.ChatConversationCreateUserReq;
import cn.iocoder.yudao.module.ai.vo.ChatConversationListReq;
import cn.iocoder.yudao.module.ai.vo.ChatConversationRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ia 模块
 *
 * @author fansili
 * @time 2024/4/13 17:44
 * @since 1.0
 */
@Tag(name = "A2聊天-对话")
@RestController
@RequestMapping("/ai/chat/conversation")
@Slf4j
@AllArgsConstructor
public class ChatConversationController {

    private final ChatConversationService chatConversationService;

    @Operation(summary = "创建 - 对话普通对话")
    @PostMapping("/createConversation")
    public CommonResult<ChatConversationRes> createConversation(@RequestBody @Validated ChatConversationCreateUserReq req) {
        return CommonResult.success(chatConversationService.createConversation(req));
    }

    @Operation(summary = "创建 - 对话角色对话")
    @PostMapping("/createRoleConversation")
    public CommonResult<ChatConversationRes> createRoleConversation(@RequestBody @Validated ChatConversationCreateRoleReq req) {
        return CommonResult.success(chatConversationService.createRoleConversation(req));
    }

    @Operation(summary = "获取 - 获取对话")
    @GetMapping("/{id}")
    public CommonResult<ChatConversationRes> getConversation(@PathVariable("id") Long id) {
        return CommonResult.success(chatConversationService.getConversation(id));
    }

    @Operation(summary = "获取 - 获取对话list")
    @GetMapping("/list")
    public CommonResult<List<ChatConversationRes>> listConversation(@ModelAttribute @Validated ChatConversationListReq req) {
        return CommonResult.success(chatConversationService.listConversation(req));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable("id") Long id) {
        chatConversationService.delete(id);
        return CommonResult.success(null);
    }
}
