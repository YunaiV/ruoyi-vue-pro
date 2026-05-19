package cn.iocoder.yudao.module.im.controller.admin.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMaterialService;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.IM_CHANNEL_MATERIAL_NOT_EXISTS;

@Tag(name = "用户 APP - IM 频道素材")
@RestController
@RequestMapping("/im/channel/material")
@Validated
public class ImChannelMaterialController {

    @Resource
    private ImChannelMaterialService channelMaterialService;
    @Resource
    private ImChannelMessageService channelMessageService;

    // TODO @AI：要不，还是不校验了。会存在一个情况，有人转发图文消息到私聊里。（先不考虑这块的安全性了；简化简化！）
    @GetMapping("/get")
    @Operation(summary = "获取素材详情；用于客户端点击图文卡片渲染详情页")
    @Parameter(name = "id", description = "素材编号", required = true, example = "1024")
    public CommonResult<ImChannelMaterialRespVO> getMaterial(@RequestParam("id") Long id) {
        // 防横向越权：仅允许拉取自己曾经收到过的素材，否则视为不存在
        if (!channelMessageService.isUserReceivedMaterial(getLoginUserId(), id)) {
            throw exception(IM_CHANNEL_MATERIAL_NOT_EXISTS);
        }
        // 获取素材详情
        ImChannelMaterialDO material = channelMaterialService.validateMaterialExists(id);
        return success(BeanUtils.toBean(material, ImChannelMaterialRespVO.class));
    }

}
