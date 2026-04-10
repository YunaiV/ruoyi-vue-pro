package cn.iocoder.yudao.module.mp.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.template.MpMessageTemplateListReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.template.MpMessageTemplateRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.template.MpMessageTemplateSendReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageTemplateDO;
import cn.iocoder.yudao.module.mp.service.message.MpMessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 公众号模版消息")
@RestController
@RequestMapping("/mp/message-template")
@Validated
public class MpMessageTemplateController {

    @Resource
    private MpMessageTemplateService messageTemplateService;

    @DeleteMapping("/delete")
    @Operation(summary = "删除模版消息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mp:message-template:delete')")
    public CommonResult<Boolean> deleteMessageTemplate(@RequestParam("id") Long id) {
        messageTemplateService.deleteMessageTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得模版消息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mp:message-template:query')")
    public CommonResult<MpMessageTemplateRespVO> getMessageTemplate(@RequestParam("id") Long id) {
        MpMessageTemplateDO msgTemplate = messageTemplateService.getMessageTemplate(id);
        return success(BeanUtils.toBean(msgTemplate, MpMessageTemplateRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得模版消息列表")
    @Parameter(name = "accountId", description = "公众号账号的编号", required = true, example = "2048")
    @PreAuthorize("@ss.hasPermission('mp:message-template:query')")
    public CommonResult<List<MpMessageTemplateRespVO>> getMessageTemplateList(MpMessageTemplateListReqVO listReqVO) {
        List<MpMessageTemplateDO> list = messageTemplateService.getMessageTemplateList(listReqVO);
        return success(BeanUtils.toBean(list, MpMessageTemplateRespVO.class));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步公众号模板")
    @Parameter(name = "accountId", description = "公众号账号的编号", required = true, example = "2048")
    @PreAuthorize("@ss.hasPermission('mp:message-template:sync')")
    public CommonResult<Boolean> syncMessageTemplate(@RequestParam("accountId") Long accountId) {
        messageTemplateService.syncMessageTemplate(accountId);
        return success(true);
    }

    @PostMapping("/send")
    @Operation(summary = "给粉丝发送模版消息")
    @PreAuthorize("@ss.hasPermission('mp:message-template:send')")
    public CommonResult<Boolean> sendMessageTemplate(@Valid @RequestBody MpMessageTemplateSendReqVO sendReqVO) {
        messageTemplateService.sendMessageTempalte(sendReqVO);
        return success(true);
    }

}