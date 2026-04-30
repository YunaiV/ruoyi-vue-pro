package cn.iocoder.yudao.module.im.controller.admin.manager.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.privates.ImPrivateMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.privates.ImPrivateMessageManagerRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 私聊消息")
@RestController
@RequestMapping("/im/manager/message/private")
@Validated
public class ImPrivateMessageManagerController {

    @Resource
    private ImPrivateMessageService privateMessageService;

    @GetMapping("/page")
    @Operation(summary = "获得私聊消息分页")
    @PreAuthorize("@ss.hasPermission('im:manager:message:query')")
    public CommonResult<PageResult<ImPrivateMessageManagerRespVO>> getPrivateMessagePage(
            @Valid ImPrivateMessageManagerPageReqVO pageReqVO) {
        return success(privateMessageService.getPrivateMessagePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得私聊消息详情")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:message:query')")
    public CommonResult<ImPrivateMessageManagerRespVO> getPrivateMessage(@RequestParam("id") Long id) {
        ImPrivateMessageDO message = privateMessageService.getPrivateMessage(id);
        return success(BeanUtils.toBean(message, ImPrivateMessageManagerRespVO.class));
    }

}
