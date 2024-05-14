package cn.iocoder.yudao.module.ai.controller.admin.chat;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.service.chat.AiChatConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 聊天会话")
@RestController
@RequestMapping("/ai/chat/conversation")
@Validated
public class AiChatConversationController {

    @Resource
    private AiChatConversationService chatConversationService;

    @PostMapping("/create-my")
    @Operation(summary = "创建【我的】聊天会话")
    public CommonResult<Long> createChatConversationMy(@RequestBody @Valid AiChatConversationCreateMyReqVO createReqVO) {
        return success(chatConversationService.createChatConversationMy(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update-my")
    @Operation(summary = "更新【我的】聊天会话")
    public CommonResult<Boolean> updateChatConversationMy(@RequestBody @Valid AiChatConversationUpdateMyReqVO updateReqVO) {
        chatConversationService.updateChatConversationMy(updateReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得【我的】聊天会话列表")
    public CommonResult<List<AiChatConversationRespVO>> getChatConversationMyList() {
        List<AiChatConversationDO> list = chatConversationService.getChatConversationListByUserId(getLoginUserId());
        return success(BeanUtils.toBean(list, AiChatConversationRespVO.class));
    }

    // TODO @fan：实现一下
    @GetMapping("/get")
    @Operation(summary = "获得聊天会话")
    @Parameter(name = "id", required = true, description = "会话编号", example = "1024")
    public CommonResult<AiChatConversationRespVO> getConversation(@RequestParam("id") Long id) {
        return success(chatConversationService.getConversationOfValidate(id));
    }

    // TODO @fan：实现一下
    @DeleteMapping("/delete")
    @Operation(summary = "删除聊天会话")
    @Parameter(name = "id", required = true, description = "会话编号", example = "1024")
    public CommonResult<Boolean> deleteConversation(@RequestParam("id") Long id) {
        return success(chatConversationService.deleteConversation(id));
    }

    // ========== 会话管理 ==========

}
