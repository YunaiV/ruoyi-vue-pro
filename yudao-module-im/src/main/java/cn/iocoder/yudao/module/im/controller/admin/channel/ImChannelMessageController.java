package cn.iocoder.yudao.module.im.controller.admin.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.channel.vo.ImChannelMessagePullRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - IM 频道消息")
@RestController
@RequestMapping("/im/channel/message")
@Validated
public class ImChannelMessageController {

    @Resource
    private ImChannelMessageService channelMessageService;

    @GetMapping("/pull")
    @Operation(summary = "拉取频道消息（离线增量）；按 minId 游标分页")
    public CommonResult<List<ImChannelMessagePullRespVO>> pull(
            @RequestParam(value = "minId", defaultValue = "0") Long minId,
            @RequestParam(value = "size", defaultValue = "100") Integer size) {
        List<ImChannelMessageDO> list = channelMessageService.getMessageListForPull(getLoginUserId(), minId, size);
        return success(BeanUtils.toBean(list, ImChannelMessagePullRespVO.class));
    }

}
