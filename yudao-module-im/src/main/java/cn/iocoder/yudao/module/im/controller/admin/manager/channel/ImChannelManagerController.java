package cn.iocoder.yudao.module.im.controller.admin.manager.channel;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel.ImChannelPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel.ImChannelRespVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel.ImChannelSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelDO;
import cn.iocoder.yudao.module.im.service.channel.ImChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 频道")
@RestController
@RequestMapping("/im/manager/channel")
@Validated
public class ImChannelManagerController {

    @Resource
    private ImChannelService channelService;

    @PostMapping("/create")
    @Operation(summary = "新增频道")
    @PreAuthorize("@ss.hasPermission('im:manager:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody ImChannelSaveReqVO reqVO) {
        return success(channelService.createChannel(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改频道")
    @PreAuthorize("@ss.hasPermission('im:manager:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody ImChannelSaveReqVO reqVO) {
        channelService.updateChannel(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除频道")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        channelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得频道分页")
    @PreAuthorize("@ss.hasPermission('im:manager:channel:query')")
    public CommonResult<PageResult<ImChannelRespVO>> getChannelPage(@Valid ImChannelPageReqVO pageReqVO) {
        PageResult<ImChannelDO> pageResult = channelService.getChannelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImChannelRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得频道详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:channel:query')")
    public CommonResult<ImChannelRespVO> getChannel(@RequestParam("id") Long id) {
        ImChannelDO channel = channelService.getChannel(id);
        return success(BeanUtils.toBean(channel, ImChannelRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用的频道精简列表；前端表单选择频道时调用")
    public CommonResult<List<ImChannelRespVO>> getSimpleChannelList() {
        List<ImChannelDO> list = channelService.getChannelListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(BeanUtils.toBean(list, ImChannelRespVO.class));
    }

}
