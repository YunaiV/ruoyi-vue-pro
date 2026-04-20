package cn.iocoder.yudao.module.im.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageListReqVO;
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
    private ImPrivateMessageService privateMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送私聊消息")
    public CommonResult<ImPrivateMessageRespVO> sendPrivateMessage(
            @Valid @RequestBody ImPrivateMessageSendReqVO reqVO) {
        ImPrivateMessageDO message = privateMessageService.sendPrivateMessage(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(message, ImPrivateMessageRespVO.class));
    }

    @GetMapping("/pull")
    @Operation(summary = "拉取私聊消息（增量）")
    @Parameter(name = "minId", description = "最小消息 id", required = true, example = "0")
    @Parameter(name = "size", description = "拉取数量", required = true, example = "100")
    public CommonResult<List<ImPrivateMessageRespVO>> pullPrivateMessageList(@RequestParam("minId") Long minId,
                                                                             @RequestParam("size") Integer size) {
        List<ImPrivateMessageDO> messages = privateMessageService.pullPrivateMessageList(getLoginUserId(), minId, size);
        return success(BeanUtils.toBean(messages, ImPrivateMessageRespVO.class));
    }

    @PutMapping("/read")
    @Operation(summary = "标记私聊消息已读")
    @Parameter(name = "receiverId", description = "接收方用户编号（对方）", required = true, example = "2")
    public CommonResult<Boolean> readPrivateMessages(@RequestParam("receiverId") Long receiverId) {
        privateMessageService.readPrivateMessages(getLoginUserId(), receiverId);
        return success(true);
    }

    @DeleteMapping("/recall")
    @Operation(summary = "撤回私聊消息")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1")
    public CommonResult<ImPrivateMessageRespVO> recallPrivateMessage(@RequestParam("id") Long id) {
        ImPrivateMessageDO message = privateMessageService.recallPrivateMessage(getLoginUserId(), id);
        return success(BeanUtils.toBean(message, ImPrivateMessageRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "查询私聊历史消息")
    public CommonResult<List<ImPrivateMessageRespVO>> getPrivateMessageList(@Valid ImPrivateMessageListReqVO reqVO) {
        List<ImPrivateMessageDO> messages = privateMessageService.getPrivateMessageList(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(messages, ImPrivateMessageRespVO.class));
    }

}
