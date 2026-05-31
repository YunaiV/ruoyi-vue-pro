package cn.iocoder.yudao.module.im.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageRespVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - IM 群聊消息")
@RestController
@RequestMapping("/im/message/group")
@Validated
public class ImGroupMessageController {

    @Resource
    private ImGroupMessageService groupMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送群聊消息")
    public CommonResult<ImGroupMessageRespVO> sendGroupMessage(@Valid @RequestBody ImGroupMessageSendReqVO reqVO) {
        ImGroupMessageDO message = groupMessageService.sendGroupMessage(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(message, ImGroupMessageRespVO.class));
    }

    @GetMapping("/pull")
    @Operation(summary = "拉取群聊消息（增量）")
    @Parameter(name = "minId", description = "最小消息 id", required = true, example = "0")
    @Parameter(name = "size", description = "拉取数量", required = true, example = "100")
    public CommonResult<List<ImGroupMessageRespVO>> pullGroupMessageList(
            @RequestParam("minId") Long minId,
            @RequestParam("size") @Min(value = 1, message = "拉取数量最小值为 1") Integer size) {
        List<ImGroupMessageDO> messages = groupMessageService.pullGroupMessageList(getLoginUserId(), minId, size);
        return success(BeanUtils.toBean(messages, ImGroupMessageRespVO.class));
    }

    @PutMapping("/read")
    @Operation(summary = "标记群聊消息已读")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1")
    @Parameter(name = "messageId", description = "已读到的消息编号", required = true, example = "100")
    public CommonResult<Boolean> readGroupMessages(@RequestParam("groupId") Long groupId,
                                                   @RequestParam("messageId") Long messageId) {
        groupMessageService.readGroupMessages(getLoginUserId(), groupId, messageId);
        return success(true);
    }

    @DeleteMapping("/recall")
    @Operation(summary = "撤回群聊消息")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1")
    public CommonResult<ImGroupMessageRespVO> recallGroupMessage(@RequestParam("id") Long id) {
        ImGroupMessageDO message = groupMessageService.recallGroupMessage(getLoginUserId(), id);
        return success(BeanUtils.toBean(message, ImGroupMessageRespVO.class));
    }

    @GetMapping("/get-read-user-ids")
    @Operation(summary = "获取群消息已读用户列表")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1")
    @Parameter(name = "messageId", description = "消息编号", required = true, example = "1")
    public CommonResult<List<Long>> getGroupReadUserIds(@RequestParam("groupId") Long groupId,
                                                        @RequestParam("messageId") Long messageId) {
        return success(groupMessageService.getGroupReadUserIds(getLoginUserId(), groupId, messageId));
    }

    @GetMapping("/list")
    @Operation(summary = "查询群聊历史消息")
    public CommonResult<List<ImGroupMessageRespVO>> getGroupMessageList(@Valid ImGroupMessageListReqVO reqVO) {
        List<ImGroupMessageDO> messages = groupMessageService.getGroupMessageList(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(messages, ImGroupMessageRespVO.class));
    }

}
