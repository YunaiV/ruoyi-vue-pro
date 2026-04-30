package cn.iocoder.yudao.module.im.controller.admin.manager.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.group.ImGroupMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.group.ImGroupMessageManagerRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 群聊消息")
@RestController
@RequestMapping("/im/manager/message/group")
@Validated
public class ImGroupMessageManagerController {

    @Resource
    private ImGroupMessageService groupMessageService;

    @GetMapping("/page")
    @Operation(summary = "获得群聊消息分页")
    @PreAuthorize("@ss.hasPermission('im:manager:message:query')")
    public CommonResult<PageResult<ImGroupMessageManagerRespVO>> getGroupMessagePage(
            @Valid ImGroupMessageManagerPageReqVO pageReqVO) {
        return success(groupMessageService.getGroupMessagePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得群聊消息详情")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:message:query')")
    public CommonResult<ImGroupMessageManagerRespVO> getGroupMessage(@RequestParam("id") Long id) {
        ImGroupMessageDO message = groupMessageService.getGroupMessage(id);
        return success(BeanUtils.toBean(message, ImGroupMessageManagerRespVO.class));
    }

}
