package cn.iocoder.yudao.module.im.controller.admin.manager.message;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.privates.ImPrivateMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.privates.ImPrivateMessageManagerRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - IM 私聊消息")
@RestController
@RequestMapping("/im/manager/message/private")
@Validated
public class ImPrivateMessageManagerController {

    @Resource
    private ImPrivateMessageService privateMessageService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得私聊消息分页")
    @PreAuthorize("@ss.hasPermission('im:manager:message:query')")
    public CommonResult<PageResult<ImPrivateMessageManagerRespVO>> getPrivateMessagePage(
            @Valid ImPrivateMessageManagerPageReqVO pageReqVO) {
        // 1. 分页查询
        PageResult<ImPrivateMessageDO> pageResult = privateMessageService.getPrivateMessagePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2.1 一次性批量查询发送人 + 接收人昵称
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(pageResult.getList(),
                m -> Stream.of(m.getSenderId(), m.getReceiverId())));
        // 2.2 转换为 VO，填充昵称
        return success(BeanUtils.toBean(pageResult, ImPrivateMessageManagerRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getSenderId(), user -> vo.setSenderNickname(user.getNickname()));
            MapUtils.findAndThen(userMap, vo.getReceiverId(), user -> vo.setReceiverNickname(user.getNickname()));
        }));
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
