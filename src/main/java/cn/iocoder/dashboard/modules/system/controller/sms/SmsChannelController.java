package cn.iocoder.dashboard.modules.system.controller.sms;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.resp.SmsChannelEnumRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsChannelDO;
import cn.iocoder.dashboard.modules.system.service.sms.SmsChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api("短信 渠道/签名 API")
@RestController
@RequestMapping("/sms/channel")
public class SmsChannelController {

    @Resource
    private SmsChannelService service;

    @ApiOperation("获取渠道/签名分页")
    @GetMapping("/page")
    public CommonResult<PageResult<SmsChannelDO>> getPermissionInfo(@Validated SmsChannelPageReqVO reqVO) {
        return success(service.pageChannels(reqVO));
    }

    @ApiOperation("获取渠道枚举")
    @GetMapping("/list/channel-enum")
    public CommonResult<List<SmsChannelEnumRespVO>> getChannelEnums() {
        return success(service.getChannelEnums());
    }


    @ApiOperation("添加消息渠道")
    @PostMapping("/create")
    public CommonResult<Long> add(@Validated @RequestBody SmsChannelCreateReqVO reqVO) {
        return success(service.createChannel(reqVO));
    }

    @ApiOperation("刷新消息渠道信息")
    @PutMapping("/flush")
    public CommonResult<Boolean> flushChannel() {
        return success(service.flushChannel());
    }


}
