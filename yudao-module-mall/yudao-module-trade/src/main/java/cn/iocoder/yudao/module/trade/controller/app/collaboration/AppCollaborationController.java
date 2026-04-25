package cn.iocoder.yudao.module.trade.controller.app.collaboration;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.collaboration.vo.AppCollaborationCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.collaboration.vo.AppCollaborationRespVO;
import cn.iocoder.yudao.module.trade.service.collaboration.CollaborationService;
import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabParticipantBO;
import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabSessionBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 App - 多方协作（CollaborationWorkflow）
 *
 * @author deepay
 */
@Tag(name = "用户 App - 多方协作")
@RestController
@RequestMapping("/trade/collaboration")
@Validated
public class AppCollaborationController {

    @Resource
    private CollaborationService collaborationService;

    @PostMapping("/create")
    @Operation(summary = "创建多方协作会话（采购/财务/物流协同）")
    public CommonResult<AppCollaborationRespVO> createSession(
            @Valid @RequestBody AppCollaborationCreateReqVO reqVO) {
        List<CollabParticipantBO> participants = reqVO.getParticipants().stream().map(p -> {
            CollabParticipantBO bo = new CollabParticipantBO();
            bo.setUserId(p.getUserId());
            bo.setName(p.getName());
            bo.setRole(p.getRole());
            return bo;
        }).collect(Collectors.toList());

        CollabSessionBO session = collaborationService.createSession(reqVO.getOrderId(), participants);
        AppCollaborationRespVO vo = new AppCollaborationRespVO();
        vo.setSession(session);
        return success(vo);
    }

    @GetMapping("/get")
    @Operation(summary = "获取协作会话详情")
    @Parameter(name = "sessionId", description = "协作会话 ID", required = true)
    public CommonResult<AppCollaborationRespVO> getSession(@RequestParam("sessionId") String sessionId) {
        CollabSessionBO session = collaborationService.getSession(sessionId);
        AppCollaborationRespVO vo = new AppCollaborationRespVO();
        vo.setSession(session);
        return success(vo);
    }

}
