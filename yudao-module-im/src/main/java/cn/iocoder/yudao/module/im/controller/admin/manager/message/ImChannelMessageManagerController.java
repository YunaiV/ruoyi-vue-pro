package cn.iocoder.yudao.module.im.controller.admin.manager.message;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessageRespVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelDO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMaterialService;
import cn.iocoder.yudao.module.im.service.message.ImChannelMessageService;
import cn.iocoder.yudao.module.im.service.channel.ImChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - IM 频道消息")
@RestController
@RequestMapping("/im/manager/channel-message")
@Validated
public class ImChannelMessageManagerController {

    @Resource
    private ImChannelMessageService channelMessageService;
    @Resource
    private ImChannelService channelService;
    @Resource
    private ImChannelMaterialService channelMaterialService;

    @PostMapping("/send")
    @Operation(summary = "立即推送频道消息")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-message:send')")
    public CommonResult<Long> sendMessage(@Valid @RequestBody ImChannelMessageSendReqVO reqVO) {
        return success(channelMessageService.sendMessage(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除频道消息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-message:delete')")
    public CommonResult<Boolean> deleteMessage(@RequestParam("id") Long id) {
        channelMessageService.deleteMessage(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得频道消息分页；回填频道名 / 素材标题")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-message:query')")
    public CommonResult<PageResult<ImChannelMessageRespVO>> getMessagePage(@Valid ImChannelMessagePageReqVO pageReqVO) {
        PageResult<ImChannelMessageDO> pageResult = channelMessageService.getMessagePage(pageReqVO);
        return success(new PageResult<>(buildMessageRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得频道消息详情")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-message:query')")
    public CommonResult<ImChannelMessageRespVO> getMessage(@RequestParam("id") Long id) {
        ImChannelMessageDO message = channelMessageService.getMessage(id);
        return success(buildMessageRespVO(message));
    }

    // ==================== 拼接 VO ====================

    private ImChannelMessageRespVO buildMessageRespVO(ImChannelMessageDO message) {
        if (message == null) {
            return null;
        }
        return CollUtil.getFirst(buildMessageRespVOList(Collections.singletonList(message)));
    }

    private List<ImChannelMessageRespVO> buildMessageRespVOList(List<ImChannelMessageDO> messages) {
        if (CollUtil.isEmpty(messages)) {
            return Collections.emptyList();
        }
        // 1. 查询关联数据
        Map<Long, ImChannelDO> channelMap = channelService.getChannelMap(
                convertSet(messages, ImChannelMessageDO::getChannelId));
        Map<Long, ImChannelMaterialDO> materialMap = channelMaterialService.getMaterialMap(
                convertSet(messages, ImChannelMessageDO::getMaterialId));
        // 2. 拼接 VO
        return BeanUtils.toBean(messages, ImChannelMessageRespVO.class, vo -> {
            MapUtils.findAndThen(channelMap, vo.getChannelId(), channel -> vo.setChannelName(channel.getName()));
            MapUtils.findAndThen(materialMap, vo.getMaterialId(),
                    material -> vo.setMaterialTitle(material.getTitle()).setMaterialCoverUrl(material.getCoverUrl()));
        });
    }

}
