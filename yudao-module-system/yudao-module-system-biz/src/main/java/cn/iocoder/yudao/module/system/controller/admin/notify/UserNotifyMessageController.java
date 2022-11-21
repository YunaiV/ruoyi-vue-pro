package cn.iocoder.yudao.module.system.controller.admin.notify;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageRespVO;
import cn.iocoder.yudao.module.system.convert.notify.NotifyMessageConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.enums.notify.NotifyReadStatusEnum;
import cn.iocoder.yudao.module.system.service.notify.NotifyMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

// TODO 芋艿：合并到 合并到 NotifyTemplateController 中
@Api(tags = "管理后台 - 站内信-消息中心")
@RestController
@RequestMapping("/system/user/notify-message")
@Validated
public class UserNotifyMessageController {

    @Resource
    private NotifyMessageService notifyMessageService;


    @GetMapping("/page")
    @ApiOperation("获得站内信分页")
    public CommonResult<PageResult<NotifyMessageRespVO>> getNotifyMessagePage(@Valid NotifyMessagePageReqVO pageVO) {
        pageVO.setUserId(getLoginUserId());
        pageVO.setUserType(UserTypeEnum.ADMIN.getValue());
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getNotifyMessagePage(pageVO);
        return success(NotifyMessageConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/latest/list")
    @ApiOperation("获得最新10站内信列表")
    public CommonResult<List<NotifyMessageRespVO>> getNotifyLatestMessageList() {
        NotifyMessagePageReqVO reqVO = new NotifyMessagePageReqVO();
        reqVO.setUserId(getLoginUserId());
        reqVO.setUserType(UserTypeEnum.ADMIN.getValue());
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getNotifyMessagePage(reqVO);
        if (CollUtil.isNotEmpty(pageResult.getList())) {
            return success(NotifyMessageConvert.INSTANCE.convertList(pageResult.getList()));
        }
        return success(Collections.emptyList());
    }

    @GetMapping("/unread/count")
    @ApiOperation("获得未读站内信数量")
    public CommonResult<Long> getUnreadNotifyMessageCount() {
        return success(notifyMessageService.getUnreadNotifyMessageCount(getLoginUserId(), UserTypeEnum.ADMIN.getValue()));
    }

    @GetMapping("/read")
    @ApiOperation("获得站内信")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    public CommonResult<NotifyMessageRespVO> readNotifyMessage(@RequestParam("id") Long id) {
        NotifyMessageDO notifyMessage = notifyMessageService.getNotifyMessage(id);
        // 记录消息已读。
        notifyMessageService.updateNotifyMessageReadStatus(id, NotifyReadStatusEnum.READ.getStatus());
        return success(NotifyMessageConvert.INSTANCE.convert(notifyMessage));
    }

    @GetMapping("/read/list")
    @ApiOperation("批量标记已读")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    public CommonResult<Boolean> batchUpdateNotifyMessageReadStatus(@RequestParam("ids") Collection<Long> ids) {
        notifyMessageService.batchUpdateNotifyMessageReadStatus(ids, getLoginUserId());
        return success(Boolean.TRUE);
    }

    @GetMapping("/read/all")
    @ApiOperation("所有未读消息标记已读")
    public CommonResult<Boolean> batchUpdateAllNotifyMessageReadStatus() {
        notifyMessageService.batchUpdateAllNotifyMessageReadStatus(getLoginUserId(), UserTypeEnum.ADMIN.getValue());
        return success(Boolean.TRUE);
    }
}
