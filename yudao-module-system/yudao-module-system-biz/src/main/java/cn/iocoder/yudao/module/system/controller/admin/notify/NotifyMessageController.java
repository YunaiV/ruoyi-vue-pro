package cn.iocoder.yudao.module.system.controller.admin.notify;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageRespVO;
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
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "管理后台 - 我的站内信")
@RestController
@RequestMapping("/system/notify-message")
@Validated
public class NotifyMessageController {

    @Resource
    private NotifyMessageService notifyMessageService;

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

    @GetMapping("/get-recent-list")
    @ApiOperation("获取当前用户最新站内信，默认10条")
    @ApiImplicitParam(name = "size", value = "10", defaultValue = "10", dataTypeClass = Integer.class)
    public CommonResult<List<NotifyMessageRespVO>> getRecentList(@RequestParam(name = "size", defaultValue = "10") Integer size) {
        NotifyMessagePageReqVO reqVO = new NotifyMessagePageReqVO();
        List<NotifyMessageDO> pageResult = notifyMessageService.getNotifyMessageList(reqVO, size);
        if (CollUtil.isNotEmpty(pageResult)) {
            return success(NotifyMessageConvert.INSTANCE.convertList(pageResult));
        }
        return success(Collections.emptyList());
    }

    @GetMapping("/get-unread-count")
    @ApiOperation("获得未读站内信数量")
    public CommonResult<Long> getUnreadCount() {
        return success(notifyMessageService.getUnreadNotifyMessageCount(getLoginUserId(), UserTypeEnum.ADMIN.getValue()));
    }

    @PutMapping("/update-list-read")
    @ApiOperation("批量标记已读")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    public CommonResult<Boolean> batchUpdateNotifyMessageReadStatus(@RequestBody List<Long> ids) {
        notifyMessageService.batchUpdateNotifyMessageReadStatus(ids, getLoginUserId());
        return success(Boolean.TRUE);
    }

    @PutMapping("/update-all-read")
    @ApiOperation("所有未读消息标记已读")
    public CommonResult<Boolean> batchUpdateAllNotifyMessageReadStatus() {
        notifyMessageService.batchUpdateAllNotifyMessageReadStatus(getLoginUserId(), UserTypeEnum.ADMIN.getValue());
        return success(Boolean.TRUE);
    }


}
