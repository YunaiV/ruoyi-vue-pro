package cn.iocoder.yudao.module.promotion.controller.admin.kefu;

import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageSendReqVO;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import cn.iocoder.yudao.module.promotion.service.kefu.KeFuMessageService;

@Tag(name = "管理后台 - 客服消息")
@RestController
@RequestMapping("/promotion/kefu-message")
@Validated
public class KeFuMessageController {

    @Resource
    private KeFuMessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "发送客服消息")
    @PreAuthorize("@ss.hasPermission('promotion:kefu-message:send')")
    public CommonResult<Long> createKefuMessage(@Valid @RequestBody KeFuMessageSendReqVO sendReqVO) {
        return success(messageService.sendKefuMessage(sendReqVO));
    }

    @PutMapping("/update-read-status")
    @Operation(summary = "更新客服消息已读状态")
    @Parameter(name = "conversationId", description = "会话编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:kefu-message:update')")
    public CommonResult<Boolean> updateKefuMessageReadStatus(@RequestParam("conversationId") Long conversationId) {
        messageService.updateKefuMessageReadStatus(conversationId, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得客服消息分页")
    @PreAuthorize("@ss.hasPermission('promotion:kefu-message:query')")
    public CommonResult<PageResult<KeFuMessageRespVO>> getKefuMessagePage(@Valid KeFuMessagePageReqVO pageReqVO) {
        PageResult<KeFuMessageDO> pageResult = messageService.getKefuMessagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, KeFuMessageRespVO.class));
    }

}