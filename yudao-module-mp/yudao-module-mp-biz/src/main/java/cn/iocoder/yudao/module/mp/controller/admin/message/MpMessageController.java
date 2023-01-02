package cn.iocoder.yudao.module.mp.controller.admin.message;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.mp.controller.admin.message.vo.*;
import cn.iocoder.yudao.module.mp.convert.message.MpMessageConvert;
import cn.iocoder.yudao.module.mp.service.message.MpMessageService;

@Api(tags = "管理后台 - 粉丝消息表")
@RestController
@RequestMapping("/mp/message")
@Validated
public class MpMessageController {

    @Resource
    private MpMessageService mpMessageService;

    @GetMapping("/page")
    @ApiOperation("获得粉丝消息表分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:query')")
    public CommonResult<PageResult<MpMessageRespVO>> getWxFansMsgPage(@Valid MpMessagePageReqVO pageVO) {
        PageResult<MpMessageDO> pageResult = mpMessageService.getWxFansMsgPage(pageVO);
        return success(MpMessageConvert.INSTANCE.convertPage(pageResult));
    }

}
