package cn.iocoder.yudao.module.im.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageRespVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - IM 群聊消息
 */
@Tag(name = "管理后台 - IM 群聊消息")
@RestController
@RequestMapping("/im/message/group")
@Validated
public class ImGroupMessageController {

    @Resource
    private ImGroupMessageService imGroupMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送群聊消息")
    public CommonResult<ImGroupMessageRespVO> sendGroupMessage(@Valid @RequestBody ImGroupMessageSendReqVO reqVO) {
        ImGroupMessageDO message = imGroupMessageService.sendGroupMessage(getLoginUserId(), reqVO);
        return success(toRespVO(message));
    }

    @GetMapping("/pull")
    @Operation(summary = "拉取群聊消息（增量）")
    @Parameter(name = "minId", description = "最小消息 id", required = true, example = "0")
    @Parameter(name = "size", description = "拉取数量", required = true, example = "100")
    public CommonResult<List<ImGroupMessageRespVO>> pullGroupMessages(
            @RequestParam("minId") Long minId,
            @RequestParam("size") Integer size) {
        List<ImGroupMessageDO> messages = imGroupMessageService.pullGroupMessages(getLoginUserId(), minId, size);
        return success(messages.stream().map(this::toRespVO).collect(Collectors.toList()));
    }

    @PutMapping("/read")
    @Operation(summary = "标记群聊消息已读")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1")
    public CommonResult<Boolean> readGroupMessages(@RequestParam("groupId") Long groupId) {
        imGroupMessageService.readGroupMessages(getLoginUserId(), groupId);
        return success(true);
    }

    @DeleteMapping("/recall")
    @Operation(summary = "撤回群聊消息")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1")
    public CommonResult<Boolean> recallGroupMessage(@RequestParam("id") Long id) {
        imGroupMessageService.recallGroupMessage(getLoginUserId(), id);
        return success(true);
    }

    // DONE @AI：这个接口，确认需要！用于群回执人数展示
    @GetMapping("/read-users")
    @Operation(summary = "获取群消息已读用户列表")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1")
    @Parameter(name = "messageId", description = "消息编号", required = true, example = "1")
    public CommonResult<List<String>> getGroupReadUsers(
            @RequestParam("groupId") Long groupId,
            @RequestParam("messageId") Long messageId) {
        List<Long> readUserIds = imGroupMessageService.getGroupReadUsers(getLoginUserId(), groupId, messageId);
        // Long -> String 转换
        return success(readUserIds.stream().map(String::valueOf).collect(Collectors.toList()));
    }

    /**
     * DO 转换为 RespVO（Long id 统一转 String）
     */
    // DONE @AI：BeanUtils？结论：因 id/senderId/groupId 需 Long→String 手动转换，不适合 BeanUtils
    private ImGroupMessageRespVO toRespVO(ImGroupMessageDO message) {
        ImGroupMessageRespVO vo = new ImGroupMessageRespVO();
        vo.setId(message.getId().toString());
        vo.setClientMessageId(message.getClientMessageId());
        vo.setSenderId(message.getSenderId().toString());
        vo.setGroupId(message.getGroupId().toString());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setStatus(message.getStatus());
        vo.setSendTime(message.getSendTime());
        vo.setAtUserIds(message.getAtUserIds());
        vo.setReceiverUserIds(message.getReceiverUserIds());
        vo.setReceiptStatus(message.getReceiptStatus());
        return vo;
    }

}
