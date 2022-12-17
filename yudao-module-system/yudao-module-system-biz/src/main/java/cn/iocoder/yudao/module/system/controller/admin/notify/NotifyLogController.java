package cn.iocoder.yudao.module.system.controller.admin.notify;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.log.NotifyLogBaseVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.log.NotifyLogPageReqVO;
import cn.iocoder.yudao.module.system.convert.notify.NotifyLogConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.service.notify.NotifyLogService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * <p>
 *
 * </p>
 *
 * @author LuoWenFeng
 */
@Api(tags = "管理后台 - 站内信发送日志")
@RestController
@RequestMapping("/system/notify-log")
@Validated
public class NotifyLogController {

    @Resource
    private NotifyLogService notifyLogService;

    @Resource
    private AdminUserService userService;

    @GetMapping("/page")
    @ApiOperation("获得发送站内信日志分页")
    public CommonResult<PageResult<NotifyLogBaseVO>> getNotifyLogPage(@Valid NotifyLogPageReqVO pageVO) {
        PageResult<NotifyMessageDO> pageResult = notifyLogService.getNotifyMessageSendPage(pageVO);
        PageResult<NotifyLogBaseVO> result = NotifyLogConvert.INSTANCE.convertPage(pageResult);
        result.getList().forEach(v -> {
            v.setReceiveUserName(userService.getUser(v.getUserId()).getNickname());
        });
        return success(result);
    }


}
