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
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 批量查询频道和素材，并回填频道名 / 素材标题
        Map<Long, ImChannelDO> channelMap = channelService.getChannelMap(
                convertSet(pageResult.getList(), ImChannelMessageDO::getChannelId));
        Map<Long, ImChannelMaterialDO> materialMap = channelMaterialService.getMaterialMap(
                convertSet(pageResult.getList(), ImChannelMessageDO::getMaterialId));
        return success(BeanUtils.toBean(pageResult, ImChannelMessageRespVO.class, vo -> {
            MapUtils.findAndThen(channelMap, vo.getChannelId(), c -> vo.setChannelName(c.getName()));
            MapUtils.findAndThen(materialMap, vo.getMaterialId(),
                    material -> vo.setMaterialTitle(material.getTitle()).setMaterialCoverUrl(material.getCoverUrl()));
        }));
    }

}
