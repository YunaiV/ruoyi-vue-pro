package cn.iocoder.yudao.module.mp.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply.MpAutoReplyRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.message.MpMessagePageReqVO;
import cn.iocoder.yudao.module.mp.convert.message.MpAutoReplyConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpAutoReplyDO;
import cn.iocoder.yudao.module.mp.service.message.MpAutoReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 公众号自动回复")
@RestController
@RequestMapping("/mp/auto-reply")
@Validated
public class MpAutoReplyController {

    @Resource
    private MpAutoReplyService mpAutoReplyService;

    @GetMapping("/page")
    @ApiOperation("获得公众号自动回复分页")
    @PreAuthorize("@ss.hasPermission('mp:auto-reply:query')")
    public CommonResult<PageResult<MpAutoReplyRespVO>> getAutoReplyPage(@Valid MpMessagePageReqVO pageVO) {
        PageResult<MpAutoReplyDO> pageResult = mpAutoReplyService.getAutoReplyPage(pageVO);
        return success(MpAutoReplyConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get")
    @ApiOperation("获得公众号自动回复")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mp:auto-reply:query')")
    public CommonResult<MpAutoReplyRespVO> getAutoReply(@RequestParam("id") Long id) {
        MpAutoReplyDO autoReply = mpAutoReplyService.getAutoReply(id);
        return success(MpAutoReplyConvert.INSTANCE.convert(autoReply));
    }

}
