package cn.iocoder.dashboard.modules.system.controller.sms;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelRespVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.sms.SysSmsChannelConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "短信渠道")
@RestController
@RequestMapping("system/sms-channel")
public class SysSmsChannelController {

    @Resource
    private SysSmsChannelService smsChannelService;

    @PostMapping("/create")
    @ApiOperation("创建短信渠道")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:create')")
    public CommonResult<Long> createSmsChannel(@Valid @RequestBody SysSmsChannelCreateReqVO createReqVO) {
        return success(smsChannelService.createSmsChannel(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新短信渠道")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:update')")
    public CommonResult<Boolean> updateSmsChannel(@Valid @RequestBody SysSmsChannelUpdateReqVO updateReqVO) {
        smsChannelService.updateSmsChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除短信渠道")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:sms-channel:delete')")
    public CommonResult<Boolean> deleteSmsChannel(@RequestParam("id") Long id) {
        smsChannelService.deleteSmsChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得短信渠道")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:sms-channel:query')")
    public CommonResult<SysSmsChannelRespVO> getSmsChannel(@RequestParam("id") Long id) {
        SysSmsChannelDO smsChannel = smsChannelService.getSmsChannel(id);
        return success(SysSmsChannelConvert.INSTANCE.convert(smsChannel));
    }

    @GetMapping("/page")
    @ApiOperation("获得短信渠道分页")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:query')")
    public CommonResult<PageResult<SysSmsChannelRespVO>> getSmsChannelPage(@Valid SysSmsChannelPageReqVO pageVO) {
        PageResult<SysSmsChannelDO> pageResult = smsChannelService.getSmsChannelPage(pageVO);
        return success(SysSmsChannelConvert.INSTANCE.convertPage(pageResult));
    }

}
