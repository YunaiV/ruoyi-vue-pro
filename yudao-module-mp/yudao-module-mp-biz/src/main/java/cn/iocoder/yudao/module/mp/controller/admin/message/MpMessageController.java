package cn.iocoder.yudao.module.mp.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.MpMessagePageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.MpMessageRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.MpMessageSendReqVO;
import cn.iocoder.yudao.module.mp.convert.message.MpMessageConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.service.message.MpMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 粉丝消息表")
@RestController
@RequestMapping("/mp/message")
@Validated
public class MpMessageController {

    @Resource
    private MpMessageService mpMessageService;

    @GetMapping("/page")
    @ApiOperation("获得粉丝消息表分页")
    @PreAuthorize("@ss.hasPermission('mp:message:query')")
    public CommonResult<PageResult<MpMessageRespVO>> getWxFansMsgPage(@Valid MpMessagePageReqVO pageVO) {
        PageResult<MpMessageDO> pageResult = mpMessageService.getWxFansMsgPage(pageVO);
        return success(MpMessageConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/send")
    @ApiOperation("给粉丝发送消息")
    @PreAuthorize("@ss.hasPermission('mp:message:send')")
    public CommonResult<MpMessageRespVO> sendMessage(@Valid @RequestBody MpMessageSendReqVO reqVO) {
        MpMessageDO message = mpMessageService.sendKefuMessage(reqVO);
        return success(MpMessageConvert.INSTANCE.convert(message));
    }

}
