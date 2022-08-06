package cn.iocoder.yudao.module.system.controller.admin.notify;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageRespVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.notify.NotifyMessageConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.service.notify.NotifyMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 站内信")
@RestController
@RequestMapping("/system/notify-message")
@Validated
public class NotifyMessageController {

    @Resource
    private NotifyMessageService notifyMessageService;

    @PostMapping("/create")
    @ApiOperation("创建站内信")
    @PreAuthorize("@ss.hasPermission('system:notify-message:create')")
    public CommonResult<Long> createNotifyMessage(@Valid @RequestBody NotifyMessageCreateReqVO createReqVO) {
        return success(notifyMessageService.createNotifyMessage(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新站内信")
    @PreAuthorize("@ss.hasPermission('system:notify-message:update')")
    public CommonResult<Boolean> updateNotifyMessage(@Valid @RequestBody NotifyMessageUpdateReqVO updateReqVO) {
        notifyMessageService.updateNotifyMessage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除站内信")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:notify-message:delete')")
    public CommonResult<Boolean> deleteNotifyMessage(@RequestParam("id") Long id) {
        notifyMessageService.deleteNotifyMessage(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得站内信")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:notify-message:query')")
    public CommonResult<NotifyMessageRespVO> getNotifyMessage(@RequestParam("id") Long id) {
        NotifyMessageDO notifyMessage = notifyMessageService.getNotifyMessage(id);
        return success(NotifyMessageConvert.INSTANCE.convert(notifyMessage));
    }

    @GetMapping("/page")
    @ApiOperation("获得站内信分页")
    @PreAuthorize("@ss.hasPermission('system:notify-message:query')")
    public CommonResult<PageResult<NotifyMessageRespVO>> getNotifyMessagePage(@Valid NotifyMessagePageReqVO pageVO) {
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getNotifyMessagePage(pageVO);
        return success(NotifyMessageConvert.INSTANCE.convertPage(pageResult));
    }
}
