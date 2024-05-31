package cn.iocoder.yudao.module.promotion.controller.app.kefu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message.AppKeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import cn.iocoder.yudao.module.promotion.service.kefu.KeFuMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 客服消息")
@RestController
@RequestMapping("/promotion/kefu-message")
@Validated
public class AppKeFuMessageController {

    @Resource
    private KeFuMessageService kefuMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送客服消息")
    @PreAuthenticated
    public CommonResult<Long> createKefuMessage(@Valid @RequestBody AppKeFuMessageSendReqVO sendReqVO) {
        return success(kefuMessageService.sendKefuMessage(BeanUtils.toBean(sendReqVO, KeFuMessageSendReqVO.class)));
    }

    @PutMapping("/update-read-status")
    @Operation(summary = "更新客服消息已读状态")
    @Parameter(name = "conversationId", description = "会话编号", required = true)
    @PreAuthenticated
    public CommonResult<Boolean> updateKefuMessageReadStatus(@RequestParam("conversationId") Long conversationId) {
        kefuMessageService.updateKefuMessageReadStatus(conversationId, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得客服消息分页")
    @PreAuthenticated
    public CommonResult<PageResult<KeFuMessageRespVO>> getKefuMessagePage(@Valid KeFuMessagePageReqVO pageReqVO) {
        PageResult<KeFuMessageDO> pageResult = kefuMessageService.getKefuMessagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, KeFuMessageRespVO.class));
    }

}