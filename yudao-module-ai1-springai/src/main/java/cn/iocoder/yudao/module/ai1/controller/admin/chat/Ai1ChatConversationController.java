package cn.iocoder.yudao.module.ai1.controller.admin.chat;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatConversationService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI1 对话")
@RestController
@RequestMapping("/ai1/chat/conversation")
@Validated
public class Ai1ChatConversationController {

    @Resource
    private Ai1ChatConversationService chatConversationService;

    @Resource
    private AdminUserApi adminUserApi;

    // TODO @AI：分块下，哪些是 我的；哪些是管理的。类似 === === 这种噢；

    @PostMapping("/create-my")
    @Operation(summary = "创建【我的】对话")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    public CommonResult<Long> createChatConversationMy(@Valid @RequestBody Ai1ChatConversationCreateMyReqVO createReqVO) {
        return success(chatConversationService.createChatConversationMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/update-my")
    @Operation(summary = "修改【我的】对话标题")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    public CommonResult<Boolean> updateChatConversationMy(@Valid @RequestBody Ai1ChatConversationUpdateMyReqVO updateReqVO) {
        chatConversationService.updateChatConversationMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】对话", description = "连带删除消息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    public CommonResult<Boolean> deleteChatConversationMy(@RequestParam("id") Long id) {
        chatConversationService.deleteChatConversationMy(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得【我的】对话列表", description = "指定 Agent 下，按创建时间倒序")
    @Parameter(name = "agentId", description = "Agent 编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    public CommonResult<List<Ai1ChatConversationRespVO>> getChatConversationMyList(@RequestParam("agentId") Long agentId) {
        List<Ai1ChatConversationDO> list = chatConversationService.getChatConversationListByAgentIdAndUserId(agentId, getLoginUserId());
        return success(BeanUtils.toBean(list, Ai1ChatConversationRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得对话分页", description = "管理端查看 Agent 下全部用户的对话")
    @PreAuthorize("@ss.hasPermission('ai1:chat-conversation:query')")
    public CommonResult<PageResult<Ai1ChatConversationRespVO>> getChatConversationPage(@Valid Ai1ChatConversationPageReqVO pageReqVO) {
        PageResult<Ai1ChatConversationDO> pageResult = chatConversationService.getChatConversationPage(pageReqVO);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(pageResult.getList(), Ai1ChatConversationDO::getUserId));
        return success(BeanUtils.toBean(pageResult, Ai1ChatConversationRespVO.class, respVO ->
                MapUtils.findAndThen(userMap, respVO.getUserId(), user -> respVO.setUserNickname(user.getNickname()))));
    }

}
