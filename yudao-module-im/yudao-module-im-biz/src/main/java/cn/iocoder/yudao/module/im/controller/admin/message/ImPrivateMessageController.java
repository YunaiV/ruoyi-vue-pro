package cn.iocoder.yudao.module.im.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.*;
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
        return success(BeanUtils.toBean(message, ImPrivateMessageRespVO.class));
    }

    @GetMapping("/pull")
    @Operation(summary = "拉取私聊消息（增量）")
    @Parameter(name = "minId", description = "最小消息 id", required = true, example = "0")
    @Parameter(name = "size", description = "拉取数量", required = true, example = "100")
    public CommonResult<ImMessagePullRespVO<ImPrivateMessageRespVO>> pullMessages(
            @RequestParam("minId") Long minId,
            @RequestParam("size") Integer size) {
        List<ImPrivateMessageDO> messages = imPrivateMessageService.pullMessages(getLoginUserId(), minId, size);
        ImMessagePullRespVO<ImPrivateMessageRespVO> resp = new ImMessagePullRespVO<>();
        resp.setList(BeanUtils.toBean(messages, ImPrivateMessageRespVO.class));
        resp.setNextMinId(messages.isEmpty() ? minId
                : messages.get(messages.size() - 1).getId());
        return success(resp);
    }

    @PutMapping("/read")
    @Operation(summary = "标记私聊消息已读")
    @Parameter(name = "friendId", description = "好友用户编号", required = true, example = "2")
    public CommonResult<Boolean> readMessages(@RequestParam("friendId") Long friendId) {
        imPrivateMessageService.readMessages(getLoginUserId(), friendId);
        return success(true);
    }

    @DeleteMapping("/recall")
    @Operation(summary = "撤回私聊消息")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1")
    public CommonResult<Boolean> recallMessage(@RequestParam("id") Long id) {
        imPrivateMessageService.recallMessage(getLoginUserId(), id);
        return success(true);
    }

}
