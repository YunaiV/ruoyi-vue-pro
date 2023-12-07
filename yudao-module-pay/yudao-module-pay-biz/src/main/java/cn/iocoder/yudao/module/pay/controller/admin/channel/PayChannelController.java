package cn.iocoder.yudao.module.pay.controller.admin.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 支付渠道")
@RestController
@RequestMapping("/pay/channel")
@Validated
public class PayChannelController {

    @Resource
    private PayChannelService channelService;

    @PostMapping("/create")
    @Operation(summary = "创建支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody PayChannelCreateReqVO createReqVO) {
        return success(channelService.createChannel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody PayChannelUpdateReqVO updateReqVO) {
        channelService.updateChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除支付渠道 ")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        channelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得支付渠道")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PayChannelRespVO> getChannel(@RequestParam(value = "id", required = false) Long id,
                                                     @RequestParam(value = "appId", required = false) Long appId,
                                                     @RequestParam(value = "code", required = false) String code) {
        PayChannelDO channel = null;
        if (id != null) {
            channel = channelService.getChannel(id);
        } else if (appId != null && code != null) {
            channel = channelService.getChannelByAppIdAndCode(appId, code);
        }
        return success(PayChannelConvert.INSTANCE.convert(channel));
    }

    @GetMapping("/get-enable-code-list")
    @Operation(summary = "获得指定应用的开启的支付渠道编码列表")
    @Parameter(name = "appId", description = "应用编号", required = true, example = "1")
    public CommonResult<Set<String>> getEnableChannelCodeList(@RequestParam("appId") Long appId) {
        List<PayChannelDO> channels = channelService.getEnableChannelList(appId);
        return success(convertSet(channels, PayChannelDO::getCode));
    }

}
