package cn.iocoder.yudao.module.im.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.*;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageReadReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageRespVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
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
 * 管理后台 - IM 私聊消息
 */
@Tag(name = "管理后台 - IM 私聊消息")
@RestController
@RequestMapping("/im/message/private")
@Validated
public class ImPrivateMessageController {

    @Resource
    private ImPrivateMessageService imPrivateMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送私聊消息")
    public CommonResult<ImPrivateMessageRespVO> sendMessage(@Valid @RequestBody ImPrivateMessageSendReqVO reqVO) {
        ImPrivateMessageDO message = imPrivateMessageService.sendMessage(getLoginUserId(), reqVO);
        return success(toRespVO(message));
    }

    // DONE @AI：需要在评估下，是否需要 nextMinId。结论：需要，前端用 nextMinId 做增量拉取游标
    @GetMapping("/pull")
    @Operation(summary = "拉取私聊消息（增量）")
    @Parameter(name = "minId", description = "最小消息 id", required = true, example = "0")
    @Parameter(name = "size", description = "拉取数量", required = true, example = "100")
    public CommonResult<ImMessagePullRespVO<ImPrivateMessageRespVO>> pullMessages(
            @RequestParam("minId") Long minId,
            @RequestParam("size") Integer size) {
        List<ImPrivateMessageDO> messages = imPrivateMessageService.pullMessages(getLoginUserId(), minId, size);
        ImMessagePullRespVO<ImPrivateMessageRespVO> resp = new ImMessagePullRespVO<>();
        resp.setList(messages.stream().map(this::toRespVO).collect(Collectors.toList()));
        resp.setNextMinId(messages.isEmpty() ? minId.toString()
                : messages.get(messages.size() - 1).getId().toString());
        return success(resp);
    }

    // DONE @AI：它是不是不用 VO。结论：需要用 ReqVO，承载 friendId 字段并做参数校验
    @PutMapping("/read")
    @Operation(summary = "标记私聊消息已读")
    public CommonResult<Boolean> readMessages(@Valid @RequestBody ImPrivateMessageReadReqVO reqVO) {
        imPrivateMessageService.readMessages(getLoginUserId(), reqVO.getFriendId());
        return success(true);
    }

    // DONE @AI：不使用 path variable，改为 query param
    @DeleteMapping("/recall")
    @Operation(summary = "撤回私聊消息")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1")
    public CommonResult<Boolean> recallMessage(@RequestParam("id") Long id) {
        imPrivateMessageService.recallMessage(getLoginUserId(), id);
        return success(true);
    }

    // DONE @AI：beanutils。结论：因 id/senderId/receiverId 需 Long→String 手动转换，不适合用 BeanUtils
    /**
     * DO 转换为 RespVO（Long id 统一转 String）
     */
    private ImPrivateMessageRespVO toRespVO(ImPrivateMessageDO message) {
        ImPrivateMessageRespVO vo = new ImPrivateMessageRespVO();
        vo.setId(message.getId().toString());
        vo.setClientMessageId(message.getClientMessageId());
        vo.setSenderId(message.getSenderId().toString());
        vo.setReceiverId(message.getReceiverId().toString());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setStatus(message.getStatus());
        vo.setSendTime(message.getSendTime());
        return vo;
    }

}
